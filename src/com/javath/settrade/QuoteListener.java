package com.javath.settrade;

import java.util.EventListener;

public interface QuoteListener extends EventListener {
	public void action(QuoteEvent event);
}
