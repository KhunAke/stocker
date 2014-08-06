package com.javath.settrade;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class QuoteScreen implements QuoteListener {
	
	private final static Map<String,QuoteScreen> instances;
	
	static {
		instances = new HashMap<String,QuoteScreen>();
	}
	
	public static QuoteScreen getInstance(String symbol) {
		
		return instance;
	}
	
	private QuoteScreen(String symbol) {
		Quote.getInstance().addSymbolListener(symbol, this);
	}
	
	@Override
	public void action(QuoteEvent event) {
		// TODO Auto-generated method stub
		
	}

}
