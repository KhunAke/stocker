package com.javath.settrade;

import java.util.Date;
import java.util.List;

import org.w3c.dom.Node;

import com.javath.html.CustomFilter;
import com.javath.html.HtmlParser;
import com.javath.html.TextNode;
import com.javath.http.Response;
import com.javath.util.ObjectException;

public class StockIndustry extends Board {
	
	private final static String industry_page;
	
	static {
		industry_page = assign.getProperty("industry_page",
				"http://www.settrade.com/C13_MarketSummaryIndustry.jsp?sector=%s&industry=%s");
	}
	
	public static StockIndustry getInstance(String sector, String industry) {
		String key = String.format("industry=%s,%s", sector, industry);
		StockIndustry instance = (StockIndustry) Board.get(key);
		if (instance == null) {
			instance = new StockIndustry(sector, industry);
			Board.put(key, instance);
		}
		return instance;
	}
	
	private String sector;
	private String industry;
	
	private StockIndustry(String sector, String industry) {
		this.sector = sector;
		this.industry = industry;
	}
	
	protected void parser(Date date, Response response) {
		HtmlParser parser = new HtmlParser(response.getContent(), charset);
		CustomFilter filter = new CustomFilter(parser.parse());
		filter.setHandler(this);
		List<Node> nodes = filter.filter(3);
		TextNode text_node = null;
		try {
			text_node = new TextNode(nodes.get(1));
		} catch (java.lang.IndexOutOfBoundsException e) {
			throw new ObjectException(e);
		}
		parser(date, text_node);
	}
	
	@Override
	protected String getURI() {
		return String.format(industry_page, sector, industry);
	}

	@Override
	public String getKey() {
		return String.format("industry=\"%s,%s\"", sector, industry);
	}

	public static void main(String[] args) {
		StockIndustry industry1 = StockIndustry.getInstance("", "AGRO");
		StockIndustry industry2 = StockIndustry.getInstance("", "CONSUMP");
		StockIndustry industry3 = StockIndustry.getInstance("", "FINCIAL");
		StockIndustry industry4 = StockIndustry.getInstance("", "INDUS");
		StockIndustry industry5 = StockIndustry.getInstance("", "PROPCON");
		StockIndustry industry6 = StockIndustry.getInstance("", "RESOURC");
		StockIndustry industry7 = StockIndustry.getInstance("", "SERVICE");
		StockIndustry industry8 = StockIndustry.getInstance("", "TECH");
		BoardScreen.getInstance();
		industry1.run(new Date(0));
		industry2.run(new Date(0));
		industry3.run(new Date(0));
		industry4.run(new Date(0));
		industry5.run(new Date(0));
		industry6.run(new Date(0));
		industry7.run(new Date(0));
		industry8.run(new Date(0));
	}
	
}
