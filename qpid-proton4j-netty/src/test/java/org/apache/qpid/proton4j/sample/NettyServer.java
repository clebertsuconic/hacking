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

package org.apache.qpid.proton4j.sample;

import org.apache.qpid.proton4j.netty.AMQPHandshaker;
import org.apache.qpid.proton4j.netty.NettyAbstractServer;

/**
 * @author Clebert Suconic
 */

public class NettyServer extends NettyAbstractServer {

   static NettyAbstractServer server;

   public static void main(String[] args) throws Exception {
      startServer(args);
   }


   public static void startServer(String... args) throws Exception {
      server = new NettyServer();
      server.start(args);
   }

   public static void stopServer() throws Exception {
      server.stop();
   }


   @Override
   protected AMQPHandshaker createHandshaker() {
      return new AMQPSampleHandshaker();
   }
}
