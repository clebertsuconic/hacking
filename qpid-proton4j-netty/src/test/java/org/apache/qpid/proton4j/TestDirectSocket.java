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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.util.internal.StringUtil;
import org.apache.qpid.proton4j.amqp.Symbol;
import org.apache.qpid.proton4j.amqp.transport.Open;
import org.apache.qpid.proton4j.buffer.ProtonBuffer;
import org.apache.qpid.proton4j.buffer.ProtonByteBuffer;
import org.apache.qpid.proton4j.netty.AMQPHandler;
import org.apache.qpid.proton4j.netty.AMQPHandshaker;
import org.apache.qpid.proton4j.netty.buffer.NettyProtonBuffer;
import org.apache.qpid.proton4j.sample.AMQPSampleHandshaker;
import org.apache.qpid.proton4j.sample.NettyServer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.apache.qpid.proton4j.netty.AMQPHandshaker.handShakeBare;

/**
 * @author Clebert Suconic
 */

public class TestDirectSocket {

   static String openStatement = "0000012602000000005310d0000001160000000aa12949443a65316261383739312d316535342d343237622d396337622d3361613939316235613238653a31a1096c6f63616c686f73747000100000607fff7000007530404040e04d04a31d736f6c652d636f6e6e656374696f6e2d666f722d636f6e7461696e65721044454c415945445f44454c49564552590f414e4f4e594d4f55532d52454c41590b5348415245442d53554253c17b06a30770726f64756374a107517069644a4d53a30776657273696f6ea106302e33302e30a308706c6174666f726da14b4a564d3a20312e382e305f37332c2032352e37332d6230322c204f7261636c6520436f72706f726174696f6e2c204f533a204d6163204f5320582c2031302e31322e362c207838365f3634";
   static String responseStatement = "000000b702000000005310c0aa0aa1096c6f63616c686f73744070ffffffff60ffff70000075304040e04d04a31d736f6c652d636f6e6e656374696f6e2d666f722d636f6e7461696e65721044454c415945445f44454c49564552590b5348415245442d535542530f414e4f4e594d4f55532d52454c415940c13c04a30770726f64756374a1176170616368652d6163746976656d712d617274656d6973a30776657273696f6ea10e322e362e302d534e415053484f54";


   static final byte[] openFrame = StringUtil.decodeHexDump(openStatement);
   static final byte[] responseFrame = StringUtil.decodeHexDump(responseStatement);

   @Before
   public void startServer() throws Exception {
      NettyServer.startServer();
   }

   @After
   public void after() throws Exception {
      NettyServer.stopServer();
   }

   /**
    * it will open a connection send the handshake BARE and then open a session. Assert conditions
    */
   @Test
   public void testDirectSocket() throws Exception {

      Socket socket = new Socket("127.0.0.1", 5672);

      OutputStream writeSocket = socket.getOutputStream();
      writeSocket.write(handShakeBare);

      byte[] readbuffer = new byte[handShakeBare.length];

      InputStream inputStream = socket.getInputStream();
      Assert.assertEquals(handShakeBare.length, inputStream.read(readbuffer));

      for (int i = 0; i < handShakeBare.length; i++) {
         Assert.assertEquals(handShakeBare[i], readbuffer[i]);
      }

      writeSocket.write(openFrame);

      readbuffer = new byte[openFrame.length];
      // this is temporary, it will be changed as we use proper framing
      Assert.assertEquals(readbuffer.length, inputStream.read(readbuffer));

      for (int i = 0; i < openFrame.length; i++) {
         Assert.assertEquals(openFrame[i], readbuffer[i]);
      }

      socket.close();
   }

   /**
    * it will open a connection send the handshake BARE and then open a session. Assert conditions
    */
   @Test
   public void testWriteOpen() throws Exception {

      Open open = new Open();

      ProtonBuffer protonBuffer = new ProtonByteBuffer(new byte[1024]);

   }

   @Test
   public void testDirectSocket2() throws Exception {

      Socket socket = new Socket("127.0.0.1", 5672);

      OutputStream writeSocket = socket.getOutputStream();
      writeSocket.write(handShakeBare);

      byte[] readbuffer = new byte[handShakeBare.length];

      InputStream inputStream = socket.getInputStream();
      Assert.assertEquals(handShakeBare.length, inputStream.read(readbuffer));

      for (int i = 0; i < handShakeBare.length; i++) {
         Assert.assertEquals(handShakeBare[i], readbuffer[i]);
      }

      writeSocket.write(openFrame);

      readbuffer = new byte[openFrame.length];
      // this is temporary, it will be changed as we use proper framing
      Assert.assertEquals(readbuffer.length, inputStream.read(readbuffer));

      for (int i = 0; i < openFrame.length; i++) {
         Assert.assertEquals(openFrame[i], readbuffer[i]);
      }

      socket.close();
   }

}
