package com.javath.settrade;

public class StockCommand extends Stock {
	
	private final static String command_page;
	
	static {
		command_page = assign.getProperty("command_page",
				"http://www.settrade.com/C13_MarketSummarySET.jsp?command=%s");
	}
	
	public static StockCommand getInstance(String command) {
		String key = String.format("command=%s", command);
		StockCommand instance = (StockCommand) Stock.get(key);
		if (instance == null) {
			instance = new StockCommand(command);
			Stock.put(key, instance);
		}
		return instance;
	}
	
	private String command;
	
	private StockCommand(String command) {
		this.command = command;
	}
	
	@Override
	protected String getURI() {
		return String.format(command_page, command);
	}

	@Override
	public String getKey() {
		return String.format("command=\"%s\"", command);
	}

}
