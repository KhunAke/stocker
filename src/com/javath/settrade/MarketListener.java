package com.javath.settrade;

import java.util.EventListener;

public interface MarketListener extends EventListener {
	
	public void action(MarketEvent event);
	
}
