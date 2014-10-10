package com.javath.set;

import com.javath.set.strategy.QuoteUpDown;
import com.javath.settrade.Quote;
import com.javath.settrade.QuoteListener;
import com.javath.util.Instance;

public abstract class Plan extends Instance implements QuoteListener {
	
	protected Broker broker;
	protected int plan_id;
	protected String symbol;
	
	public Plan(String id) {
		plan_id = Integer.valueOf(id);
		INFO("Initial Plan: %s", this);
	}
	public void setBroker(Broker broker) {
		this.broker = broker;
	}
	public void setSymbol(String symbol) {
		this.symbol = symbol;
		QuoteUpDown.getInstance(symbol)
			.addSymbolListener(symbol, this);
		Quote.getInstance().addSymbolListener(symbol, this);
	}
	
	public String toString() {
		return String.format("%s[id=%d]",
				getClassName(), plan_id);
	}
	
}
