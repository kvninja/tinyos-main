// $Id: Message.java,v 1.6 2007-05-29 16:44:50 rincon Exp $

/*									tab:4
 * "Copyright (c) 2000-2003 The Regents of the University  of California.  
 * All rights reserved.
 *
 * Permission to use, copy, modify, and distribute this software and its
 * documentation for any purpose, without fee, and without written agreement is
 * hereby granted, provided that the above copyright notice, the following
 * two paragraphs and the author appear in all copies of this software.
 * 
 * IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY FOR
 * DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT
 * OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF THE UNIVERSITY OF
 * CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 * AND FITNESS FOR A PARTICULAR PURPOSE.  THE SOFTWARE PROVIDED HEREUNDER IS
 * ON AN "AS IS" BASIS, AND THE UNIVERSITY OF CALIFORNIA HAS NO OBLIGATION TO
 * PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS."
 *
 * Copyright (c) 2002-2003 Intel Corporation
 * All rights reserved.
 *
 * This file is distributed under the terms in the attached INTEL-LICENSE     
 * file. If you do not find these files, copies can be found by writing to
 * Intel Research Berkeley, 2150 Shattuck Avenue, Suite 1300, Berkeley, CA, 
 * 94704.  Attention:  Intel License Inquiry.
 */
/* Authors:  David Gay  <dgay@intel-research.net>
 *           Intel Research Berkeley Lab
 *
 */

/**
 * Message class (encode/decode tinyos messages).<p>
 *
 * The base class for encoding and decoding tinyos messages.  Provides
 * methods to read and write bit fields at an offset for a particular bit
 * length.  Intended for use by the Java code generated by mig.
 *
 * @version	1, 15 Jul 2002
 * @author	David Gay
 * @author David Gay <dgay@intel-research.net>
 * @author Intel Research Berkeley Lab
 */
package net.tinyos.message;

public class Message implements Cloneable {

  /**
   * The maximum number of characters read from an 8-bit array field being
   * converted into a Java String.
   */
  public static final int MAX_CONVERTED_STRING_LENGTH = 512;

  /**
   * The underlying byte array storing the data for this message. This is
   * private to enforce access to the data through the accessor methods in this
   * class, which do bounds checking and manage the base_offset for embedded
   * messages.
   */
  private byte[] data;

  /**
   * The base offset into the data. This allows the message data to exist at
   * some non-zero offset into the actual data.
   */
  protected int base_offset;

  /**
   * The actual length of the message data. Must be less than or equal to
   * (data.length - base_offset).
   */
  protected int data_length;

  /**
   * The AM type corresponding to this object. Set to -1 if no AM type is known.
   */
  protected int am_type;

  /** The serial packet this message originated from */
  private SerialPacket serialPacket;
  
  /** Limit no-arg instantiation. */
  protected Message() {
  }

  /**
   * Construct a new message of the given size.
   * 
   * @param data_length
   *          The size of the message to create.
   */
  public Message(int data_length) {
    init(data_length);
  }

  public void init(int data_length) {
    init(new byte[data_length]);
  }

  /**
   * Construct a new message of the given size and base offset. Allocates a new
   * byte array of size data_length+base_offset.
   * 
   * @param data_length
   *          The size of the message to create.
   * @param base_offset
   *          The base offset into the newly created message.
   */
  public Message(int data_length, int base_offset) {
    init(data_length, base_offset);
  }

  protected void init(int data_length, int base_offset) {
    init(new byte[data_length + base_offset], base_offset);
  }

  /**
   * Construct a message using data as the storage. The length of data
   * determines the length of this message.
   * 
   * @param data
   *          the storage for this message
   */
  public Message(byte[] data) {
    init(data);
  }

  protected void init(byte[] data) {
    init(data, 0);
  }

  /**
   * Construct a message using data as the storage. Use the given base_offset as
   * the base offset into the data array. The data length will be (data.length -
   * base_offset).
   * 
   * @param data
   *          the storage for this message
   * @param base_offset
   *          the base offset into the data array
   */
  public Message(byte[] data, int base_offset) {
    init(data, base_offset);
  }

  protected void init(byte[] data, int base_offset) {
    init(data, base_offset, data.length - base_offset);
  }

  /**
   * Construct a message using data as the storage. Use the given base_offset as
   * the base offset into the data array, and the specified data length.
   * 
   * @param data
   *          the storage for this message
   * @param base_offset
   *          the base offset into the data array
   * @param data_length
   *          the length of the message data
   */
  public Message(byte[] data, int base_offset, int data_length) {
    init(data, base_offset, data_length);
  }

  protected void init(byte[] data, int base_offset, int data_length) {
    this.data = data;
    this.base_offset = base_offset;
    this.data_length = data_length;
    if (base_offset + data_length > data.length)
      throw new ArrayIndexOutOfBoundsException(
          "Cannot create Message with base_offset " + base_offset
              + ", data_length " + data_length + " and data array size "
              + data.length);
  }

