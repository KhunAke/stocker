package com.javath.settrade;

import java.util.HashMap;
import java.util.Map;

import com.javath.util.Assign;

public class StockType extends Board {

	private final static String type_page;
	
	private static final Map<String,StockType> instances;
	
	static {
		type_page = assign.getProperty("board_page",
				"http://www.settrade.com/C13_MarketSummaryStockType.jsp?type=%s");
		instances = new HashMap<String,StockType>();
	}
	
	public static StockType getInstance(String type) {
		synchronized (instances) {
			StockType instance = instances.get(type);
			if (instance == null) {
				instance = new StockType(type);
				instances.put(type, instance);
			}
			return instance;
		}
	}
	
	private String type;
	
	private StockType(String type) {
		this.type = type;
	}
	
	protected String getURI() {
		return String.format(type_page, type);
	}
	
	
}
