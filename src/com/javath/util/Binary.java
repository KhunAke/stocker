package com.javath.util;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;

public class Binary {
	
	private final byte[] binary;
	private final int offset;
	private final int length;
	
	private Binary(byte[] binary, int offset, int length) {
		this.binary = binary;
		this.offset = offset;
		this.length = length;
	}
	public Binary(byte[] binary) {
		this(binary, 0 , binary.length);
	}
	public Binary(Binary binary, int offset, int length) {
		this(binary.binary, binary.offset + offset, 
				(offset + length) < binary.length ? length : binary.length - offset);
	}
	
	public byte[] getByte() {
		return Arrays.copyOfRange(binary, offset, offset + length);
	}
	public byte[] getByte(int offset, int length) {
		return Arrays.copyOfRange(binary, this.offset + offset, length);
	}
	public byte getByte(int index) {
		if (index < length)
			return binary[offset + index];
		else
			throw new ObjectException("Index \"%d\" out of range", index);
	}
	
	public short getShort() {
		return getShort(0, 2, false);
	}
	public short getShort(boolean little_endian) {
		return getShort(0, 2, little_endian);
	}
	public short getShort(int offset) {
		return getShort(offset, 2, false);
	}
	public short getShort(int offset, boolean little_endian) {
		return getShort(offset, 2, little_endian);
	}
	public short getShort(int offset, int length) {
		return getShort(offset, length, false);
	}
	public short getShort(int offset, int length, boolean little_endian) {
		final int limit = Short.SIZE / 8;
		if (length > limit)
			throw new ObjectException("Length not support %d byte", length);
		Binary binary = new Binary(this, offset, length);
		try {
			String hex = binary.toHexString(little_endian);
			return Short.decode(hex);
		} catch (NumberFormatException e) { 
			String hex = binary.toInvertHexString(little_endian);
			return (short) ((Short.decode(hex) + 1) * (-1));
		}
	}
	public int getInt() {
		return getInt(0, 4, false);
	}
	public int getInt(boolean little_endian) {
		return getInt(0, 4, little_endian);
	}
	public int getInt(int offset) {
		return getInt(offset, 4, false);
	}
	public int getInt(int offset, boolean little_endian) {
		return getInt(offset, 4, little_endian);
	}
	public int getInt(int offset, int length) {
		return getInt(offset, length, false);
	}
	public int getInt(int offset, int length, boolean little_endian) {
		final int limit = Integer.SIZE / 8;
		if (length > limit)
			throw new ObjectException("Length not support %d byte", length);
		Binary binary = new Binary(this, offset, length);
		try {
			String hex = binary.toHexString(little_endian);
			return Integer.decode(hex);
		} catch (NumberFormatException e) {
			String hex = binary.toInvertHexString(little_endian);
			return (Integer.decode(hex) + 1) * (-1);
		}
	}
	public long getLong() {
		return getLong(0, 8, false);
	}
	public long getLong(boolean little_endian) {
		return getLong(0, 8, little_endian);
	}
	public long getLong(int offset) {
		return getLong(offset, 8, false);
	}
	public long getLong(int offset, boolean little_endian) {
		return getLong(offset, 8, little_endian);
	}
	public long getLong(int offset, int length) {
		return getLong(offset, length, false);
	}
	public long getLong(int offset, int length, boolean little_endian) {
		final int limit = Long.SIZE / 8;
		if (length > limit)
			throw new ObjectException("Length not support %d byte", length);
		Binary binary = new Binary(this, offset, length);
		try {
			String hex = binary.toHexString(little_endian);
			return Long.decode(hex);
		} catch (NumberFormatException e) {
			String hex = binary.toInvertHexString(little_endian);
			return (Long.decode(hex) + (int) 1) * (int) (-1);
		}
	}
	public float getFloat() {
		return getFloat(0, 4, false);
	}
	public float getFloat(boolean little_endian) {
		return getFloat(0, 4, little_endian);
	}
	public float getFloat(int offset) {
		return getFloat(offset, 4, false);
	}
	public float getFloat(int offset, boolean little_endian) {
		return getFloat(offset, 4, little_endian);
	}
	public float getFloat(int offset, int length) {
		return getFloat(offset, length, false);
	}
	public float getFloat(int offset, int length, boolean little_endian) {
		return Float.intBitsToFloat(getInt(offset, length, little_endian));
	}
	public double getDouble() {
		return getDouble(0, 8, false);
	}
	public double getDouble(boolean little_endian) {
		return getDouble(0, 8, little_endian);
	}
	public double getDouble(int offset) {
		return getDouble(offset, 8, false);
	}
	public double getDouble(int offset, boolean little_endian) {
		return getDouble(offset, 8, little_endian);
	}
	public double getDouble(int offset, int length) {
		return getDouble(offset, length, false);
	}
	public double getDouble(int offset, int length, boolean little_endian) {
		return Double.longBitsToDouble(getLong(offset, length, little_endian));
	}
	public String getString() {
		return getString(offset, length, Charset.defaultCharset().displayName());
	}
	public String getString(String charset) {
		return getString(offset, length, charset);
	}
	public String getString(int offset) {
		return getString(offset, length, Charset.defaultCharset().displayName());
	}
	public String getString(int offset, String charset) {
		return getString(offset, length, charset);
	}
	public String getString(int offset, int length) {
		return getString(offset, length, Charset.defaultCharset().displayName());
	}
	public String getString(int offset, int length, String charset) {
		ByteBuffer bb = ByteBuffer.wrap(binary, offset, 
				(offset + length) < binary.length ? length : binary.length - offset);
		CharBuffer buffer = Charset.forName(charset).decode(bb);
		return buffer.toString();
	}
	
	private String toInvertHexString(boolean little_endian) {
		StringBuffer buffer = (StringBuffer)
				Assign.borrowObject(StringBuffer.class);
		try {
			buffer.delete(0, buffer.length());
			buffer.append("0x");
			if (little_endian)
				for (int index = (offset + length - 1); index >= offset; index--)
					buffer.append(String.format("%02X", (byte) (binary[index] ^ 0xFF)));
			else
				for (int index = offset; index < offset + length; index++)
					buffer.append(String.format("%02X", (byte) (binary[index] ^ 0xFF)));
			return buffer.toString();
		} finally {
			Assign.returnObject(buffer);
		}
	}
	public String toHexString() {
		return toHexString(false);
	}
	public String toHexString(boolean little_endian) {
		StringBuffer buffer = (StringBuffer)
				Assign.borrowObject(StringBuffer.class);
		try {
			buffer.delete(0, buffer.length());
			buffer.append("0x");
			if (little_endian)
				for (int index = (offset + length - 1); index >= offset; index--)
					buffer.append(String.format("%02X", binary[index]));
			else
				for (int index = offset; index < offset + length; index++)
					buffer.append(String.format("%02X", binary[index]));
			return buffer.toString();
		} finally {
			Assign.returnObject(buffer);
		}
	}
	
	public int length() {
		return length;
	}
	
 	public String toString() {
		StringBuffer buffer = (StringBuffer)
				Assign.borrowObject(StringBuffer.class);
		buffer.delete(0, buffer.length());
		buffer.append("[");
		for (int index = offset; index < offset + length; index++) {
			buffer.append(String.format("%02X",binary[index]));
			buffer.append(", ");
		}
		buffer.replace(buffer.length() - 2, buffer.length(), "]");
		return buffer.toString();
	}

}
