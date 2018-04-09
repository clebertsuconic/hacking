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

package org.apache.qpid.proton4j.netty.buffer;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.qpid.proton4j.buffer.ProtonBuffer;
import org.apache.qpid.proton4j.buffer.ProtonByteBuffer;

/**
 * @author Clebert Suconic
 */

public class NettyProtonBuffer implements ProtonBuffer {

   ByteBuf buffer;

   public NettyProtonBuffer(ByteBuf buffer) {
      this.buffer = buffer;
   }

   public NettyProtonBuffer() {
   }

   public ByteBuf getBuffer() {
      return buffer;
   }

   public NettyProtonBuffer setBuffer(ByteBuf buffer) {
      this.buffer = buffer;
      return this;
   }

   @Override
   public boolean hasArray() {
      return false;
   }

   @Override
   public byte[] getArray() {
      byte[] bytes = new byte[buffer.readableBytes()];
      buffer.readBytes(bytes);
      return bytes;
   }

   @Override
   public int getArrayOffset() {
      return 0;
   }

   @Override
   public int capacity() {
      return buffer.capacity();
   }

   @Override
   public ProtonBuffer capacity(int newCapacity) {
      buffer.capacity(newCapacity);
      return this;
   }

   @Override
   public int maxCapacity() {
      return buffer.maxCapacity();
   }

   @Override
   public ProtonBuffer duplicate() {
      return new NettyProtonBuffer(buffer.duplicate());
   }

   @Override
   public ProtonBuffer copy() {
      return new NettyProtonBuffer(buffer.copy());
   }

   @Override
   public ProtonBuffer copy(int index, int length) {
      return new NettyProtonBuffer(buffer.copy(index, length));
   }

   @Override
   public ProtonBuffer clear() {
      buffer.clear();
      return this;
   }

   @Override
   public ByteBuffer toByteBuffer() {
      return buffer.nioBuffer();
   }

   @Override
   public String toString(Charset charset) {
      return buffer.toString(charset);
   }

   @Override
   public int getReadableBytes() {
      return buffer.readableBytes();
   }

   @Override
   public int getWritableBytes() {
      return buffer.writableBytes();
   }

   @Override
   public int getReadIndex() {
      return buffer.readerIndex();
   }

   @Override
   public ProtonBuffer setReadIndex(int value) {
      buffer.readerIndex(value);
      return this;
   }

   @Override
   public int getWriteIndex() {
      return buffer.writerIndex();
   }

   @Override
   public ProtonBuffer setWriteIndex(int value) {
      buffer.writerIndex(value);
      return this;
   }

   @Override
   public ProtonBuffer setIndex(int readIndex, int writeIndex) {
      buffer.setIndex(readIndex, writeIndex);
      return this;
   }

   @Override
   public ProtonBuffer markReadIndex() {
      buffer.markReaderIndex();
      return this;
   }

   @Override
   public ProtonBuffer resetReadIndex() {
      buffer.resetReaderIndex();
      return this;
   }

   @Override
   public ProtonBuffer markWriteIndex() {
      buffer.markReaderIndex();
      return this;
   }

   @Override
   public ProtonBuffer resetWriteIndex() {
      buffer.resetWriterIndex();
      return this;
   }

   @Override
   public boolean isReadable() {
      return buffer.isReadable();
   }

   @Override
   public boolean isReadable(int size) {
      return buffer.isReadable(size);
   }

   @Override
   public boolean getBoolean(int index) {
      return buffer.getBoolean(index);
   }

   @Override
   public byte getByte(int index) {
      return buffer.getByte(index);
   }

   @Override
   public short getUnsignedByte(int index) {
      return buffer.getUnsignedByte(index);
   }

   @Override
   public char getChar(int index) {
      return buffer.getChar(index);
   }

   @Override
   public short getShort(int index) {
      return buffer.getShort(index);
   }

   @Override
   public int getUnsignedShort(int index) {
      return buffer.getUnsignedShort(index);
   }

   @Override
   public int getInt(int index) {
      return buffer.getInt(index);
   }

   @Override
   public long getUnsignedInt(int index) {
      return buffer.getUnsignedInt(index);
   }

   @Override
   public long getLong(int index) {
      return buffer.getLong(index);
   }

   @Override
   public float getFloat(int index) {
      return buffer.getFloat(index);
   }

