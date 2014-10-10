package com.javath.set.strategy;

import com.javath.mapping.SettradeQuote;
import com.javath.settrade.Quote;
import com.javath.settrade.QuoteEvent;
import com.javath.settrade.QuoteFilter;

public class QuoteChangeVolume extends QuoteFilter {
	
	public static QuoteChangeVolume getInstance(String symbol) {
		QuoteChangeVolume result = (QuoteChangeVolume) instances.get(symbol);
		if (result == null) {
			result = new QuoteChangeVolume(symbol);
			instances.put(symbol, result);
		}
		return result;
	}
	
	private SettradeQuote data;
	
	private QuoteChangeVolume(String symbol) {
		super(symbol);
		Quote.getInstance().addSymbolListener(symbol, this);
	}

	@Override
	public void run(QuoteEvent event) {
		SettradeQuote quote = event.getQuote();
		try {
			if ((quote.getVolume() != null) && !quote.getVolume().equals(data.getVolume())) {
				send(event);
			} 
		} catch (NullPointerException e) {
			send(event);
		}
		data = quote;
	}
	
}
