package com.javath.settrade;

import java.util.EventListener;

public interface MarketStatusListener extends EventListener {
	public void action(MarketStatusEvent event);
}
