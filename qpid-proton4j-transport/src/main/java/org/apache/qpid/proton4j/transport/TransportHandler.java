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
package org.apache.qpid.proton4j.transport;

import org.apache.qpid.proton4j.amqp.transport.AMQPHeader;

/**
 * Listen for events generated from the Transport
 */
public interface TransportHandler {

    // TODO Define events.  transportRead(Object), transportWrite(Object) ?

    // Some things that might flow through a transport pipeline

    void handleAMQPHeader(TransportHandlerContext context, AMQPHeader header);

    void handleSaslPerformative(TransportHandlerContext context, SaslFrame frame);

    void handleProtocolFrame(TransportHandlerContext context, ProtocolFrame frame);

    void handlePartialFrame(TransportHandlerContext context, PartialFrame frame);

    void transportEncodingError(TransportHandlerContext context, Throwable e);

    void transportDecodingError(TransportHandlerContext context, Throwable e);

    void transportFailed(TransportHandlerContext context, Throwable e);

    // Should we convey events to the outside that we aren't accepting more input ?

    void transportReadable(TransportHandlerContext context);

    // Should we convey events to the outside that we aren't allowing output ?

    void transportWritable(TransportHandlerContext context);

}