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

import java.util.Arrays;

import org.apache.qpid.proton4j.amqp.transport.DeliveryState;

public class DeliveryImpl
{
    public static final int DEFAULT_MESSAGE_FORMAT = 0;

    private DeliveryImpl _linkPrevious;
    private DeliveryImpl _linkNext;

    private DeliveryImpl _workNext;
    private DeliveryImpl _workPrev;
    boolean _work;

    private DeliveryImpl _transportWorkNext;
    private DeliveryImpl _transportWorkPrev;
    boolean _transportWork;

    private Record _attachments;
    private Object _context;

    private final byte[] _tag;
    private final LinkImpl _link;
    private DeliveryState _deliveryState;
    private boolean _settled;
    private boolean _remoteSettled;
    private DeliveryState _remoteDeliveryState;
    private DeliveryState _defaultDeliveryState = null;
    private int _messageFormat = DEFAULT_MESSAGE_FORMAT;

    /**
     * A bit-mask representing the outstanding work on this delivery received from the transport layer
     * that has not yet been processed by the application.
     */
    private int _flags = (byte) 0;

//    private TransportDelivery _transportDelivery;
    private byte[] _data;
    private int _dataSize;
    private boolean _complete;
    private boolean _updated;
    private boolean _done;
    private int _offset;

    DeliveryImpl(final byte[] tag, final LinkImpl link, DeliveryImpl previous)
    {
        _tag = tag;
        _link = link;
        _link.incrementUnsettled();
        _linkPrevious = previous;
        if(previous != null)
        {
            previous._linkNext = this;
        }
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("DeliveryImpl [_tag=").append(Arrays.toString(_tag))
            .append(", _link=").append(_link)
            .append(", _deliveryState=").append(_deliveryState)
            .append(", _settled=").append(_settled)
            .append(", _remoteSettled=").append(_remoteSettled)
            .append(", _remoteDeliveryState=").append(_remoteDeliveryState)
            .append(", _flags=").append(_flags)
            .append(", _defaultDeliveryState=").append(_defaultDeliveryState)
            //.append(", _transportDelivery=").append(_transportDelivery)
            .append(", _dataSize=").append(_dataSize)
            .append(", _complete=").append(_complete)
            .append(", _updated=").append(_updated)
            .append(", _done=").append(_done)
            .append(", _offset=").append(_offset).append("]");
        return builder.toString();
    }

}
