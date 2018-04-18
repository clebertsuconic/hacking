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

import java.util.Map;

import org.apache.qpid.proton4j.amqp.Symbol;
import org.apache.qpid.proton4j.amqp.UnsignedInteger;

/**
 * @author Clebert Suconic
 */

public class Session extends Endpoint {
   private final Connection _connection;
//
//   private Map<String, SenderImpl> _senders = new LinkedHashMap<String, SenderImpl>();
//   private Map<String, ReceiverImpl>  _receivers = new LinkedHashMap<String, ReceiverImpl>();
//   private List<LinkImpl> _oldLinksToFree = new ArrayList<LinkImpl>();
//   private TransportSession _transportSession;
   private int _incomingCapacity = 0;
   private int _incomingBytes = 0;
   private int _outgoingBytes = 0;
   private int _incomingDeliveries = 0;
   private int _outgoingDeliveries = 0;
   private short channel;
   private int _outgoingWindow = Integer.MAX_VALUE;
   private int _incomingWindow = 0;
   private Map<Object, Object> _properties;
   private Map<Object, Object> _remoteProperties;
   private Symbol[] _offeredCapabilities;
   private Symbol[] _remoteOfferedCapabilities;
   private Symbol[] _desiredCapabilities;
   private Symbol[] _remoteDesiredCapabilities;
   private UnsignedInteger _handleMax;



   public Session(Connection connection)
   {
      _connection = connection;
      _connection.incref();
   }

   public short getChannel() {
      return channel;
   }

   public Session setChannel(short channel) {
      this.channel = channel;
      return this;
   }

   //
//   @Override
//   public SenderImpl sender(String name)
//   {
//      SenderImpl sender = _senders.get(name);
//      if(sender == null)
//      {
//         sender = new SenderImpl(this, name);
//         _senders.put(name, sender);
//      }
//      else
//      {
//         if(sender.getLocalState() == EndpointState.CLOSED
//            && sender.getRemoteState() == EndpointState.CLOSED)
//         {
//            _oldLinksToFree.add(sender);
//
//            sender = new SenderImpl(this, name);
//            _senders.put(name, sender);
//         }
//      }
//      return sender;
//   }
//
//   @Override
//   public ReceiverImpl receiver(String name)
//   {
//      ReceiverImpl receiver = _receivers.get(name);
//      if(receiver == null)
//      {
//         receiver = new ReceiverImpl(this, name);
//         _receivers.put(name, receiver);
//      }
//      else
//      {
//         if(receiver.getLocalState() == EndpointState.CLOSED
//            && receiver.getRemoteState() == EndpointState.CLOSED)
//         {
//            _oldLinksToFree.add(receiver);
//
//            receiver = new ReceiverImpl(this, name);
//            _receivers.put(name, receiver);
//         }
//      }
//      return receiver;
//   }
//

   public Connection getConnection()
   {
      return _connection;
   }

   @Override
   void postFinal() {
   }

   @Override
   void doFree() {
   }

   void modifyEndpoints() {
   }

   public int getIncomingCapacity()
   {
      return _incomingCapacity;
   }

   public void setIncomingCapacity(int capacity)
   {
      _incomingCapacity = capacity;
   }

   public int getIncomingBytes()
   {
      return _incomingBytes;
   }

   void incrementIncomingBytes(int delta)
   {
      _incomingBytes += delta;
   }

   public int getOutgoingBytes()
   {
      return _outgoingBytes;
   }

   void incrementOutgoingBytes(int delta)
   {
      _outgoingBytes += delta;
   }

   void incrementIncomingDeliveries(int delta)
   {
      _incomingDeliveries += delta;
   }

   int getOutgoingDeliveries()
   {
      return _outgoingDeliveries;
   }

   void incrementOutgoingDeliveries(int delta)
   {
      _outgoingDeliveries += delta;
   }

   public UnsignedInteger getHandleMax() {
      return _handleMax;
   }

   public Session setHandleMax(UnsignedInteger _handleMax) {
      this._handleMax = _handleMax;
      return this;
   }

   void localOpen()
   {
      //getConnection().getProcessor().sendSessionOpen(this);
   }

   void localClose()
   {
      //getConnection().getProcessor().sendSessionClose(this);
   }

   public int getIncomingWindow() {
      return _incomingWindow;
   }

   public Session setIncomingWindow(int _incomingWindow) {
      this._incomingWindow = _incomingWindow;
      return this;
   }

   public void setOutgoingWindow(int outgoingWindow) {
      if(outgoingWindow < 0 || outgoingWindow > 0xFFFFFFFFL)
      {
         throw new IllegalArgumentException("Value '" + outgoingWindow + "' must be in the"
                                               + " range [0 - 2^32-1]");
      }

      _outgoingWindow = outgoingWindow;
   }

   public int getOutgoingWindow()
   {
      return _outgoingWindow;
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

   public Symbol[] getDesiredCapabilities()
   {
      return _desiredCapabilities;
   }

   public void setDesiredCapabilities(Symbol[] desiredCapabilities)
   {
      _desiredCapabilities = desiredCapabilities;
   }

   public Symbol[] getRemoteDesiredCapabilities()
   {
      return _remoteDesiredCapabilities;
   }

   public void setRemoteDesiredCapabilities(Symbol[] remoteDesiredCapabilities)
   {
      _remoteDesiredCapabilities = remoteDesiredCapabilities;
   }

   public Symbol[] getOfferedCapabilities()
   {
      return _offeredCapabilities;
   }

   public void setOfferedCapabilities(Symbol[] offeredCapabilities)
   {
      _offeredCapabilities = offeredCapabilities;
   }

   public Symbol[] getRemoteOfferedCapabilities()
   {
      return _remoteOfferedCapabilities;
   }

   public void setRemoteOfferedCapabilities(Symbol[] remoteOfferedCapabilities)
   {
      _remoteOfferedCapabilities = remoteOfferedCapabilities;
   }
}
