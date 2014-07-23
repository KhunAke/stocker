package com.javath.settrade;

import java.util.Date;
import java.util.EventObject;

import com.javath.util.DateTime;

public class MarketEvent extends EventObject {

	private static final long serialVersionUID = 1L;
	public final static int NAME = 0;
	public final static int LAST = 1;
	public final static int HIGH = 2;
	public final static int LOW = 3;
	public final static int VOLUME = 4;
	public final static int VALUE = 5;
	
	private final Date date;
	private final MarketStatus status;
	private final String[][] rows;

	public MarketEvent(Object source) {
		this(source, new Date(), MarketStatus.Unknow , null);
	}
	
	public MarketEvent(Object source, Date date, MarketStatus status, String[][] rows) {
		super(source);
		this.date = date;
		this.status = status;
		this.rows = rows;
	}
	
	public Date getDate() {
		return date ;
	}
	
	public MarketStatus getStatus() {
		return status ;
	}
	
	public String[][] getRows() {
		return rows;
	}
	
	public String toString() {
		return String.format("%s[date=%s]", 
				this.getClass().getCanonicalName(), DateTime.timestamp(date));
	}
	
}
