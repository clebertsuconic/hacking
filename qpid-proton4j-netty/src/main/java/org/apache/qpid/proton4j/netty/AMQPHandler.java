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
import org.apache.qpid.proton4j.amqp.Symbol;
import org.apache.qpid.proton4j.amqp.UnsignedInteger;
import org.apache.qpid.proton4j.amqp.transport.Attach;
import org.apache.qpid.proton4j.amqp.transport.Begin;
import org.apache.qpid.proton4j.amqp.transport.Close;
import org.apache.qpid.proton4j.amqp.transport.End;
import org.apache.qpid.proton4j.amqp.transport.ErrorCondition;
import org.apache.qpid.proton4j.amqp.transport.Open;
import org.apache.qpid.proton4j.amqp.transport.Performative;
import org.apache.qpid.proton4j.amqp.transport.Role;
import org.apache.qpid.proton4j.buffer.ProtonBuffer;
import org.apache.qpid.proton4j.codec.CodecFactory;
import org.apache.qpid.proton4j.codec.DecoderState;
import org.apache.qpid.proton4j.codec.EncoderState;
import org.apache.qpid.proton4j.engine.Transport;
import org.apache.qpid.proton4j.engine.state.Connection;
import org.apache.qpid.proton4j.engine.state.ConnectionError;
import org.apache.qpid.proton4j.engine.state.EndpointState;
import org.apache.qpid.proton4j.engine.state.LinkImpl;
import org.apache.qpid.proton4j.engine.state.Session;
import org.apache.qpid.proton4j.engine.state.TransportLink;
import org.apache.qpid.proton4j.engine.state.TransportSession;
import org.apache.qpid.proton4j.netty.buffer.NettyProtonBuffer;

/**
 * @author Clebert Suconic
 */