   @Override
   public double getDouble(int index) {
      return buffer.getDouble(index);
   }

   @Override
   public ProtonBuffer getBytes(int index, ProtonBuffer dst) {
      if (dst instanceof NettyProtonBuffer) {
         ByteBuf other = getNettyBuffer(dst);
         buffer.getBytes(index, other);
         return this;
      } else {
         throw new IllegalStateException("TODO");
      }
   }

   private ByteBuf getNettyBuffer(ProtonBuffer dst) {
      if (dst instanceof NettyProtonBuffer) {
         return ((NettyProtonBuffer) dst).getBuffer();
      } else  if (dst.hasArray()){
         ByteBuf theBuffer = Unpooled.wrappedBuffer(dst.getArray());
         theBuffer.setIndex(dst.getReadIndex(), dst.getWriteIndex());
         return theBuffer;
      } else {
         return null;
      }
   }

   @Override
   public ProtonBuffer getBytes(int index, ProtonBuffer dst, int length) {
      if (dst instanceof NettyProtonBuffer) {
         ByteBuf other = getNettyBuffer(dst);
         buffer.getBytes(index, other, length);
         return this;
      } else {
         throw new IllegalStateException("TODO");
      }
   }

   @Override
   public ProtonBuffer getBytes(int index, ProtonBuffer dst, int dstIndex, int length) {
      if (dst instanceof NettyProtonBuffer) {
         ByteBuf other = getNettyBuffer(dst);
         buffer.getBytes(index, other, dstIndex, length);
         return this;
      } else {
         throw new IllegalStateException("TODO");
      }
   }

   @Override
   public ProtonBuffer getBytes(int index, byte[] dst) {
      buffer.getBytes(index, dst);
      return this;
   }

   @Override
   public ProtonBuffer getBytes(int index, byte[] target, int offset, int length) {
      buffer.getBytes(index, target, offset, length);
      return this;
   }

   @Override
   public ProtonBuffer getBytes(int index, ByteBuffer destination) {
      buffer.getBytes(index, destination);
      return this;
   }

   @Override
   public ProtonBuffer setByte(int index, int value) {
      buffer.setByte(index, value);
      return this;
   }

   @Override
   public ProtonBuffer setBoolean(int index, boolean value) {
      buffer.setBoolean(index, value);
      return this;
   }

   @Override
   public ProtonBuffer setChar(int index, int value) {
      buffer.setChar(index, value);
      return this;
   }

   @Override
   public ProtonBuffer setShort(int index, int value) {
      buffer.setShort(index, value);
      return this;
   }

   @Override
   public ProtonBuffer setInt(int index, int value) {
      buffer.setInt(index, value);
      return this;
   }

   @Override
   public ProtonBuffer setLong(int index, long value) {
      buffer.setLong(index, value);
      return this;
   }

   @Override
   public ProtonBuffer setFloat(int index, float value) {
      buffer.setFloat(index, value);
      return this;
   }

   @Override
   public ProtonBuffer setDouble(int index, double value) {
      buffer.setDouble(index, value);
      return this;
   }

   @Override
   public ProtonBuffer setBytes(int index, ProtonBuffer source) {
      ByteBuf nettysource = getNettyBuffer(source);
      buffer.setBytes(index, nettysource);
      return this;
   }

   @Override
   public ProtonBuffer setBytes(int index, ProtonBuffer source, int length) {
      ByteBuf nettysource = getNettyBuffer(source);
      buffer.setBytes(index, nettysource, length);
      return this;
   }

   @Override
   public ProtonBuffer setBytes(int index, ProtonBuffer source, int sourceIndex, int length) {
      ByteBuf nettysource = getNettyBuffer(source);
      buffer.setBytes(index, nettysource, sourceIndex, length);
      return this;
   }

   @Override
   public ProtonBuffer setBytes(int index, byte[] source) {
      buffer.setBytes(index, source);
      return this;
   }

   @Override
   public ProtonBuffer setBytes(int index, byte[] src, int srcIndex, int length) {
      buffer.setBytes(index, src, srcIndex, length);
      return this;
   }

   @Override
   public ProtonBuffer setBytes(int index, ByteBuffer src) {
      buffer.setBytes(index, src);
      return this;
   }

   @Override
   public ProtonBuffer skipBytes(int length) {
      buffer.skipBytes(length);
      return this;
   }

