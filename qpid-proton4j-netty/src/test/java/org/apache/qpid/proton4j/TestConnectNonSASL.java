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

package org.apache.qpid.proton4j;

import javax.jms.Connection;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;

import org.apache.qpid.jms.JmsConnectionFactory;
import org.apache.qpid.proton4j.sample.NettyServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Clebert Suconic
 */

public class TestConnectNonSASL {

   @Before
   public void startServer() throws Exception {
      NettyServer.startServer();
   }

   @After
   public void after() throws Exception {
      NettyServer.stopServer();
   }

   @Test
   public void testConnect() throws Exception {
      JmsConnectionFactory factory = new JmsConnectionFactory("amqp://localhost:5672?amqp.saslLayer=false");

      Connection connection = factory.createConnection();
      Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
      Queue queue = session.createQueue("test");
      // MessageProducer producer = session.createProducer(queue);
      session.close();
      connection.close();

   }
}