public abstract class AMQPHandler extends ChannelDuplexHandler implements Transport {

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
      return (Performative) state.getDecoder().readObject(buffer, state);
   }

   protected final Channel nettyChannel;

   public AMQPHandler(Channel channel, int maxFrameSize) {
      nettyChannel = channel;
      this.maxFrameSize = maxFrameSize;
   }

   // TODO move into connections?
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

   protected final int maxFrameSize;
   protected int remoteFrameSize = 512;

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
            handleOpen((Open) currentPerformative);
            break;
         case CLOSE:
            handleClose((Close) currentPerformative);
            break;
         case BEGIN:
            handleBegin((Begin) currentPerformative);
            break;
         case END:
            handleEnd((End) currentPerformative);
            break;
         case ATTACH:
            handleAttach((Attach) currentPerformative);
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


   public void handleOpen(Open open) {
      if (connection != null && connection.getRemoteState() == EndpointState.ACTIVE) {
         sendError(ConnectionError.FRAMING_ERROR, "Connection previously open");
         return;
      }
      this.remoteFrameSize = open.getMaxFrameSize().intValue();
      connection = createConnection();
      connection.setRemoteState(EndpointState.ACTIVE);
      connection.setRemoteHostname(open.getHostname());
      connection.setRemoteContainer(open.getContainerId());
      connection.setRemoteDesiredCapabilities(open.getDesiredCapabilities());
      connection.setRemoteOfferedCapabilities(open.getOfferedCapabilities());
      connection.setRemoteProperties(open.getProperties());
      connection.setRemoteIdleTimeout(open.getIdleTimeOut());

      connectionOpened(open, connection);
   }

   public void handleClose(Close close) {
      if (connection != null && connection.getRemoteState() == EndpointState.ACTIVE) {
         sendError(ConnectionError.FRAMING_ERROR, "Connection previously open");
         return;
      }
      connection.handleClose(close);
      connectionClosed(close, connection);
   }

   public void handleBegin(Begin begin) {
      if (connection == null) {
         sendError(ConnectionError.FRAMING_ERROR, "no connection opened");
         return;
      }

      Session session;

      if (begin.getRemoteChannel() == null) {
         session = connection.newSession();
         if (sessions.get(currentChannel) != null) {
            sendError(ConnectionError.FRAMING_ERROR, "Session previously set");
            return;
         }
         session.setChannel(currentChannel);
         sessions.put(currentChannel, session);
      } else {
         session = sessions.get(begin.getRemoteChannel().shortValue());
         if (session == null) {
            sendError(ConnectionError.FRAMING_ERROR, "uncorrelated channel " + begin.getRemoteChannel());
         }
      }
      session.setChannel(currentChannel);
      session.setRemoteState(EndpointState.ACTIVE);
      session.setRemoteProperties(begin.getProperties());
      session.setHandleMax(begin.getHandleMax());
      session.setOutgoingWindow(begin.getOutgoingWindow().intValue());
      session.setIncomingWindow(begin.getIncomingWindow().intValue());
      session.setRemoteDesiredCapabilities(begin.getDesiredCapabilities());
      session.setRemoteOfferedCapabilities(begin.getOfferedCapabilities());
      sessionBegin(begin, session);
   }

   public void sendError(Symbol symbol, String message) {

      ErrorCondition condition = new ErrorCondition(symbol, message);
      Close close = new Close();
      close.setError(condition);
      sendFrame((short) 0, (byte) 0, close);
      nettyChannel.close();
   }

   public void handleAttach(Attach attach) {

      Session session = sessions.get(currentChannel);

      if (session == null) {
         sendError(ConnectionError.FRAMING_ERROR, "Session does not exist");
         return;
      }
      final UnsignedInteger handle = attach.getHandle();
      if (handle.compareTo(session.getHandleMax()) > 0) {
         // The handle-max value is the highest handle value that can be used on the session. A peer MUST
         // NOT attempt to attach a link using a handle value outside the range that its partner can handle.
         // A peer that receives a handle outside the supported range MUST close the connection with the
         // framing-error error-code.
         sendError(ConnectionError.FRAMING_ERROR, "handle-max exceeded");
         return;
      }

      TransportSession transportSession = session.getTransportSession();

      TransportLink<?> transportLink = transportSession.getLinkFromRemoteHandle(handle);
      LinkImpl link = null;

      if(transportLink != null)
      {
         sendError(ConnectionError.FRAMING_ERROR, "handle was previously use");
         return;
      }
      else
      {
         transportLink = transportSession.resolveHalfOpenLink(attach.getName());
         if(transportLink == null)
         {

            link = new LinkImpl(attach.getRole(), session, attach.getName());
            transportLink = new TransportLink(link);
         }
         else
         {
            link = transportLink.getLink();
         }
         if(attach.getRole() == Role.SENDER)
         {
            transportLink.setDeliveryCount(attach.getInitialDeliveryCount());
         }

         link.setRemoteState(EndpointState.ACTIVE);
         link.setRemoteSource(attach.getSource());
         link.setRemoteTarget(attach.getTarget());

         link.setRemoteReceiverSettleMode(attach.getRcvSettleMode());
         link.setRemoteSenderSettleMode(attach.getSndSettleMode());

         link.setRemoteProperties(attach.getProperties());

         link.setRemoteDesiredCapabilities(attach.getDesiredCapabilities());
         link.setRemoteOfferedCapabilities(attach.getOfferedCapabilities());

         link.setRemoteMaxMessageSize(attach.getMaxMessageSize());

         transportLink.setName(attach.getName());
         transportLink.setRemoteHandle(handle);
         transportSession.addLinkRemoteHandle(transportLink, handle);

      }

   }

   public void handleEnd(End end) {
      Session session = sessions.remove(currentChannel);
      sessionEnded(end, currentChannel, session);

   }

   protected void connectionOpened(Open open, Connection connection) {
      this.remoteFrameSize = open.getMaxFrameSize().intValue();
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
      sendFrame(session.getChannel(), (byte) 0, begin);
   }

   protected void sendEnd(Session session, short channel, End end) {
      sendFrame(channel, (byte) 0, end);
   }

   public void sendOpen(Connection connection) {
      Open open = new Open();
      open.setMaxFrameSize(new UnsignedInteger(maxFrameSize));
      open.setChannelMax(connection.getMaxChannels());
      open.setIdleTimeOut(connection.getLocalIdleTimeout());
      open.setContainerId(connection.getRemoteContainer());
      open.setIdleTimeOut(connection.getRemoteIdleTimeout());

      sendFrame((short) 0, (byte) 0, open);

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

   public void sendClose(Connection connection) {
      Close close = new Close();
      sendFrame((short) 0, (byte) 0, close);
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

   @Override
   public int getMaxFrameSize() {
      return maxFrameSize;
   }

   @Override
   public int getRemoteMaxFrameSize() {
      return remoteFrameSize;
   }
}
