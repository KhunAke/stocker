package com.javath.settrade;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.javath.mapping.SettradeQuote;
import com.javath.util.DateTime;

public class QuoteFilterChangeVolumeScreen implements QuoteListener {
	
	private final static Map<String,QuoteFilterChangeVolumeScreen> instances;
	
	static {
		instances = new HashMap<String,QuoteFilterChangeVolumeScreen>();
	}
	
	public static QuoteFilterChangeVolumeScreen getInstance(String symbol) {
		QuoteFilterChangeVolumeScreen result = instances.get(symbol);
		if (result == null) {
			result = new QuoteFilterChangeVolumeScreen(symbol);
			instances.put(symbol, result);
		}
		return result;
	}
	
	private QuoteFilterChangeVolumeScreen(String symbol) {
		QuoteFilterChangeVolume.getInstance(symbol).addSymbolListener(symbol, this);
	}

	@Override
	public void action(QuoteEvent event) {
		SettradeQuote quote = event.getQuote();
		System.out.printf("%s: %s, \"%s\", %s, %s, [%s], %s%n", 
				DateTime.timestamp(new Date()), event.getSymbol(), DateTime.string(event.getDate()), 
				quote.getLast(), quote.getChangePrior(), quote.getVolume(), quote.getValue());
	}
	
}
