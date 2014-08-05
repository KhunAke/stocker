package com.javath.settrade;

public class StockMethod extends Board {
	
	private final static String method_page;
	
	static {
		method_page = assign.getProperty("method_page",
				"http://www.settrade.com/C13_MarketSummaryStockMethod.jsp?method=%s");
	}
	
	public static StockMethod getInstance(String method) {
		StockMethod instance = (StockMethod) Board.get("method=" + method);
		if (instance == null) {
			instance = new StockMethod(method);
			Board.put("method=" + method, instance);
		}
		return instance;
	}
	
	private String method;
	
	private StockMethod(String method) {
		this.method = method;
	}
	
	protected String getURI() {
		return String.format(method_page, method);
	}

	@Override
	public String getKey() {
		return String.format("method=\"%s\"", method);
	}

}
