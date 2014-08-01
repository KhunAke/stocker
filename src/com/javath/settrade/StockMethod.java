package com.javath.settrade;

import java.util.HashMap;
import java.util.Map;

import com.javath.trigger.Oscillator;
import com.javath.util.Assign;

public class StockMethod extends Board {
	
	private final static String method_page;
	
	private static final Map<String,StockMethod> instances;
	
	static {
		method_page = assign.getProperty("board_page",
				"http://www.settrade.com/C13_MarketSummaryStockMethod.jsp?method=%s");
		instances = new HashMap<String,StockMethod>();
	}
	
	public static StockMethod getInstance(String method) {
		synchronized (instances) {
			StockMethod instance = instances.get(method);
			if (instance == null) {
				instance = new StockMethod(method);
				instances.put(method, instance);
			}
			return instance;
		}
	}
	
	private String method;
	
	private StockMethod(String method) {
		this.method = method;
	}
	
	protected String getURI() {
		return String.format(method_page, method);
	}
	
}
