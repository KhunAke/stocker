package com.javath.util;

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
				length < binary.length ? length : binary.length);
	}
	
	public byte[] getByte() {
		return Arrays.copyOfRange(binary, offset, offset + length);
	}
	public byte[] getByte(int offset, int length) {
		return Arrays.copyOfRange(binary, this.offset + offset, length);
	}
	
	public short getShort() {
		return getShort(0, 2);
	}
	public short getShort(int offset) {
		return getShort(offset, 2);
	}
	public short getShort(int offset, int length) {
		Binary binary = new Binary(this, offset, length);
		try {
			String hex = binary.toHexString();
			return Short.decode(hex);
		} catch (NumberFormatException e) {
			String hex = binary.toInvertHexString();
			return (short) ((Short.decode(hex) + 1) * (-1));
		}
	}
	public int getInt() {
		return getInt(0, 4);
	}
	public int getInt(int offset) {
		return getInt(offset, 4);
	}
	public int getInt(int offset, int length) {
		Binary binary = new Binary(this, offset, length);
		try {
			String hex = binary.toHexString();
			return Integer.decode(hex);
		} catch (NumberFormatException e) {
			String hex = binary.toInvertHexString();
			return (Integer.decode(hex) + 1) * (-1);
		}
	}
	public long getLong() {
		return getLong(0, 8);
	}
	public long getLong(int offset) {
		return getLong(offset, 8);
	}
	public long getLong(int offset, int length) {
		Binary binary = new Binary(this, offset, length);
		try {
			String hex = binary.toHexString();
			return Long.decode(hex);
		} catch (NumberFormatException e) {
			String hex = binary.toInvertHexString();
			return (Long.decode(hex) + (int) 1) * (int) (-1);
		}
	}
	public float getFloat() {
		return getFloat(0, 4);
	}
	public float getFloat(int offset) {
		return getFloat(offset, 4);
	}
	public float getFloat(int offset, int length) {
		return Float.intBitsToFloat(getInt(offset, length));
	}
	public double getDouble() {
		return getDouble(0, 8);
	}
	public double getDouble(int offset) {
		return getDouble(offset, 8);
	}
	public double getDouble(int offset, int length) {
		return Double.longBitsToDouble(getLong(offset, length));
	}
	
	private String toInvertHexString() {
		StringBuffer buffer = (StringBuffer)
				Assign.borrowObject(StringBuffer.class);
		try {
			buffer.delete(0, buffer.length());
			buffer.append("0x");
			for (int index = offset; index < offset + length; index++) {
				buffer.append(String.format("%02X", (binary[index] ^ (byte) 0xFF)));
			}
			return buffer.toString();
		} finally {
			Assign.returnObject(buffer);
		}
	}
	public String toHexString() {
		StringBuffer buffer = (StringBuffer)
				Assign.borrowObject(StringBuffer.class);
		try {
			buffer.delete(0, buffer.length());
			buffer.append("0x");
			for (int index = offset; index < offset + length; index++) {
				buffer.append(String.format("%02X", binary[index]));
			}
			return buffer.toString();
		} finally {
			Assign.returnObject(buffer);
		}
		
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
	
	public static void main(String[] args) {
		//new Integer(1).
		/**/
		float f = 1.0f;
		System.out.println(Float.floatToIntBits(f));
		int i = Float.floatToIntBits(f);
		System.out.printf("%08x%n", i);
		System.out.printf("%08x%n", i);
		/**/
		
		Binary a = new Binary(new byte[] {0, 15, 63,-128,0,0});
		System.out.println(a.getInt(2,2));
		System.out.println(a.getFloat(2,2));
		
		/**
		Binary b = new Binary(a,1,4);
		Binary c = new Binary(b,1,2);
		System.out.println(a);
		System.out.println(b);
		System.out.println(c);
		System.out.println(a.getInteger());
		System.out.println(a.getInteger(2));
		System.out.println(a.getInteger(1, 1));
		**/
	}

}
