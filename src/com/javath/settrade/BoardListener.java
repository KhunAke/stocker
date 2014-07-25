package com.javath.settrade;

import java.util.EventListener;

public interface BoardListener extends EventListener {
	public void action(MarketEvent event);
}
