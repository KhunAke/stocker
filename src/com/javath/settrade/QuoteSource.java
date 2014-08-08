package com.javath.settrade;

public interface QuoteSource {
	public boolean addSymbolListener(String symbol, QuoteListener listener);
	public boolean removeSymbolListener(String symbol, QuoteListener listener);
}
