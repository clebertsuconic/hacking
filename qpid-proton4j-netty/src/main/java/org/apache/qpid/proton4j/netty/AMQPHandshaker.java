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

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/**
 * The handshaker can be added to an acceptor pipiline to decide upon AMQP Processing.
 * Sub classes here will instatiate the proper AMQPHandler and AMQPSaslHandler, which are abstract classes
 * that can be used as extension points for your own processing.
 *
 * @author Clebert Suconic
 */

public abstract class AMQPHandshaker extends ByteToMessageDecoder {

   public static final byte[] handShakeBare = new byte[]{'A', 'M', 'Q', 'P', 0, 1, 0, 0};

   public static final byte[] handShakeSASL = new byte[]{'A', 'M', 'Q', 'P', 4, 1, 0, 0};

   protected final int maxFrameSize;

   public AMQPHandshaker(int maxFrameSize) {
      this.maxFrameSize = maxFrameSize;
   }

   @Override
   protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
      if (ctx.isRemoved()) {
         return;
      }
      if (in.readableBytes() < handShakeBare.length) {
         return;
      }

      if (checkHeader(in, handShakeBare)) {
         // TODO: Setup proper max size on frame size
         ctx.pipeline().addLast(new LengthFieldBasedFrameDecoder(maxFrameSize,0, 4, -4, 0));
         ctx.pipeline().addLast(createHandler(ctx));
         ctx.pipeline().remove(this);
         ctx.writeAndFlush(Unpooled.wrappedBuffer(handShakeBare));
      } else if (checkHeader(in, handShakeSASL)) {
         // SASL.. TODO
         System.err.println("SASL not implemented yet!");
         ctx.write(Unpooled.wrappedBuffer(handShakeBare));
         in.readerIndex(in.writerIndex());
         ctx.channel().close();
         ctx.flush();

      } else {
         ctx.write(Unpooled.wrappedBuffer(handShakeBare));
         in.readerIndex(in.writerIndex());
         ctx.flush();
         ctx.close();
      }

   }

   /** this is an extention point.
    *  When you use the Handshaker in your project you can instantiate your own Handler extending AMQPHandler */
   protected abstract AMQPHandler createHandler(ChannelHandlerContext ctx);


   private boolean checkHeader(ByteBuf buffer, byte[] bytes) {
      for (byte b : bytes) {
         if (b != buffer.readByte()) {
            return false;
         }
      }

      return true;
   }

   @Override
   public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
      // Close the connection when an exception is raised.
      cause.printStackTrace();
      ctx.close();
   }
}
