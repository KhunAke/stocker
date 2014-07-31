package com.javath.settrade;

import java.util.Date;

import com.javath.util.DateTime;

public class BoardScreen implements BoardListener {
	
	private static BoardScreen instance = new BoardScreen();
	
	public static BoardScreen getInstance() {
		return instance;
	}
	
	private StringBuffer buffer;
	
	private BoardScreen() {
		buffer = new StringBuffer();
		Board board = Board.getInstance();
		board.addListener(this);
	}
	
	@Override
	public void action(BoardEvent event) {
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
				change = Double.valueOf(rows[index][Board.CHANGE]);
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
		System.out.printf("%s: Update=\"%s\", gainers=%d, unchanged=%d, losers=%d%n", DateTime.timestamp(new Date()), 
				DateTime.string(event.getDate()), gainers, unchanged, losers);
	}

}
