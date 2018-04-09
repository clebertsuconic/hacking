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

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import org.apache.qpid.proton4j.netty.AMQPHandshaker;

/**
 * This is really a sample on how you would implement your own AMQP Server
 * You can create your own server, but feel free to use this one if you prefer.
 */

public abstract class NettyAbstractServer {

   static final boolean SSL = System.getProperty("ssl") != null;
   static final int PORT = Integer.parseInt(System.getProperty("port", "5672"));

   ChannelFuture server;
   EventLoopGroup bossGroup;
   EventLoopGroup workerGroup;



   public void stop() {
      if (server != null) {
         Channel channel = server.channel();
         if (channel != null) {
            channel.closeFuture();
         }
         bossGroup.shutdownGracefully();
         workerGroup.shutdownGracefully();
      }
   }

   public void start(String... args) throws Exception {
      // Configure SSL.
      final SslContext sslCtx;
      if (SSL) {
         SelfSignedCertificate ssc = new SelfSignedCertificate();
         sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
      } else {
         sslCtx = null;
      }

//
//      ChannelInitializer<Channel> factory = new ChannelInitializer<Channel>() {
//         @Override
//         public void initChannel(Channel channel) throws Exception {
//            ChannelPipeline pipeline = channel.pipeline();
//            pipeline.addLast(new LoggingHandler(LogLevel.INFO));
//            pipeline.addLast(new AMQPHandler());
//         }
//      };
//
//      // Configure the server.
      bossGroup = new NioEventLoopGroup(1);
      workerGroup = new NioEventLoopGroup();
         ServerBootstrap b = new ServerBootstrap();
         b.group(bossGroup, workerGroup)
            .channel(NioServerSocketChannel.class)
            .option(ChannelOption.SO_BACKLOG, 100)
            .handler(new LoggingHandler(LogLevel.INFO))
            .childHandler(new ChannelInitializer<SocketChannel>() {
               @Override
               public void initChannel(SocketChannel ch) throws Exception {
                  ChannelPipeline p = ch.pipeline();
                  if (sslCtx != null) {
                     p.addLast(sslCtx.newHandler(ch.alloc()));
                  }
                  p.addLast(new LoggingHandler(LogLevel.INFO));
                  p.addLast(createHandshaker());
               }
            });

         // Start the server.
         server = b.bind("localhost", PORT);
         System.out.println("Server = " + server);
         server.await();
//         server.sync();
//
//         // Wait until the server socket is closed.
//         server.channel().closeFuture().sync();
   }

   protected abstract AMQPHandshaker createHandshaker();
}
