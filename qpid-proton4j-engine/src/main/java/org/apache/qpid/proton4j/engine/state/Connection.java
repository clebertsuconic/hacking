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

package org.apache.qpid.proton4j.engine.state;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.qpid.proton4j.amqp.Symbol;
import org.apache.qpid.proton4j.amqp.UnsignedInteger;
import org.apache.qpid.proton4j.amqp.UnsignedShort;
import org.apache.qpid.proton4j.amqp.transport.Close;
import org.apache.qpid.proton4j.engine.Transport;

/**
 * @author Clebert Suconic
 */

public class Connection extends Endpoint {
   private final Transport transport;

   public static final UnsignedShort MAX_CHANNELS = UnsignedShort.MAX_VALUE;

   private Set<Session> _sessions = new HashSet<>();
   private UnsignedShort _maxChannels = MAX_CHANNELS;
   private UnsignedInteger remoteIdleTimeout;
   private UnsignedInteger localIdleTimeout;

   private String _localContainerId = "";
   private String _localHostname;
   private String _remoteContainer;
   private String _remoteHostname;
   private Symbol[] _offeredCapabilities;
   private Symbol[] _desiredCapabilities;
   private Symbol[] _remoteOfferedCapabilities;
   private Symbol[] _remoteDesiredCapabilities;
   private Map<Object, Object> _properties;
   private Map<Object, Object> _remoteProperties;

   private Object _context;

   private static final Symbol[] EMPTY_SYMBOL_ARRAY = new Symbol[0];

   public Connection(Transport transport)
   {
      if (transport == null) {
         throw new NullPointerException("transport == null");
      }
      this.transport = transport;
   }

   public Session newSession()
   {
      Session session = new Session(this, transport);
      _sessions.add(session);

      return session;
   }

   public Transport getTransport() {
      return transport;
   }

   void freeSession(Session session)
   {
      _sessions.remove(session);
   }

   public void handleClose(Close close) {
      setRemoteState(EndpointState.CLOSED);
   }

   public UnsignedInteger getRemoteIdleTimeout() {
      return remoteIdleTimeout;
   }

   public void setRemoteIdleTimeout(UnsignedInteger idleTimeout) {
      this.remoteIdleTimeout = idleTimeout;
   }

   public UnsignedInteger getLocalIdleTimeout() {
      return localIdleTimeout;
   }

   public Connection setLocalIdleTimeout(UnsignedInteger localIdleTimeout) {
      this.localIdleTimeout = localIdleTimeout;
      return this;
   }

   public UnsignedShort getMaxChannels()
   {
      return _maxChannels;
   }

   public String getLocalContainerId()
   {
      return _localContainerId;
   }

   public void setLocalContainerId(String localContainerId)
   {
      _localContainerId = localContainerId;
   }

   public void setContainer(String container)
   {
      _localContainerId = container;
   }

   public String getContainer()
   {
      return _localContainerId;
   }

   public void setHostname(String hostname)
   {
      _localHostname = hostname;
   }

   public String getRemoteContainer()
   {
      return _remoteContainer;
   }

   public String getRemoteHostname()
   {
      return _remoteHostname;
   }

   public void setOfferedCapabilities(Symbol[] capabilities)
   {
      _offeredCapabilities = capabilities;
   }

   public void setDesiredCapabilities(Symbol[] capabilities)
   {
      _desiredCapabilities = capabilities;
   }

   public Symbol[] getRemoteOfferedCapabilities()
   {
      return _remoteOfferedCapabilities == null ? EMPTY_SYMBOL_ARRAY : _remoteOfferedCapabilities;
   }

   public Symbol[] getRemoteDesiredCapabilities()
   {
      return _remoteDesiredCapabilities == null ? EMPTY_SYMBOL_ARRAY : _remoteDesiredCapabilities;
   }


   public Symbol[] getOfferedCapabilities()
   {
      return _offeredCapabilities;
   }

   public Symbol[] getDesiredCapabilities()
   {
      return _desiredCapabilities;
   }

   public void setRemoteOfferedCapabilities(Symbol[] remoteOfferedCapabilities)
   {
      _remoteOfferedCapabilities = remoteOfferedCapabilities;
   }

   public void setRemoteDesiredCapabilities(Symbol[] remoteDesiredCapabilities)
   {
      _remoteDesiredCapabilities = remoteDesiredCapabilities;
   }


   public Map<Object, Object> getProperties()
   {
      return _properties;
   }

   public void setProperties(Map<Object, Object> properties)
   {
      _properties = properties;
   }

   public Map<Object, Object> getRemoteProperties()
   {
      return _remoteProperties;
   }

   public void setRemoteProperties(Map<Object, Object> remoteProperties)
   {
      _remoteProperties = remoteProperties;
   }

   public String getHostname()
   {
      return _localHostname;
   }

   public void setRemoteContainer(String remoteContainerId)
   {
      _remoteContainer = remoteContainerId;
   }

   public void setRemoteHostname(String remoteHostname)
   {
      _remoteHostname = remoteHostname;
   }

   public Object getContext()
   {
      return _context;
   }

   public void setContext(Object context)
   {
      _context = context;
   }

   public void localOpen()
   {
   }

   public void localClose()
   {

   }

   @Override
   public void postFinal() {

   }

   @Override
   public void doFree() {

   }
}