   @Override
   public byte readByte() {
      return buffer.readByte();
   }

   @Override
   public ProtonBuffer readBytes(byte[] target) {
      buffer.readBytes(target);
      return this;
   }

   @Override
   public ProtonBuffer readBytes(byte[] target, int length) {
      buffer.readBytes(target, 0, length);
      return this;
   }

   @Override
   public ProtonBuffer readBytes(byte[] target, int offset, int length) {
      buffer.readBytes(target, offset, length);
      return this;
   }

   @Override
   public ProtonBuffer readBytes(ProtonBuffer target) {
      ByteBuf targetBuffer = getNettyBuffer(target);
      buffer.readBytes(targetBuffer);
      fixUp(target, targetBuffer);
      return this;
   }

   /** used to fix the writerIndex when we cross into other types of Buffers */
   private void fixUp(ProtonBuffer target, ByteBuf targetBuffer) {
      if (!(target instanceof NettyProtonBuffer)) {
         target.setWriteIndex(targetBuffer.writerIndex());
      }
   }

   @Override
   public ProtonBuffer readBytes(ProtonBuffer target, int length) {
      buffer.readBytes(getNettyBuffer(target), length);
      target.setWriteIndex(length);
      return this;
   }

   @Override
   public ProtonBuffer readBytes(ProtonBuffer target, int offset, int length) {
      buffer.readBytes(getNettyBuffer(target), offset, length);
      target.setWriteIndex(offset + length);
      return this;
   }

   @Override
   public ProtonBuffer readBytes(ByteBuffer dst) {
      buffer.readBytes(dst);
      return this;
   }

   @Override
   public boolean readBoolean() {
      return buffer.readBoolean();
   }

   @Override
   public short readShort() {
      return buffer.readShort();
   }

   @Override
   public int readInt() {
      return buffer.readInt();
   }

   @Override
   public long readLong() {
      return buffer.readLong();
   }

   @Override
   public float readFloat() {
      return buffer.readFloat();
   }

   @Override
   public double readDouble() {
      return buffer.readDouble();
   }

   @Override
   public boolean isWritable() {
      return buffer.isWritable();
   }

   @Override
   public boolean isWritable(int size) {
      return buffer.isWritable(size);
   }

   @Override
   public ProtonBuffer writeByte(int value) {
      buffer.writeByte(value);
      return this;
   }

   @Override
   public ProtonBuffer writeBytes(byte[] value) {
      buffer.writeBytes(value);
      return this;
   }

   @Override
   public ProtonBuffer writeBytes(byte[] value, int length) {
      buffer.writeBytes(value, 0, length);
      return this;
   }

   @Override
   public ProtonBuffer writeBytes(byte[] value, int offset, int length) {
      buffer.writeBytes(value, offset, length);
      return this;
   }

   @Override
   public ProtonBuffer writeBytes(ProtonBuffer src) {
      buffer.writeBytes(getNettyBuffer(src));
      return this;
   }

   @Override
   public ProtonBuffer writeBytes(ProtonBuffer src, int length) {
      buffer.writeBytes(getNettyBuffer(src), length);
      return this;
   }

   @Override
   public ProtonBuffer writeBytes(ProtonBuffer src, int srcIndex, int length) {
      buffer.writeBytes(getNettyBuffer(src), srcIndex, length);
      return this;
   }

   @Override
   public ProtonBuffer writeBytes(ByteBuffer source) {
      buffer.writeBytes(source);
      return this;
   }

   @Override
   public ProtonBuffer writeBoolean(boolean value) {
      buffer.writeBoolean(value);
      return this;
   }

   @Override
   public ProtonBuffer writeShort(short value) {
      buffer.writeShort(value);
      return this;
   }

   @Override
   public ProtonBuffer writeInt(int value) {
      buffer.writeInt(value);
      return this;
   }

   @Override
   public ProtonBuffer writeLong(long value) {
      buffer.writeLong(value);
      return this;
   }

   @Override
   public ProtonBuffer writeFloat(float value) {
      buffer.writeFloat(value);
      return this;
   }

   @Override
   public ProtonBuffer writeDouble(double value) {
      buffer.writeDouble(value);
      return this;
   }

   @Override
   public int compareTo(ProtonBuffer o) {
      return buffer.compareTo(getNettyBuffer(o));
   }
}
