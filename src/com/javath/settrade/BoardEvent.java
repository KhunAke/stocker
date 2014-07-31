package com.javath.settrade;

import java.util.Date;
import java.util.EventObject;

import com.javath.util.DateTime;

public class BoardEvent extends EventObject {
	
	private static final long serialVersionUID = 1L;
	
	private final Date date;
	private final String[][] data_set;
	
	public BoardEvent(Object source) {
		this(source, new Date(), null);
	}
	
	public BoardEvent(Object source, Date date, String[][] data_set) {
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
