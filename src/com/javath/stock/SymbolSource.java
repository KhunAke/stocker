package com.javath.stock;

public interface SymbolSource {
	public boolean addListener(String symbol, SymbolListener listener);
	public boolean removeListener(String symbol, SymbolListener listener);
}