  /**
   * Construct an embedded message within the given 'msg'. Use the given
   * base_offset as the base offset into the data array, and the specified data
   * length.
   * 
   * @param msg
   *          the message to embed this message into
   * @param base_offset
   *          the base offset into the data array
   * @param data_length
   *          the length of the message data
   */
  public Message(Message msg, int base_offset, int data_length) {
    init(msg, base_offset, data_length);
  }

  protected void init(Message msg, int base_offset, int data_length) {
    init(msg.dataGet(), msg.base_offset + base_offset, data_length);
  }

  private Message cloneself() {
    Message copy;

    try {
      copy = (Message) super.clone();
    } catch (CloneNotSupportedException e) {
      System.err
          .println("Message: WARNING: CloneNotSupportedException in cloneself(): "
              + e);
      System.err
          .println("Message: This is a bug - please contact dgay@intel-research.net");
      copy = null;
      System.exit(2);
    }
    return copy;
  }

  /**
   * Clone this Message, including making a copy of its data
   */
  public Object clone() {
    Message copy = cloneself();
    copy.init((byte[]) data.clone(), base_offset, data_length);
    copy.am_type = this.am_type;
    return copy;
  }

  /**
   * Clone this Message, but give it a new unitialised data array of size size
   * 
   * @param size
   *          size of the new data array
   */
  public Message clone(int size) {
    Message copy = cloneself();
    copy.init(new byte[size], 0, size);
    copy.am_type = this.am_type;
    return copy;
  }

  /**
   * Copy new data for this message from 'data'. Copies min(data.length,
   * this.data_length) bytes.
   * 
   * @param data
   *          the array containing the data to be copied
   * @exception ArrayIndexOutOfBoundsException
   *              if any of data[0..getData().length - 1] are invalid
   */
  public void dataSet(byte[] data) {
    dataSet(data, 0, this.base_offset, Math.min(this.data_length, data.length));
  }

  /**
   * Copy new data for this message from offsetFrom in data to offsetTo in this
   * message. Copies a total of length bytes
   * 
   * @param data
   *          the array containing the data to be copied
   * @param offsetFrom
   *          the offset in data to start copying from
   * @param offsetTo
   *          the offset at which to start copying data into this message.
   * @param length
   *          bytes are copied.
   * @exception ArrayIndexOutOfBoundsException
   *              if any of the source or target indices are invalid
   */
  public void dataSet(byte[] data, int offsetFrom, int offsetTo, int length) {
    System.arraycopy(data, offsetFrom, this.data, offsetTo + base_offset,
        length);
  }

  /**
   * Copy new data for this message from the raw data in msg to offsetTo in this
   * message. Copies a total of msg.dataLength() bytes
   * 
   * @param msg
   *          the message containing the data to be copied
   * @param offsetTo
   *          the offset at which to start copying data into this message.
   * @exception ArrayIndexOutOfBoundsException
   *              if any of the target indices are invalid
   */
  public void dataSet(Message msg, int offsetTo) {
    System.arraycopy(msg.dataGet(), msg.baseOffset(), this.data, offsetTo
        + base_offset, msg.dataLength());
  }

  /**
   * Return the raw byte array representing the data of this message. Note that
   * only indices in the range (this.baseOffset(),
   * this.baseOffset()+this.dataLength()) are valid.
   */
  public byte[] dataGet() {
    return data;
  }

  /**
   * Return the base offset into the data array for this message.
   */
  public int baseOffset() {
    return base_offset;
  }

  /**
   * Return the length of the data (in bytes) contained in this message.
   */
  public int dataLength() {
    return data_length;
  }

  /**
   * Return the active message type of this message (-1 if unknown)
   */
  public int amType() {
    return am_type;
  }

  /**
   * Set the active message type of this message
   */
  public void amTypeSet(int type) {
    this.am_type = type;
  }

  // Check that length bits from offset are in bounds
  private void checkBounds(int offset, int length) {
    if (offset < 0 || length <= 0 || offset + length > (data_length * 8))
      throw new ArrayIndexOutOfBoundsException(
          "Message.checkBounds: bad offset (" + offset + ") or length ("
              + length + "), for data_length " + data_length + " in class "
              + this.getClass());
  }

  // Check that value is valid for a bitfield of length length
  private void checkValue(int length, long value) {
    if (length != 64 && (value < 0 || value >= 1L << length))
      throw new IllegalArgumentException("Message.checkValue: bad length ("
          + length + " or value (" + value + ")");
  }

  // Unsigned byte read
  private int ubyte(int offset) {
    int val = data[base_offset + offset];

    if (val < 0)
      return val + 256;
    else
      return val;
  }

