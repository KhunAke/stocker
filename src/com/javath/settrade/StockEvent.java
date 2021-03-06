package com.javath.settrade;

import java.util.Date;
import java.util.EventObject;

import com.javath.util.DateTime;

public class StockEvent extends EventObject {
	
	private static final long serialVersionUID = 1L;
	public final static int SYMBOL = 0;
	public final static int OPEN = 1;
	public final static int HIGH = 2;
	public final static int LOW = 3;
	public final static int LAST = 4;
	public final static int CHANGE = 5;
	public final static int BID = 6;
	public final static int OFFER = 7;
	public final static int VOLUME = 8;
	public final static int VALUE = 9;
	
	private final Date date;
	private final String[][] data_set;
	
	public StockEvent(Object source) {
		this(source, new Date(), null);
	}
	
	public StockEvent(Object source, Date date, String[][] data_set) {
		super(source);
		this.date = date;
		this.data_set = data_set;
	}
	
	public Date getDate() {
		return date ;
	}
	
	public String[][] getDataSet() {
		return data_set;
	}
	
	public String toString() {
		return String.format("%s[date=%s]", 
				this.getClass().getCanonicalName(), DateTime.timestamp(date));
	}

}
