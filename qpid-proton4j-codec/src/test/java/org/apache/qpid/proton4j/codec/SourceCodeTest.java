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

package org.apache.qpid.proton4j.codec;

import org.apache.qpid.proton4j.amqp.messaging.Source;
import org.apache.qpid.proton4j.amqp.messaging.TerminusDurability;
import org.apache.qpid.proton4j.buffer.ProtonBuffer;
import org.apache.qpid.proton4j.buffer.ProtonByteBufferAllocator;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Test for handling Source serialization
 */

public class SourceCodeTest extends CodecTestSupport {

   @Test
   public void testWriteSource() throws Exception {

      ProtonBuffer buffer = ProtonByteBufferAllocator.DEFAULT.allocate();

      Source value = new Source();
      value.setAddress("test");
      value.setDurable(TerminusDurability.UNSETTLED_STATE);

      encoder.writeObject(buffer, encoderState, value);

      final Source result = (Source)decoder.readObject(buffer, decoderState);

      assertEquals("test", result.getAddress());
      assertEquals(TerminusDurability.UNSETTLED_STATE, result.getDurable());

   }
}
