package com.javath.settrade;

import java.util.Date;

import com.javath.util.DateTime;

public class StockScreen implements StockListener {
	
	private final static StockScreen instance;
	
	static {
		instance = new StockScreen();
	}
	
	public static StockScreen getInstance() {
		return instance;
	}
	
	private StringBuffer buffer;
	
	private StockScreen() {
		buffer = new StringBuffer();
		Stock.addListener(this);
	}

	@Override
	public void action(StockEvent event) {
		String[][] rows = event.getDataSet();
		buffer.delete(0, buffer.length());
		buffer.append("Update=\"");
		buffer.append(DateTime.string(event.getDate()));
		buffer.append("\", ");
		int gainers = 0;
		int unchanged = 0; 
		int losers = 0;
		for (int index = 0; index < rows.length; index++) {
			double change = 0.0; 
			try {
				change = Double.valueOf(rows[index][StockEvent.CHANGE]);
			} catch (NumberFormatException e) {}
			if (change > 0.0)
				gainers += 1;
			else if (change < 0.0)
				losers += 1;
			else
				unchanged +=1;
			//buffer.append(rows[index][BoardEvent.SYMBOL]);
			//buffer.append("=");
			//buffer.append(rows[index][BoardEvent.CHANGE]);
			//buffer.append(", ");
		}
		Stock board = (Stock) event.getSource();
		System.out.printf("%s: Update=\"%s\", %s, gainers=%d, unchanged=%d, losers=%d%n", DateTime.timestamp(new Date()), 
				DateTime.string(event.getDate()), board.getKey(), gainers, unchanged, losers);
	}

}
