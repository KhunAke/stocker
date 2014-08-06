package com.javath.settrade;

import java.util.EventListener;

public interface StockListener extends EventListener {
	public void action(StockEvent event);
}
