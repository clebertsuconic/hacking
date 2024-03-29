/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.qpid.proton4j.codec.encoders.messaging;

import org.apache.qpid.proton4j.amqp.Symbol;
import org.apache.qpid.proton4j.amqp.UnsignedInteger;
import org.apache.qpid.proton4j.amqp.UnsignedLong;
import org.apache.qpid.proton4j.amqp.messaging.Target;
import org.apache.qpid.proton4j.buffer.ProtonBuffer;
import org.apache.qpid.proton4j.codec.EncoderState;
import org.apache.qpid.proton4j.codec.EncodingCodes;
import org.apache.qpid.proton4j.codec.encoders.AbstractDescribedListTypeEncoder;

/**
 * Encoder of AMQP Target type values to a byte stream.
 */
public class TargetTypeEncoder extends AbstractDescribedListTypeEncoder<Target> {

    @Override
    public UnsignedLong getDescriptorCode() {
        return Target.DESCRIPTOR_CODE;
    }

    @Override
    public Symbol getDescriptorSymbol() {
        return Target.DESCRIPTOR_SYMBOL;
    }

    @Override
    public Class<Target> getTypeClass() {
        return Target.class;
    }

    @Override
    public void writeElement(Target target, int index, ProtonBuffer buffer, EncoderState state) {
        switch (index) {
            case 0:
                state.getEncoder().writeString(buffer, state, target.getAddress());
                break;
            case 1:
                state.getEncoder().writeUnsignedInteger(buffer, state, target.getDurable().getValue());
                break;
            case 2:
                state.getEncoder().writeObject(buffer, state, target.getExpiryPolicy().getPolicy());
                break;
            case 3:
                state.getEncoder().writeUnsignedInteger(buffer, state, target.getTimeout());
                break;
            case 4:
                state.getEncoder().writeBoolean(buffer, state, target.getDynamic());
                break;
            case 5:
                state.getEncoder().writeMap(buffer, state, target.getDynamicNodeProperties());
                break;
            case 6:
                state.getEncoder().writeArray(buffer, state, target.getCapabilities());
                break;
            default:
                throw new IllegalArgumentException("Unknown Target value index: " + index);
        }
    }

    @Override
    public int getListEncoding(Target value) {
        return EncodingCodes.LIST32;
    }

    @Override
    public int getElementCount(Target target) {
        if (target.getCapabilities() != null) {
            return 7;
        } else if (target.getDynamicNodeProperties() != null) {
            return 6;
        } else if (target.getDynamic()) {
            return 5;
        } else if (target.getTimeout() != null && !target.getTimeout().equals(UnsignedInteger.ZERO)) {
            return 4;
        } else if (target.getExpiryPolicy() != null) {
            return 3;
        } else if (target.getDurable() != null) {
            return 2;
        } else if (target.getAddress() != null) {
            return 1;
        } else {
            return 0;
        }
    }
}
