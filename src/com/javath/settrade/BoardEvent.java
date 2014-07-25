package com.javath.settrade;

import java.util.Date;
import java.util.EventObject;

import com.javath.util.DateTime;

public class BoardEvent extends EventObject {
	
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
	private final String[][] rows;
	
	public BoardEvent(Object source) {
		this(source, new Date(), null);
	}
	
	public BoardEvent(Object source, Date date, String[][] rows) {
		super(source);
		this.date = date;
		this.rows = rows;
	}
	
	public Date getDate() {
		return date ;
	}
	
	public String[][] getRows() {
		return rows;
	}
	
	public String toString() {
		return String.format("%s[date=%s]", 
				this.getClass().getCanonicalName(), DateTime.timestamp(date));
	}

}
