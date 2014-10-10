package com.javath.set.strategy;

import com.javath.mapping.SettradeQuote;
import com.javath.settrade.QuoteEvent;
import com.javath.settrade.QuoteFilter;
import com.javath.util.DateTime;
import com.javath.util.FilterEvent;

public class QuoteUpDown extends QuoteFilter {
	private static int record_length = 3;
	public static QuoteUpDown getInstance(String symbol) {
		QuoteUpDown result = (QuoteUpDown) instances.get(symbol);
		if (result == null) {
			result = new QuoteUpDown(symbol);
			instances.put(symbol, result);
		}
		return result;
	}
	
	private final QuoteRecord[] record;
	private int current = -1;
	private long total_volume = 0;
	private int trend = 0;
	
	private QuoteUpDown(String symbol) {
		super(symbol);
		record = new QuoteRecord[record_length];
		for (int index = 0; index < record_length; index++) {
			record[index] = new QuoteRecord();
		}
		QuoteChangeVolume.getInstance(symbol)
			.addSymbolListener(symbol, this);
	}
	
	public void run(QuoteEvent event) {
		int compare = 0;
		SettradeQuote quote = event.getQuote();
		//System.out.printf("%s,%s,%.2f,%d%n", 
		//		DateTime.string(quote.getId().getDate()), quote.getId().getSymbol(),
		//		quote.getLast(), quote.getVolume() - total_valume);
		double price = quote.getLast();
		try {
			compare = Double.compare(price, record[current].getPrice());
		} catch (ArrayIndexOutOfBoundsException e) {
			compare = 0;
			current = 0;
			record[current].set(price);
		}
		if (compare == 0)
			record[current].add(quote.getVolume() - total_volume);
		else {
			current += compare;
			int forward = compare;
			try {
				compare = Double.compare(price, record[current].getPrice());
			} catch (ArrayIndexOutOfBoundsException e) {
				if (current == record_length)
					current = 0;
				else if (current == -1)
					current = record_length - 1;
				compare = Double.compare(price, record[current].getPrice());
			}
			if (compare == 0)
				record[current].add(quote.getVolume() - total_volume);
			else {
				record[current].set(price);
				record[current].add(quote.getVolume() - total_volume);
				try {
					record[current + forward].set(price);
				} catch (ArrayIndexOutOfBoundsException e) {
					if (current + forward == record_length)
						record[0].set(0.0);
					else if (current == -1)
						record[record_length - 1].set(0.0);
					e.printStackTrace();
				}
				FilterEvent filter = event.appendFilter(this);
				filter.setInteger("trend", forward);
				send(event);
			}
		}
		total_volume = quote.getVolume(); 
	}
	
}