  // ASSUMES: little endian bits & bytes for the methods without BE, and
  // big endian bits & bytes for the methods with BE

  /**
   * Read the length bit unsigned little-endian int at offset
   * 
   * @param offset
   *          bit offset where the unsigned int starts
   * @param length
   *          bit length of the unsigned int
   * @exception ArrayIndexOutOfBoundsException
   *              for invalid offset, length
   */
  protected long getUIntElement(int offset, int length) {
    checkBounds(offset, length);

    int byteOffset = offset >> 3;
    int bitOffset = offset & 7;
    int shift = 0;
    long val = 0;

    // all in one byte case
    if (length + bitOffset <= 8)
      return (ubyte(byteOffset) >> bitOffset) & ((1 << length) - 1);

    // get some high order bits
    if (bitOffset > 0) {
      val = ubyte(byteOffset) >> bitOffset;
      byteOffset++;
      shift += 8 - bitOffset;
      length -= 8 - bitOffset;
    }

    while (length >= 8) {
      val |= (long) ubyte(byteOffset++) << shift;
      shift += 8;
      length -= 8;
    }

    // data from last byte
    if (length > 0)
      val |= (long) (ubyte(byteOffset) & ((1 << length) - 1)) << shift;

    return val;
  }

  /**
   * Set the length bit unsigned little-endian int at offset to val
   * 
   * @param offset
   *          bit offset where the unsigned int starts
   * @param length
   *          bit length of the unsigned int
   * @param val
   *          value to set the bit field to
   * @exception ArrayIndexOutOfBoundsException
   *              for invalid offset, length
   * @exception IllegalArgumentException
   *              if val is an out-of-range value for this bitfield
   */
  protected void setUIntElement(int offset, int length, long val) {
    checkBounds(offset, length);
    // checkValue(length, val);

    int byteOffset = offset >> 3;
    int bitOffset = offset & 7;
    int shift = 0;

    // all in one byte case
    if (length + bitOffset <= 8) {
      data[base_offset + byteOffset] = (byte) ((ubyte(byteOffset) & ~(((1 << length) - 1) << bitOffset)) | val << bitOffset);
      return;
    }

    // set some high order bits
    if (bitOffset > 0) {
      data[base_offset + byteOffset] = (byte) ((ubyte(byteOffset) & ((1 << bitOffset) - 1)) | val << bitOffset);
      byteOffset++;
      shift += 8 - bitOffset;
      length -= 8 - bitOffset;
    }

    while (length >= 8) {
      data[base_offset + (byteOffset++)] = (byte) (val >> shift);
      shift += 8;
      length -= 8;
    }

    // data for last byte
    if (length > 0)
      data[base_offset + byteOffset] = (byte) ((ubyte(byteOffset) & ~((1 << length) - 1)) | val >> shift);
  }

  /**
   * Read the length bit signed little-endian int at offset
   * 
   * @param offset
   *          bit offset where the signed int starts
   * @param length
   *          bit length of the signed int
   * @exception ArrayIndexOutOfBoundsException
   *              for invalid offset, length
   */
  protected long getSIntElement(int offset, int length)
      throws ArrayIndexOutOfBoundsException {
    long val = getUIntElement(offset, length);

    if (length == 64)
      return val;

    if ((val & 1L << (length - 1)) != 0)
      return val - (1L << length);

    return val;
  }

  /**
   * Set the length bit signed little-endian int at offset to val
   * 
   * @param offset
   *          bit offset where the signed int starts
   * @param length
   *          bit length of the signed int
   * @param value
   *          value to set the bit field to
   * @exception ArrayIndexOutOfBoundsException
   *              for invalid offset, length
   * @exception IllegalArgumentException
   *              if val is an out-of-range value for this bitfield
   */
  protected void setSIntElement(int offset, int length, long value)
      throws ArrayIndexOutOfBoundsException {
    if (length != 64 && value >= 1L << (length - 1))
      throw new IllegalArgumentException();

    if (length != 64 && value < 0)
      value += 1L << length;

    setUIntElement(offset, length, value);
  }

  /**
   * Read the length bit unsigned big-endian int at offset
   * 
   * @param offset
   *          bit offset where the unsigned int starts. Note that these are
   *          big-endian bit offsets: bit 0 is the MSB, bit 7 the LSB.
   * @param length
   *          bit length of the unsigned int
   * @exception ArrayIndexOutOfBoundsException
   *              for invalid offset, length
   */
  protected long getUIntBEElement(int offset, int length) {
    checkBounds(offset, length);

    int byteOffset = offset >> 3;
    int bitOffset = offset & 7;
    long val = 0;

    // All in one byte case
    if (length + bitOffset <= 8)
      return (ubyte(byteOffset) >> (8 - bitOffset - length))
          & ((1 << length) - 1);

    // get some high order bits
    if (bitOffset > 0) {
      length -= 8 - bitOffset;
      val = (long) (ubyte(byteOffset) & ((1 << (8 - bitOffset)) - 1)) << length;
      byteOffset++;
    }

    while (length >= 8) {
      length -= 8;
      val |= (long) ubyte(byteOffset++) << length;
    }

    // data from last byte
    if (length > 0)
      val |= ubyte(byteOffset) >> (8 - length);

    return val;
  }

