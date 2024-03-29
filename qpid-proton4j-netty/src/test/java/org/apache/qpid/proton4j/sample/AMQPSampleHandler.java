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

import io.netty.channel.Channel;
import org.apache.qpid.proton4j.amqp.UnsignedInteger;
import org.apache.qpid.proton4j.amqp.UnsignedShort;
import org.apache.qpid.proton4j.amqp.transport.Begin;
import org.apache.qpid.proton4j.amqp.transport.Close;
import org.apache.qpid.proton4j.amqp.transport.End;
import org.apache.qpid.proton4j.amqp.transport.Open;
import org.apache.qpid.proton4j.engine.state.Connection;
import org.apache.qpid.proton4j.engine.state.EndpointState;
import org.apache.qpid.proton4j.engine.state.Session;
import org.apache.qpid.proton4j.netty.AMQPHandler;

/**
 * @author Clebert Suconic
 */

public class AMQPSampleHandler extends AMQPHandler {

   public AMQPSampleHandler(Channel channel, int maxFrameSize) {
      super(channel, maxFrameSize);
   }

   @Override
   public void connectionOpened(Open open, Connection connection) {
      connection.setContainer(connection.getRemoteContainer());
      connection.setOfferedCapabilities(connection.getRemoteDesiredCapabilities());
      connection.setHostname("localhost");
      connection.setLocalIdleTimeout(open.getIdleTimeOut());
      connection.setProperties(open.getProperties());

      // TODO:
      // At the moment open is issuing Processor.sendOpen..
      // If it was up to me, we would always call send directly.
      // but I will need a discussion with the qpid team
      connection.open();

      super.connectionOpened(open, connection);
   }

   @Override
   protected void sessionEnded(End end, short channel, Session session) {
      super.sessionEnded(end, channel, session);
   }

   @Override
   protected void connectionClosed(Close close, Connection connection) {
      connection.close();
      super.connectionClosed(close, connection);
   }

   @Override
   protected void sessionBegin(Begin begin, Session session) {
      session.setLocalState(EndpointState.ACTIVE);
      session.setProperties(begin.getProperties());
      session.setDesiredCapabilities(begin.getDesiredCapabilities());
      session.setOfferedCapabilities(begin.getOfferedCapabilities());
      begin.setIncomingWindow(new UnsignedInteger(session.getIncomingWindow()));
      begin.setRemoteChannel(new UnsignedShort(session.getChannel()));
      super.sessionBegin(begin, session);
   }
}
