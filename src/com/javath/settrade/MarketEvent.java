package com.javath.settrade;

import java.util.Date;
import java.util.EventObject;

import com.javath.util.DateTime;

public class MarketEvent extends EventObject {

	private static final long serialVersionUID = 1L;
	public final static int NAME = 0;
	public final static int LAST = 1;
	public final static int CHANGE = 2;
	public final static int HIGH = 3;
	public final static int LOW = 4;
	public final static int VOLUME = 5;
	public final static int VALUE = 6;
	
	private final Date date;
	private final MarketStatus status;
	private final String[][] data_set;

	public MarketEvent(Object source) {
		this(source, new Date(), MarketStatus.Unknow , null);
	}
	
	public MarketEvent(Object source, Date date, MarketStatus status, String[][] data_set) {
		super(source);
		this.date = date;
		this.status = status;
		this.data_set = data_set;
	}
	
	public Date getDate() {
		return date ;
	}
	
	public MarketStatus getStatus() {
		return status ;
	}
	
	public String[][] getDataSet() {
		return data_set;
	}
	
	public String toString() {
		return String.format("%s[date=\"%s\"]", 
				this.getClass().getCanonicalName(), DateTime.string(date));
	}
	
}
