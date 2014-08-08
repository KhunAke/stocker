package com.javath.settrade;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.javath.mapping.SettradeQuote;
import com.javath.util.DateTime;

public class QuoteFilterChangeLastScreen implements QuoteListener {
	
	private final static Map<String,QuoteFilterChangeLastScreen> instances;
	
	static {
		instances = new HashMap<String,QuoteFilterChangeLastScreen>();
	}
	
	public static QuoteFilterChangeLastScreen getInstance(String symbol) {
		QuoteFilterChangeLastScreen result = instances.get(symbol);
		if (result == null) {
			result = new QuoteFilterChangeLastScreen(symbol);
			instances.put(symbol, result);
		}
		return result;
	}
	
	private QuoteFilterChangeLastScreen(String symbol) {
		QuoteFilterChangeLast.getInstance(symbol).addSymbolListener(symbol, this);
	}

	@Override
	public void action(QuoteEvent event) {
		SettradeQuote quote = event.getQuote();
		System.out.printf("%s: %s, \"%s\", [%s], %s, %s, %s%n", 
				DateTime.timestamp(new Date()), event.getSymbol(), DateTime.string(event.getDate()), 
				quote.getLast(), quote.getChangePrior(), quote.getVolume(), quote.getValue());
	}
	

}
