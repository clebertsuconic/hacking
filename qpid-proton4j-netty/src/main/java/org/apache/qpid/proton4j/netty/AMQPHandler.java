/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.qpid.proton4j.netty;

import java.io.IOException;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import org.apache.qpid.proton4j.amqp.transport.Begin;
import org.apache.qpid.proton4j.amqp.transport.Close;
import org.apache.qpid.proton4j.amqp.transport.End;
import org.apache.qpid.proton4j.amqp.transport.Open;
import org.apache.qpid.proton4j.amqp.transport.Performative;
import org.apache.qpid.proton4j.buffer.ProtonBuffer;
import org.apache.qpid.proton4j.codec.CodecFactory;
import org.apache.qpid.proton4j.codec.DecoderState;
import org.apache.qpid.proton4j.codec.EncoderState;
import org.apache.qpid.proton4j.engine.Processor;
import org.apache.qpid.proton4j.netty.buffer.NettyProtonBuffer;
import org.apache.qpid.proton4j.engine.state.*;

/**
 * @author Clebert Suconic
 */

public abstract class AMQPHandler extends ChannelDuplexHandler implements Processor {

   // I like this to be able to debug this
   // setting to false will make the compiler to ignore the statements
   private static final boolean SYSTEM_OUT_DEBUG = false;

   public static void writePerformative(ProtonBuffer buffer, byte frameType, short channel, Performative performative) {
      buffer.writeInt(0);
      buffer.writeByte(2);
      buffer.writeByte(frameType);
      buffer.writeShort(channel);

      EncoderState encoderState = encoderStateThreadLocal.get();
      encoderState.getEncoder().writeObject(buffer, encoderState, performative);
      // setting up the size of the frame, 4 first bytes
      buffer.setInt(0, buffer.getWriteIndex());
   }


   public static Performative readPerformative(ProtonBuffer buffer) throws IOException {
      DecoderState state = decoderStateThreadLocal.get();
      return (Performative)state.getDecoder().readObject(buffer, state);
   }



   protected final Channel nettyChannel;

   public AMQPHandler(Channel channel) {
      nettyChannel = channel;
   }

   Short2ObjectMap<Session> sessions = new Short2ObjectOpenHashMap<>(2, 0.75f);

   // TODO: Perhaps use ChannelLocal? they seem faster
   private static final ThreadLocal<NettyProtonBuffer> protonBuffer = ThreadLocal.withInitial(() -> new NettyProtonBuffer());
   private static final ThreadLocal<DecoderState> decoderStateThreadLocal = ThreadLocal.withInitial(() -> CodecFactory.getDecoder().newDecoderState());
   private static final ThreadLocal<EncoderState> encoderStateThreadLocal = ThreadLocal.withInitial(() -> CodecFactory.getEncoder().newEncoderState());

   // data about the current frame being processed
   // notice that the Handler is being used within the Netty Processors...
   protected short currentDoff;
   protected short currentFrameType;
   protected short currentChannel;
   protected byte[] currentPayload;

   protected Performative currentPerformative;

   protected Connection connection;


   // Connection

   public Performative getCurrentPerformative() {
      return currentPerformative;
   }


   @Override
   public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
      //AmqpFrameParser parser = new AmqpFrameParser(decoderStateThreadLocal.get().getDecoder(), Integer.MAX_VALUE);

      ByteBuf nettyBuffer = ((ByteBuf) msg);
      if (SYSTEM_OUT_DEBUG) {
         debugInput(nettyBuffer);
      }
      readFrame(nettyBuffer);

      if (SYSTEM_OUT_DEBUG) {
         System.out.println("performative:" + currentPerformative);
      }

      switch (currentPerformative.getPerformativeType()) {
         case OPEN:
            handleOpen((Open) currentPerformative, currentPayload, currentChannel);
            break;
         case CLOSE:
            handleClose((Close) currentPerformative, currentPayload, currentChannel);
            break;
         case BEGIN:
            handleBegin((Begin) currentPerformative, currentPayload, currentChannel);
            break;
         case END:
            handleEnd((End) currentPerformative, currentPayload, currentChannel);
            break;
         default:
            System.out.println("Normative " + currentPerformative + " not implemented yet");
            ctx.channel().writeAndFlush(nettyBuffer);
      }

      //parser.parse(new ProtonTransportHandlerContext("test", null, null), buffer);

      //ctx.fireChannelRead(msg);

