package com.javath.settrade;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.javath.mapping.SettradeQuote;
import com.javath.util.DateTime;

public class QuoteScreen implements QuoteListener {
	
	private final static Map<String,QuoteScreen> instances;
	
	static {
		instances = new HashMap<String,QuoteScreen>();
	}
	
	public static QuoteScreen getInstance(String symbol) {
		QuoteScreen result = instances.get(symbol);
		if (result == null) {
			result = new QuoteScreen(symbol);
			instances.put(symbol, result);
		}
		return result;
	}
	
	private QuoteScreen(String symbol) {
		Quote.getInstance().addSymbolListener(symbol, this);
	}
	
	@Override
	public void action(QuoteEvent event) {
		SettradeQuote quote = event.getQuote();
		System.out.printf("%s: %s, \"%s\", %s, %s, %s, %s%n", 
				DateTime.timestamp(new Date()), event.getSymbol(), DateTime.string(event.getDate()), 
				quote.getLast(), quote.getChangePrior(), quote.getVolume(), quote.getValue());
	}

}