  /**
   * Set the length bit unsigned big-endian int at offset to val
   * 
   * @param offset
   *          bit offset where the unsigned int starts. Note that these are
   *          big-endian bit offsets: bit 0 is the MSB, bit 7 the LSB.
   * @param length
   *          bit length of the unsigned int
   * @param val
   *          value to set the bit field to
   * @exception ArrayIndexOutOfBoundsException
   *              for invalid offset, length
   * @exception IllegalArgumentException
   *              if val is an out-of-range value for this bitfield
   */
  protected void setUIntBEElement(int offset, int length, long val) {
    checkBounds(offset, length);
    // checkValue(length, val);

    int byteOffset = offset >> 3;
    int bitOffset = offset & 7;
    int shift = 0;

    // all in one byte case
    if (length + bitOffset <= 8) {
      int mask = ((1 << length) - 1) << (8 - bitOffset - length);

      data[base_offset + byteOffset] = (byte) ((ubyte(byteOffset) & ~mask) | val << (8 - bitOffset - length));
      return;
    }

    // set some high order bits
    if (bitOffset > 0) {
      int mask = (1 << (8 - bitOffset)) - 1;

      length -= 8 - bitOffset;
      data[base_offset + byteOffset] = (byte) (ubyte(byteOffset) & ~mask | val >> length);
      byteOffset++;
    }

    while (length >= 8) {
      length -= 8;
      data[base_offset + (byteOffset++)] = (byte) (val >> length);
    }

    // data for last byte
    if (length > 0) {
      int mask = (1 << (8 - length)) - 1;

      data[base_offset + byteOffset] = (byte) ((ubyte(byteOffset) & mask) | val << (8 - length));
    }
  }

  /**
   * Read the length bit signed big-endian int at offset
   * 
   * @param offset
   *          bit offset where the signed int starts
   * @param length
   *          bit length of the signed int
   * @exception ArrayIndexOutOfBoundsException
   *              for invalid offset, length
   */
  protected long getSIntBEElement(int offset, int length)
      throws ArrayIndexOutOfBoundsException {
    long val = getUIntBEElement(offset, length);

    if (length == 64)
      return val;

    if ((val & 1L << (length - 1)) != 0)
      return val - (1L << length);

    return val;
  }

  /**
   * Set the length bit signed big-endian int at offset to val
   * 
   * @param offset
   *          bit offset where the signed int starts
   * @param length
   *          bit length of the signed int
   * @param value
   *          value to set the bit field to
   * @exception ArrayIndexOutOfBoundsException
   *              for invalid offset, length
   * @exception IllegalArgumentException
   *              if val is an out-of-range value for this bitfield
   */
  protected void setSIntBEElement(int offset, int length, long value)
      throws ArrayIndexOutOfBoundsException {
    if (length != 64 && value >= 1L << (length - 1))
      throw new IllegalArgumentException();

    if (length != 64 && value < 0)
      value += 1L << length;

    setUIntBEElement(offset, length, value);
  }

  /**
   * Read the 32 bit IEEE float at offset
   * 
   * @param offset
   *          bit offset where the float starts
   * @param length
   *          is ignored
   * @exception ArrayIndexOutOfBoundsException
   *              for invalid offset
   */
  protected float getFloatElement(int offset, int length)
      throws ArrayIndexOutOfBoundsException {

    return Float.intBitsToFloat((int) getUIntElement(offset, 32));
  }

  /**
   * Set the 32 bit IEEE float at offset to value
   * 
   * @param offset
   *          bit offset where the float starts
   * @param length
   *          is ignored
   * @param value
   *          value to store in bitfield
   * @exception ArrayIndexOutOfBoundsException
   *              for invalid offset
   */
  protected void setFloatElement(int offset, int length, float value)
      throws ArrayIndexOutOfBoundsException {

    // using SInt because floatToRawIntBits might return a negative value
    setSIntElement(offset, 32, Float.floatToRawIntBits(value));
  }

  /**
   * 
   * @return the SerialPacket this message originated from, if it was set
   *     externally
   */
  public SerialPacket getSerialPacket() {
    return serialPacket;
  }

  /**
   * 
   * @param mySerialPacket the SerialPacket this message originated from
   */
  protected void setSerialPacket(SerialPacket mySerialPacket) {
    serialPacket = mySerialPacket;
  }
  
  
}
