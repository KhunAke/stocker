package com.javath.settrade;

import java.util.Date;

import com.javath.util.DateTime;

public class MarketScreen implements MarketListener {
	
	private static MarketScreen instance = new MarketScreen();
	
	public static MarketScreen getInstance() {
		return instance;
	}
	
	private StringBuffer buffer;
	
	private MarketScreen() {
		buffer = new StringBuffer();
		Market market = Market.getInstance();
		market.addMarketListener(this);
	}
	
	@Override
	public void action(MarketEvent event) {
		String[][] rows = event.getRows();
		buffer.delete(0, buffer.length());
		buffer.append("Update=\"");
		buffer.append(DateTime.string(event.getDate()));
		buffer.append("\", ");
		for (int index = 0; index < rows.length; index++) {
			buffer.append(rows[index][MarketEvent.NAME]);
			buffer.append("=");
			buffer.append(rows[index][MarketEvent.LAST]);
			buffer.append(", ");
		}
		System.out.printf("%s: %s%n", DateTime.timestamp(new Date()), 
				buffer.substring(0, buffer.length() - 2));
	}

}
