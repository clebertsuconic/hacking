/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.apache.qpid.proton4j.engine.state;

import java.util.EnumSet;
import java.util.Map;

import org.apache.qpid.proton4j.amqp.*;
import org.apache.qpid.proton4j.amqp.messaging.Source;
import org.apache.qpid.proton4j.amqp.messaging.Target;
import org.apache.qpid.proton4j.amqp.transport.ReceiverSettleMode;
import org.apache.qpid.proton4j.amqp.transport.Role;
import org.apache.qpid.proton4j.amqp.transport.SenderSettleMode;

public class LinkImpl extends Endpoint {


    private final Session _session;

    DeliveryImpl _head;
    DeliveryImpl _tail;
    DeliveryImpl _current;
    private String _name;
    private Source _source;
    private Source _remoteSource;
    private Target _target;
    private Target _remoteTarget;
    private int _queued;
    private int _credit;
    private int _unsettled;
    private int _drained;
    private UnsignedLong _maxMessageSize;
    private UnsignedLong _remoteMaxMessageSize;

    private SenderSettleMode _senderSettleMode;
    private SenderSettleMode _remoteSenderSettleMode;
    private ReceiverSettleMode _receiverSettleMode;
    private ReceiverSettleMode _remoteReceiverSettleMode;


    private boolean _drain;
    private boolean _detached;
    private Map<Object, Object> _properties;
    private Map<Object, Object> _remoteProperties;
    private Symbol[] _offeredCapabilities;
    private Symbol[] _remoteOfferedCapabilities;
    private Symbol[] _desiredCapabilities;
    private Symbol[] _remoteDesiredCapabilities;

    private final Role role;

    public LinkImpl(Role role, Session session, String name)
    {
        this.role = role;
        _session = session;
        _session.incref();
        _name = name;
        Connection conn = session.getConnection();
    }

    public Role getRole() {
        return role;
    }

    public String getName()
    {
        return _name;
    }

//
//    public DeliveryImpl delivery(byte[] tag)
//    {
//        return delivery(tag, 0, tag.length);
//    }

    
//    public DeliveryImpl delivery(byte[] tag, int offset, int length)
//    {
//        if (offset != 0 || length != tag.length)
//        {
//            throw new IllegalArgumentException("At present delivery tag must be the whole byte array");
//        }
//        incrementQueued();
//        try
//        {
//            DeliveryImpl delivery = new DeliveryImpl(tag, this, _tail);
//            if (_tail == null)
//            {
//                _head = delivery;
//            }
//            _tail = delivery;
//            if (_current == null)
//            {
//                _current = delivery;
//            }
//            getConnectionImpl().workUpdate(delivery);
//            return delivery;
//        }
//        catch (RuntimeException e)
//        {
//            e.printStackTrace();
//            throw e;
//        }
//    }
//
//
//    void postFinal() {
//        _session.getConnectionImpl().put(Event.Type.LINK_FINAL, this);
//        _session.decref();
//    }
//
//
//    void doFree()
//    {
//        DeliveryImpl dlv = _head;
//        while (dlv != null) {
//            DeliveryImpl next = dlv.next();
//            dlv.free();
//            dlv = next;
//        }
//
//        _session.getConnectionImpl().removeLinkEndpoint(_node);
//        _node = null;
//    }
//
//    void modifyEndpoints() {
//        modified();
//    }
//
//    /*
//     * Called when settling a message to ensure that the head/tail refs of the link are updated.
//     * The caller ensures the delivery updates its own refs appropriately.
//     */
//    void remove(DeliveryImpl delivery)
//    {
//        if(_head == delivery)
//        {
//            _head = delivery.getLinkNext();
//        }
//        if(_tail == delivery)
//        {
//            _tail = delivery.getLinkPrevious();
//        }
//    }
//
//
//    public DeliveryImpl current()
//    {
//        return _current;
//    }
//
//
//    public boolean advance()
//    {
//        if(_current != null )
//        {
//            DeliveryImpl oldCurrent = _current;
//            _current = _current.getLinkNext();
//            getConnectionImpl().workUpdate(oldCurrent);
//
//            if(_current != null)
//            {
//                getConnectionImpl().workUpdate(_current);
//            }
//            return true;
//        }
//        else
//        {
//            return false;
//        }
//
//    }

    
    protected Connection getConnectionImpl()
    {
        return _session.getConnection();
    }

    
    public Session getSession()
    {
        return _session;
    }

    
    public Source getRemoteSource()
    {
        return _remoteSource;
    }

