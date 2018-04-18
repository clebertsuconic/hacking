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
import org.apache.qpid.proton4j.amqp.transport.Open;
import org.apache.qpid.proton4j.engine.EmptyProcessor;
import org.apache.qpid.proton4j.engine.Processor;

/**
 * @author Clebert Suconic
 */

public class Connection extends Endpoint {
   private final Processor processor;

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

   public Connection(Processor processor)
   {
      if (processor == null) {
         throw new NullPointerException("processor == null");
      }
      this.processor = processor;
   }

   public Connection()
   {
      this.processor = EmptyProcessor.get();
   }

   public Session newSession()
   {
      Session session = new Session(this);
      _sessions.add(session);

      return session;
   }

   public Processor getProcessor() {
      return processor;
   }

   void freeSession(Session session)
   {
      _sessions.remove(session);
   }

   public void handleClose(Close close) {
      setRemoteState(EndpointState.CLOSED);
   }

   public void handleOpen(Open open)
   {
      setRemoteState(EndpointState.ACTIVE);
      setRemoteHostname(open.getHostname());
      setRemoteContainer(open.getContainerId());
      setRemoteDesiredCapabilities(open.getDesiredCapabilities());
      setRemoteOfferedCapabilities(open.getOfferedCapabilities());
      setRemoteProperties(open.getProperties());
      setRemoteIdleTimeout(open.getIdleTimeOut());
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


   Symbol[] getOfferedCapabilities()
   {
      return _offeredCapabilities;
   }

   Symbol[] getDesiredCapabilities()
   {
      return _desiredCapabilities;
   }

   void setRemoteOfferedCapabilities(Symbol[] remoteOfferedCapabilities)
   {
      _remoteOfferedCapabilities = remoteOfferedCapabilities;
   }

   void setRemoteDesiredCapabilities(Symbol[] remoteDesiredCapabilities)
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

   void setRemoteProperties(Map<Object, Object> remoteProperties)
   {
      _remoteProperties = remoteProperties;
   }

   public String getHostname()
   {
      return _localHostname;
   }

   void setRemoteContainer(String remoteContainerId)
   {
      _remoteContainer = remoteContainerId;
   }

   void setRemoteHostname(String remoteHostname)
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

   void localOpen()
   {
   }

   void localClose()
   {

   }

   @Override
   void postFinal() {

   }

   @Override
   void doFree() {

   }
}

