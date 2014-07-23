package com.javath.settrade;

import java.util.Date;

import com.javath.util.DateTime;

public class MarketStatusScreen implements MarketStatusListener {
	
	private static MarketStatusScreen instance = new MarketStatusScreen();
	
	public static MarketStatusScreen getInstance() {
		return instance;
	}
	
	private MarketStatusScreen() {
		Market market = Market.getInstance();
		market.addStatusListener(this);
	}

	@Override
	public void action(MarketStatusEvent event) {
		System.out.printf("%s: Update=\"%s\", Status=\"%s\"%n", DateTime.timestamp(new Date()), 
				DateTime.string(event.getDate()), event.getStatus());
	}
	
}
