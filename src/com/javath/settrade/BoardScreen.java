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
	public void action(MarketEvent event) {
		String[][] rows = event.getRows();
		buffer.delete(0, buffer.length());
		buffer.append("Update=\"");
		buffer.append(DateTime.string(event.getDate()));
		buffer.append("\", ");
		for (int index = 0; index < rows.length; index++) {
			buffer.append(rows[index][MarketEvent.NAME]);
			buffer.append("=");
			buffer.append(rows[index][MarketEvent.CHANGE]);
			buffer.append(", ");
		}
		System.out.printf("%s: %s%n", DateTime.timestamp(new Date()), 
				buffer.substring(0, buffer.length() - 2));
	}

}
