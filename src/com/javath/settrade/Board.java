package com.javath.settrade;

import java.util.ArrayList;
import java.util.Date;
import java.util.EventListener;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.w3c.dom.Node;

import com.javath.html.CustomFilter;
import com.javath.html.CustomHandler;
import com.javath.html.HtmlParser;
import com.javath.html.TextNode;
import com.javath.http.Browser;
import com.javath.http.Cookie;
import com.javath.http.Response;
import com.javath.trigger.MulticastEvent;
import com.javath.util.Assign;
import com.javath.util.Instance;
import com.javath.util.NotificationAdaptor;
import com.javath.util.ObjectException;

public class Board extends Instance implements CustomHandler, Runnable{
	
	private final static Assign assign;
	private final static String board_page;
	private final static String charset;
	private static Board instance;
	
	static {
		String default_Properties = Assign.etc + Assign.File_Separator +
				"settrade.properties";
		assign = Assign.getInstance(Board.class, default_Properties);
		board_page = assign.getProperty("board_page",
				"http://www.settrade.com/C13_MarketSummaryStockMethod.jsp?method=AOM");
		charset = assign.getProperty("charset", "windows-874");
		instance = new Board();
	}
	
	public static Board getInstance() {
		return instance;
	}
	
	private final Cookie cookie;
	private final Set<BoardListener> board_listeners;
	
	private Date last_update;
	private String[][] rows;
	
	private Board() {
		cookie = new Cookie();
		board_listeners = new HashSet<BoardListener>();
		
		last_update = new Date(0);
	}
	
	public boolean addListener(BoardListener listener) {
		return board_listeners.add(listener);
	}
	public boolean removeListener(BoardListener listener) {
		return board_listeners.remove(listener);
	}
	
	private Response getWebPage() {
		Browser browser = (Browser) Assign.borrowObject(Browser.class);
		browser.setCookie(cookie.getCookieStore());
		try {
			browser.address(getURI());
			return browser.get();
		} finally {
			Assign.returnObject(browser);
		}
	}
	private String getURI() {
		return board_page;
	}

	@Override
	public void run() {
		Response response = getWebPage();
		parser(new Date(0), response);
	}
	
	private void parser(Date datetime, Response response) {
		HtmlParser parser = new HtmlParser(response.getContent(), charset);
		CustomFilter filter = new CustomFilter(parser.parse());
		filter.setHandler(this);
		List<Node> nodes = filter.filter(3);
		TextNode textNode = null;
		try {
			textNode =new TextNode(nodes.get(0));
		} catch (java.lang.IndexOutOfBoundsException e) {
			throw new ObjectException(e);
		}
		updateRows(datetime, textNode);
		sendEvent();
	}
	@Override
	public boolean condition(Node node) {
		try {
			if (node.getNodeName().equals("DIV"))
				return HtmlParser.attribute(node, "class").equals("divDetailBox");
		} catch (NullPointerException e) {
			return false;
		}
		return false;
	}
	public void updateRows(Date datetime, TextNode node) {
		ArrayList<String[]> rows = new ArrayList<String[]>();
		for (int index = 1; index < node.length(); index++) {
			String[] data = node.getStringArray(index);
			if (data[0].equals("2") && (data[2].length() > 0)) {
				String[] symbolArray = node.getStringArray(index + 1);
				String symbol = symbolArray[1];
				String open = Market.replace(data[4]);
				String high = Market.replace(data[6]);
				String low = Market.replace(data[8]);
				String last = Market.replace(data[10]);
				String change = Market.replace(data[12]);
				String bid = Market.replace(data[16]);
				String offer = Market.replace(data[18]);
				String volume = Market.replace(data[20]);
				String value = Market.replace(data[22]);
				rows.add(new String[]
						{symbol, open, high, low, last, change, bid, offer, volume, value});
			}
		}
		last_update = datetime;
		this.rows = rows.toArray(new String[][] {});
	}
	private void sendEvent() {
		try {
			EventListener[] listeners = new EventListener[] {};
			listeners = this.board_listeners.toArray(listeners);
			if (listeners.length > 0) { 
				BoardEvent event = new BoardEvent(this, last_update, rows);
				MulticastEvent.send("action", listeners, event);
			}
		} catch (NoSuchElementException e) {
			throw new ObjectException(e);
		} catch (IllegalStateException e) {
			throw new ObjectException(e);
		}
	}
	public static void main(String[] args) {
		Board stock = Board.getInstance();
		BoardScreen.getInstance();
		stock.run();
	}

	
}
