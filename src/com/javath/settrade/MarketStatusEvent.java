package com.javath.settrade;

import java.util.Date;
import java.util.EventObject;

import com.javath.util.DateTime;

public class MarketStatusEvent extends EventObject {
	
	private static final long serialVersionUID = 1L;
	
	private final Date date;
	private final MarketStatus status;
	
	public MarketStatusEvent(Object source) {
		this(source, new Date(), MarketStatus.Unknow);
	}
	
	public MarketStatusEvent(Object source, Date date, MarketStatus status) {
		super(source);
		this.date = date;
		this.status = status;
	}
	
	public Date getDate() {
		return date ;
	}
	
	public MarketStatus getStatus() {
		return status ;
	}
	
	public String toString() {
		return String.format("%s[date=%s,status=%s]", 
				this.getClass().getCanonicalName(), DateTime.timestamp(date), status);
	}
}
