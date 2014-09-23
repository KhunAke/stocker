package com.javath.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class DataSet implements Iterator<Object[]> {
	
	private static final Object[] empty = new Object[] {};
	
	private Object[] data;
	
	public DataSet(List<?> data) {
		this(data.toArray(empty));
	}
	public DataSet(Object[] data) {
		this.data = data;
		current = -1;
	}
	
	public int size() {
		return data.length;
	}
	public Object[] get(int index) {
		return (Object[]) data[index];
	}
	
	private int current;
	public  Iterator<Object[]> iterator() {
		current = -1;
		return this;
	}
	public Object[] seek(int index) {
		Object[] result =  (Object[]) data[index];
		current = index;
		return result;
	}
	@Override
	public boolean hasNext() {
		return current >= data.length - 1 ? false : true;
	}
	@Override
	public Object[] next() {
		current += 1;
		return (Object[]) data[current];
	}
	@Override
	public void remove() {
		throw new ObjectException("class does not implement interface member"); 
	}
	
	public int fields() {
		return ((Object[]) data[current]).length;
	}
	public byte fieldByte(int column) {
		return (Byte) ((Object[]) data[current])[column];
	}
	public short fieldShort(int column) {
		return (Short) ((Object[]) data[current])[column];
	}
	public int fieldInteger(int column) {
		return (Integer) ((Object[]) data[current])[column];
	}
	public long fieldLong(int column) {
		return (Long) ((Object[]) data[current])[column];
	}
	public float fieldFloat(int column) {
		return (Float) ((Object[]) data[current])[column];
	}
	public double fieldDouble(int column) {
		return (Double) ((Object[]) data[current])[column];
	}
	public String fieldString(int column) {
		return (String) ((Object[]) data[current])[column];
	}
	public char fieldChar(int column) {
		return (Character) ((Object[]) data[current])[column];
	}
	public boolean fieldBoolean(int column) {
		return (Boolean) ((Object[]) data[current])[column];
	}
	public Object fieldObject(int column) {
		return ((Object[]) data[current])[column];
	}
	
	private Map<String,Integer> map_header;
	public void setHeader(String header) {
		try {
			map_header.clear();
		} catch (NullPointerException e) {
			map_header = new HashMap<String, Integer>();
		}
		String[] headers = header.split(",");
		for (int index = 0; index < headers.length; index++) {
			map_header.put(headers[index], index);
		}
	}
	public int column(String name) {
		try {
			return map_header.get(name);
		} catch (NullPointerException e) {
			throw new ObjectException("Header not initiated.");
		}
	}

}
