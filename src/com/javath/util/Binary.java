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
	
	public int getInteger() {
		return getInteger(0, 4);
	}
	public int getInteger(int offset) {
		return getInteger(offset, 4);
	}
	public int getInteger(int offset, int length) {
		return 0;
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
		Binary a = new Binary(new byte[] {0, 15, 31, 63, 127, -1});
		Binary b = new Binary(a,1,4);
		Binary c = new Binary(b,1,2);
		System.out.println(a);
		System.out.println(b);
		System.out.println(c);
	}

}
