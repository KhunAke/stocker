package com.javath.set.strategy;

import com.javath.set.Broker;
import com.javath.set.Plan;
import com.javath.settrade.Quote;
import com.javath.settrade.QuoteEvent;

public class DSM extends Plan {

	public DSM(String id) {
		super(id);
	}
	public DSM initPlan(Broker broker, String symbol) {
		setBroker(broker);
		setSymbol(symbol);
		//QuoteFilterChangeVolume.getInstance(symbol)
		//	.addSymbolListener(symbol, this);
		return this;
	}
	
	@Override
	public void action(QuoteEvent event) {
		try {
			System.out.printf("%s,%.2f,%d%n", event.getSymbol(), event.getQuote().getLast(), event.getFilter().getInteger("trend"));
		} catch (NullPointerException e) {
			System.out.printf("%s,%.2f%n", event.getSymbol(), event.getQuote().getLast());
		}
		try {
			event.getFilter().printStackFilter();
		} catch (NullPointerException e) {}
	}

}
