package com.javath.stock;

import java.util.Date;
import java.util.EventObject;

import com.javath.util.DateTime;

public class SymbolEvent extends EventObject {

	private static final long serialVersionUID = 1L;
	
	private final String name;
	private final Date date;
	private final short decimal_price;
	private final long open;
	private final long high;
	private final long low;
	private final long last;
	private final long bid;
	private final long offer;
	private final short decimal_volume;
	private final long volume;
	private final short decimal_value;
	private final long value;
	

	public SymbolEvent(Object source) {
		this(source, null, null, (short) 0, 0, 0, 0, 0, 0, 0, (short) 0, 0, (short) 0, 0);
	}
	
	public SymbolEvent(Object source,String name, Date date, 
			short decimal_price, long open, long high, long low, long last, long bid, long offer, 
			short decimal_volume, long volume, 
			short decimal_value, long value) {
		super(source);
		this.name = name;
		this.date = date;
		this.decimal_price = decimal_price;
		this.open = open;
		this.high = high;
		this.low = low;
		this.last = last;
		this.bid = bid;
		this.offer = offer;
		this.decimal_volume = decimal_volume;
		this.volume = volume;
		this.decimal_value = decimal_value;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public Date getDate() {
		return date;
	}

	public long getOpen() {
		return open;
	}

	public long getHigh() {
		return high;
	}

	public long getLow() {
		return low;
	}

	public long getLast() {
		return last;
	}
	
	public long getBid() {
		return bid;
	}

	public long getOffer() {
		return offer;
	}

	public long getVolume() {
		return volume;
	}

	public long getValue() {
		return value;
	}
	
	public String toString() {
		return String.format("%s[name=%s, date=%s]", 
				this.getClass().getCanonicalName(), name, DateTime.timestamp(date));
	}
	
	public double price(long variable) {
		return (double) variable / decimal_price;
	}
	
	public double volume(long variable) {
		return (double) variable / decimal_volume;
	}
	
	public double value(long variable) {
		return (double) variable / decimal_value;
	}
	
}
