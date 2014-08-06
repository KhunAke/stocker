package com.javath.settrade;

public class StockType extends Stock {

	private final static String type_page;
	
	static {
		type_page = assign.getProperty("type_page",
				"http://www.settrade.com/C13_MarketSummaryStockType.jsp?type=%s");
	}
	
	public static StockType getInstance(String type) {
		StockType instance = (StockType) Stock.get("type=" + type);
		if (instance == null) {
			instance = new StockType(type);
			Stock.put("type=" + type, instance);
		}
		return instance;
	}
	
	private String type;
	
	private StockType(String type) {
		this.type = type;
	}
	
	protected String getURI() {
		return String.format(type_page, type);
	}
	
	@Override
	public String getKey() {
		return  String.format("type=\"%s\"", type);
	}
}
