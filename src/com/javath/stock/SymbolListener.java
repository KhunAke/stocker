package com.javath.stock;

import java.util.EventListener;

public interface SymbolListener extends EventListener {
	public void action(SymbolEvent event);
}