    public void setRemoteSource(Source source)
    {
        _remoteSource = source;
    }

    
    public Target getRemoteTarget()
    {
        return _remoteTarget;
    }

    public void setRemoteTarget(Target target)
    {
        _remoteTarget = target;
    }

    
    public Source getSource()
    {
        return _source;
    }

    
    public void setSource(Source source)
    {
        // TODO - should be an error if local state is ACTIVE
        _source = source;
    }

    
    public Target getTarget()
    {
        return _target;
    }

    
    public void setTarget(Target target)
    {
        // TODO - should be an error if local state is ACTIVE
        _target = target;
    }


    public int getCredit()
    {
        return _credit;
    }

    public void addCredit(int credit)
    {
        _credit+=credit;
    }

    public void setCredit(int credit)
    {
        _credit = credit;
    }

    boolean hasCredit()
    {
        return _credit > 0;
    }

    void incrementCredit()
    {
        _credit++;
    }

    void decrementCredit()
    {
        _credit--;
    }

    
    public int getQueued()
    {
        return _queued;
    }

    void incrementQueued()
    {
        _queued++;
    }

    void decrementQueued()
    {
        _queued--;
    }

    
    public int getUnsettled()
    {
        return _unsettled;
    }

    void incrementUnsettled()
    {
        _unsettled++;
    }

    void decrementUnsettled()
    {
        _unsettled--;
    }

    void setDrain(boolean drain)
    {
        _drain = drain;
    }

    
    public boolean getDrain()
    {
        return _drain;
    }

    
    public SenderSettleMode getSenderSettleMode()
    {
        return _senderSettleMode;
    }

    
    public void setSenderSettleMode(SenderSettleMode senderSettleMode)
    {
        _senderSettleMode = senderSettleMode;
    }

    
    public SenderSettleMode getRemoteSenderSettleMode()
    {
        return _remoteSenderSettleMode;
    }

    
    public void setRemoteSenderSettleMode(SenderSettleMode remoteSenderSettleMode)
    {
        _remoteSenderSettleMode = remoteSenderSettleMode;
    }

    
    public ReceiverSettleMode getReceiverSettleMode()
    {
        return _receiverSettleMode;
    }

    
    public void setReceiverSettleMode(ReceiverSettleMode receiverSettleMode)
    {
        _receiverSettleMode = receiverSettleMode;
    }

    
    public ReceiverSettleMode getRemoteReceiverSettleMode()
    {
        return _remoteReceiverSettleMode;
    }

    public void setRemoteReceiverSettleMode(ReceiverSettleMode remoteReceiverSettleMode)
    {
        _remoteReceiverSettleMode = remoteReceiverSettleMode;
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

    
    public UnsignedLong getMaxMessageSize()
    {
        return _maxMessageSize;
    }

    
    public void setMaxMessageSize(UnsignedLong maxMessageSize)
    {
        _maxMessageSize = maxMessageSize;
    }

    
    public UnsignedLong getRemoteMaxMessageSize()
    {
        return _remoteMaxMessageSize;
    }

    public void setRemoteMaxMessageSize(UnsignedLong remoteMaxMessageSize)
    {
        _remoteMaxMessageSize = remoteMaxMessageSize;
    }

    
    /* public int drained()
    {
        int drained = 0;

        if (this instanceof SenderImpl) {
            if(getDrain() && hasCredit())
            {
                _drained = getCredit();
                setCredit(0);
                modified();
                drained = _drained;
            }
        } else {
            drained = _drained;
            _drained = 0;
        }

        return drained;
    }

    int getDrained()
    {
        return _drained;
    }

    void setDrained(int value)
    {
        _drained = value;
    } */

    
    public DeliveryImpl head()
    {
        return _head;
    }

    @Override
    public void localClose() {
    }

    @Override
    public void localOpen()
    {
    }

   /*
    void localClose()
    {
        getConnectionImpl().put(Event.Type.LINK_LOCAL_CLOSE, this);
    }

    
    public void detach()
    {
        _detached = true;
        getConnectionImpl().put(Event.Type.LINK_LOCAL_DETACH, this);
        modified();
    } */

    public boolean detached()
    {
        return _detached;
    }
}
