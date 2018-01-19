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
package org.apache.qpid.proton4j.transport.sasl;

import org.apache.qpid.proton4j.codec.Decoder;
import org.apache.qpid.proton4j.codec.Encoder;

/**
 * Interface for a strategy type which manages how the transport deals
 * with incoming SASL AMQP Headers and SASL based mechanisms
 */
public interface SaslHandler {

    boolean isDone();

    void setSaslListener(SaslListener listener);

    SaslListener getSaslListener();

    Encoder getSaslEndoer();

    void setSaslEncoder(Encoder encoder);

    Decoder getSaslDecoder();

    void setSaslDecoder(Decoder decoder);

    void client();

    void server();

}