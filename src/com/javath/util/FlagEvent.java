package com.javath.util;

import java.util.EventObject;

public class FlagEvent extends EventObject {

	private static final long serialVersionUID = 1L;
	private final Object flag;
	
	public FlagEvent(Object source, Object flag) {
		super(source);
		this.flag = flag;
	}
	
	public Object getObject() {
		return flag;
	}
	public byte getByte() {
		return (Byte) flag;
	}
	public short getShort() {
		return (Short) flag;
	}
	public int getInteger() {
		return (Integer) flag;
	}
	public long getLong() {
		return (Long) flag;
	}
	public float getFloat() {
		return (Float) flag;
	}
	public double getDouble() {
		return (Double) flag;
	}
	public String getString() {
		return (String) flag;
	}
	public char getChar() {
		return (Character) flag;
	}
	public boolean getBoolean() {
		return (Boolean) flag;
	}

}
