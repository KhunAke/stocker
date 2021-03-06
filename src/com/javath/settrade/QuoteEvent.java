package com.javath.settrade;

import java.util.Date;
import java.util.EventObject;

import com.javath.mapping.SettradeQuote;
import com.javath.mapping.SettradeQuoteId;
import com.javath.util.FilterEvent;

public class QuoteEvent extends EventObject {
	
	private static final long serialVersionUID = 1L;
	
	private final String symbol;
	private final Date date;
	private final SettradeQuote quote;
	private FilterEvent filter;
	
	public QuoteEvent(Object source) {
		this(source, null);
	}
	public QuoteEvent(Object source, SettradeQuote quote) {
		super(source);
		SettradeQuoteId id = quote.getId();
		symbol = id.getSymbol();
		date = id.getDate();
		this.quote = quote;
	}

	public String getSymbol() {
		return symbol;
	}
	public Date getDate() {
		return date;
	}
	public SettradeQuote getQuote() {
		return quote;
	}
	
	public FilterEvent appendFilter(Object filter) {
		try {
			if (!this.filter.getFilter().equals(filter))
				this.filter = new FilterEvent(filter, this.filter);
		} catch (NullPointerException e) {
			this.filter = new FilterEvent(filter, this.filter);
		}
		return getFilter();
	}
	public FilterEvent getFilter() {
		return filter;
	}
	
}
