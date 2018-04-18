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

package org.apache.qpid.proton4j;

import java.io.IOException;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.util.internal.StringUtil;
import org.apache.qpid.proton4j.amqp.Symbol;
import org.apache.qpid.proton4j.amqp.transport.Open;
import org.apache.qpid.proton4j.buffer.ProtonBuffer;
import org.apache.qpid.proton4j.buffer.ProtonByteBuffer;
import org.apache.qpid.proton4j.buffer.ProtonByteBufferAllocator;
import org.apache.qpid.proton4j.netty.AMQPHandler;
import org.apache.qpid.proton4j.netty.buffer.NettyProtonBuffer;
import org.apache.qpid.proton4j.sample.AMQPSampleHandler;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Clebert Suconic
 */

public class NettyBufferTests {

   static String openStatement = "0000012602000000005310d0000001160000000aa12949443a65316261383739312d316535342d343237622d396337622d3361613939316235613238653a31a1096c6f63616c686f73747000100000607fff7000007530404040e04d04a31d736f6c652d636f6e6e656374696f6e2d666f722d636f6e7461696e65721044454c415945445f44454c49564552590f414e4f4e594d4f55532d52454c41590b5348415245442d53554253c17b06a30770726f64756374a107517069644a4d53a30776657273696f6ea106302e33302e30a308706c6174666f726da14b4a564d3a20312e382e305f37332c2032352e37332d6230322c204f7261636c6520436f72706f726174696f6e2c204f533a204d6163204f5320582c2031302e31322e362c207838365f3634";
   static String responseStatement = "000000b702000000005310c0aa0aa1096c6f63616c686f73744070ffffffff60ffff70000075304040e04d04a31d736f6c652d636f6e6e656374696f6e2d666f722d636f6e7461696e65721044454c415945445f44454c49564552590b5348415245442d535542530f414e4f4e594d4f55532d52454c415940c13c04a30770726f64756374a1176170616368652d6163746976656d712d617274656d6973a30776657273696f6ea10e322e362e302d534e415053484f54";
   static String newResponse =       "0000004d00020000005310d00000003d00000005a12949443a37343335363936392d303966342d343963322d383362342d3332663234343731303566653a314070ffffffff60ffff7000007530";


   static final byte[] openFrame = StringUtil.decodeHexDump(openStatement);
   static final byte[] responseFrame = StringUtil.decodeHexDump(responseStatement);
   static final byte[] newResponseFrame = StringUtil.decodeHexDump(newResponse);


   @Test
   public void testInterpretOpenNettyBuffer() throws Exception {
      ProtonBuffer buffer = new NettyProtonBuffer(Unpooled.wrappedBuffer(openFrame));
      testInterpretOpen(buffer);
   }

   @Test
   public void testInterpretOpenProtonByteBuffer() throws Exception {
      ProtonBuffer buffer = new ProtonByteBuffer(openFrame);
      testInterpretOpen(buffer);
   }

   @Test
   public void testPrintOutput() throws Exception {
      ProtonBuffer buffer = new ProtonByteBuffer(responseFrame);
      buffer.setReadIndex(8);
      Open performative = (Open) AMQPHandler.readPerformative(buffer);
      System.out.println("Response would been " + performative);


      buffer = new ProtonByteBuffer(newResponseFrame);
      buffer.setReadIndex(8);
      Open newPerformative = (Open) AMQPHandler.readPerformative(buffer);
      System.out.println("New Response would been " + newPerformative);
   }


   @Test
   public void testWriteOpen() throws Exception {



   }

   @Test
   public void testInterpretOpen() throws Exception {
      ProtonBuffer buffer = new ProtonByteBuffer(openFrame);
      testInterpretOpen(buffer);
   }

   private void testInterpretOpen(ProtonBuffer buffer) throws IOException {
      buffer.setReadIndex(8);
      Open performative = (Open) AMQPHandler.readPerformative(buffer);

      Assert.assertEquals(4, performative.getDesiredCapabilities().length);

      Assert.assertEquals(Symbol.getSymbol("sole-connection-for-container"), performative.getDesiredCapabilities()[0]);
      Assert.assertEquals(Symbol.getSymbol("DELAYED_DELIVERY"), performative.getDesiredCapabilities()[1]);
      Assert.assertEquals(Symbol.getSymbol("ANONYMOUS-RELAY"), performative.getDesiredCapabilities()[2]);
      Assert.assertEquals(Symbol.getSymbol("SHARED-SUBS"), performative.getDesiredCapabilities()[3]);

      System.out.println("Performative::" + performative);


      ProtonBuffer writeBuffer1 = new ProtonByteBuffer(new byte[1024]);
      writeBuffer1.clear();

      AMQPHandler.writePerformative(writeBuffer1, (byte)5, (short)33, performative);
      AMQPHandler handler = new AMQPSampleHandler(null, Integer.MAX_VALUE);
      handler.readFrame(writeBuffer1);
      // this will validate we can actually read stuff
      performative = (Open)handler.getCurrentPerformative();


      Assert.assertEquals(Symbol.getSymbol("sole-connection-for-container"), performative.getDesiredCapabilities()[0]);
      Assert.assertEquals(Symbol.getSymbol("DELAYED_DELIVERY"), performative.getDesiredCapabilities()[1]);
      Assert.assertEquals(Symbol.getSymbol("ANONYMOUS-RELAY"), performative.getDesiredCapabilities()[2]);
      Assert.assertEquals(Symbol.getSymbol("SHARED-SUBS"), performative.getDesiredCapabilities()[3]);


   }

   @Test
   public void testCopyIntoBuffer() {
      byte[] test = new byte[] {1,2,3,4,5,6};
      NettyProtonBuffer nettyBuffer = new NettyProtonBuffer(Unpooled.buffer(1024));
      nettyBuffer.writeBytes(test);

      ProtonBuffer dst = ProtonByteBufferAllocator.DEFAULT.allocate(test.length);
      System.out.println(dst.capacity());
      nettyBuffer.readBytes(dst);


      byte[] read = new byte[test.length];


      dst.readBytes(read);


      for (int i = 0; i < test.length; i++) {
         Assert.assertEquals(test[i], read[i]);
      }
   }

}