      //ctx.write(nettyBuffer.readerIndex(0).retain());
      //ctx.flush();
   }

   public void readFrame(ByteBuf nettyBuffer) throws IOException {
      NettyProtonBuffer buffer = protonBuffer.get().setBuffer(nettyBuffer);
      readFrame(buffer);
   }

   public void readFrame(ProtonBuffer buffer) throws IOException {
      buffer.skipBytes(4);
      currentDoff = buffer.readByte();
      currentFrameType = buffer.readByte();
      currentChannel = buffer.readShort();
      buffer.setReadIndex(currentDoff * 4);

      currentPerformative = readPerformative(buffer);

      if (buffer.isReadable()) {
         currentPayload = new byte[buffer.getReadableBytes()];
         buffer.readBytes(currentPayload);
      }
   }

   public void sendError(String message) {
      // TODO: What to do?
      nettyChannel.close();
   }

   public void handleOpen(Open open, byte[] payload, short channel)
   {
      if (connection != null && connection.getRemoteState() == EndpointState.ACTIVE) {
         sendError("Connection previously open");
         return;
      }
      connection = createConnection();
      connection.handleOpen(open);
      connectionOpened(open, connection);
   }

   public void handleClose(Close close, byte[] payload, short channel)
   {
      if (connection != null && connection.getRemoteState() == EndpointState.ACTIVE) {
         sendError("Connection previously open");
         return;
      }
      connection.handleClose(close);
      connectionClosed(close, connection);
   }

   public void handleBegin(Begin begin, byte[] payload, short channel)
   {
      if (connection == null) {
         sendError("no connection opened");
         return;
      }

      Session session;

      if(begin.getRemoteChannel() == null)
      {
         session = connection.newSession();
         if (sessions.get(channel) != null) {
            sendError("Session previously set");
            return;
         }
         session.setChannel(channel);
         sessions.put(channel, session);
      }
      else
      {
         session = sessions.get(begin.getRemoteChannel().shortValue());
         if (session == null) {
            sendError("uncorrelated channel " + begin.getRemoteChannel());
         }
      }
      session.setChannel(channel);
      session.setRemoteState(EndpointState.ACTIVE);
      session.setRemoteProperties(begin.getProperties());
      session.setOutgoingWindow(begin.getOutgoingWindow().intValue());
      session.setIncomingWindow(begin.getIncomingWindow().intValue());
      session.setRemoteDesiredCapabilities(begin.getDesiredCapabilities());
      session.setRemoteOfferedCapabilities(begin.getOfferedCapabilities());
      sessionBegin(begin, session);
   }

   public void handleEnd(End end, byte[] payload, short channel) {
      Session session = sessions.remove(channel);
      sessionEnded(end, channel, session);

   }


   protected void connectionOpened(Open open, Connection connection) {
      sendOpen(connection);
   }

   protected void connectionClosed(Close close, Connection connection) {
      sendClose(connection);
   }

   protected void sessionBegin(Begin begin, Session session) {
      sendBegin(begin, session);
   }

   protected void sessionEnded(End end, short channel, Session session) {
      sendEnd(session, channel, end);
   }


   public Connection createConnection() {
      return new Connection(this);
   }

   protected void sendBegin(Begin begin, Session session) {
     sendFrame(session.getChannel(), (byte)0, begin);
   }

   protected void sendEnd(Session session, short channel, End end) {
      sendFrame(channel, (byte)0, end);
   }

   @Override
   public void sendOpen(Connection connection) {
      Open open = new Open();
      open.setChannelMax(connection.getMaxChannels());
      open.setIdleTimeOut(connection.getLocalIdleTimeout());
      open.setContainerId(connection.getRemoteContainer());
      open.setIdleTimeOut(connection.getRemoteIdleTimeout());

      sendFrame((short)0, (byte)0, open);

   }

   private void sendFrame(short channel, byte frameType, Performative performative) {
      sendFrame(channel, frameType, performative, 1024);
   }
   private void sendFrame(short channel, byte frameType, Performative performative, int expectedSize) {
      ByteBuf nettyBuffer = nettyChannel.alloc().buffer(expectedSize);
      ProtonBuffer wrappedBuffer = protonBuffer.get().setBuffer(nettyBuffer);
      writePerformative(wrappedBuffer, frameType, channel, performative);
      System.out.println("Sending:" + ByteBufUtil.hexDump(nettyBuffer));
      nettyChannel.writeAndFlush(nettyBuffer);
      //debugOutput(nettyBuffer);
   }

   @Override
   public void sendClose(Connection connection) {
      Close close = new Close();
      sendFrame((short)0, (byte)0, close);
   }

   private void debugInput(ByteBuf buffer) {
      synchronized (System.out) {
         StringBuilder builder = new StringBuilder();
         ByteBufUtil.appendPrettyHexDump(builder, buffer);
         System.out.println(builder);
         System.out.flush();
      }
   }

   private void debugOutput(ByteBuf buffer) {
      synchronized (System.out) { // I really meant out. want to avoid garbling debug messages
         StringBuilder builder = new StringBuilder();
         ByteBufUtil.appendPrettyHexDump(builder, buffer);
         System.err.println(builder);
         System.err.flush();
      }
   }

}
