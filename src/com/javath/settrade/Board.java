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
import com.javath.util.DateTime;
import com.javath.util.Instance;
import com.javath.util.ObjectException;
import com.javath.util.TaskManager;

public class Board extends Instance implements MarketListener, CustomHandler{
	
	private final static Assign assign;
	private final static String storage_path;
	private final static String board_page;
	private final static String charset;
	private static Board instance;
	
	static {
		String default_Properties = Assign.etc + Assign.File_Separator +
				"settrade.properties";
		assign = Assign.getInstance(Market.class, default_Properties);
		//String default_path = Assign.temp + Assign.File_Separator + "settrade";
		String default_path = Assign.temp;
		storage_path = assign.getProperty("storage_path", default_path);
		//board_page = assign.getProperty("board_page",
		//		"http://www.settrade.com/C13_MarketSummaryStockType.jsp?type=S");
		board_page = assign.getProperty("board_page",
				"http://www.settrade.com/C13_MarketSummaryStockMethod.jsp?method=AOM");
		charset = assign.getProperty("charset", "windows-874");
		instance = new Board();
	}
	
	public static Board getInstance() {
		return instance;
	}
	
	private final Set<BoardListener> listeners;
	
	private final Cookie cookie;
	private Date last_update;
	private String[][] rows;
	
	public Board() {
		listeners = new HashSet<BoardListener>();
		cookie = new Cookie();
	}
	
	public boolean addListener(BoardListener listener) {
		return listeners.add(listener);
	}
	public boolean removeListener(BoardListener listener) {
		return listeners.remove(listener);
	}
	private void sendEvent() {
		try {
			EventListener[] listeners = new EventListener[] {};
			listeners = this.listeners.toArray(listeners);
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
	public boolean condition(Node node) {
		try {
			if (node.getNodeName().equals("DIV"))
				return HtmlParser.attribute(node, "class").equals("divDetailBox");
		} catch (NullPointerException e) {
			return false;
		}
		return false;
	}
	public boolean updateRows(Date date, TextNode text_node) {
		ArrayList<String[]> rows = new ArrayList<String[]>();
		text_node.printStringArray(0);
		String[] root = text_node.getStringArray(0);
		System.out.println(text_node.getString(0,root.length - 2));
		if (!text_node.getString(0,root.length - 2)
				.equals("หมายเหตุ: ข้อมูลการซื้อขายแบบ Auto Matching เท่านั้น")) {
			SEVERE("\"%s\" Data loss", DateTime.string(date));
			return false;
		}
		for (int index = 1; index < text_node.length(); index++) {
			String[] data = text_node.getStringArray(index);
			if (data[0].equals("2") && (data[2].length() > 0)) {
				String[] symbolArray = text_node.getStringArray(index + 1);
				String symbol = symbolArray[1];
				try {
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
				} catch (ArrayIndexOutOfBoundsException e) {
					throw new RuntimeException(
							String.format("Symbol=\"%s\", index=%s", symbol, e.getMessage()), e);
				}
			}
		}
		this.last_update = date;
		this.rows = rows.toArray(new String[][] {});
		return true;
	}
	private void parser(Date date, Response response) {
		HtmlParser parser = new HtmlParser(response.getContent(), charset);
		CustomFilter filter = new CustomFilter(parser.parse());
		filter.setHandler(this);
		List<Node> nodes = filter.filter(3);
		
		TextNode text_node = null;
		try {
			text_node = new TextNode(nodes.get(0));
		} catch (java.lang.IndexOutOfBoundsException e) {
			throw new ObjectException(e);
		}
		if (updateRows(date, text_node))
			sendEvent();
	}
	public void run(Date date) {
		Response response = getWebPage();
		if (response.getStatusCode() == 200) {
			try {
				parser(date, response);
			} catch (RuntimeException e) {
				SEVERE(e);
			}
		} else 
			WARNING("%s: %d %s", response.getFilename(), 
					response.getStatusCode(), response.getReasonPhrase());
	}
	@Override
	public void action(MarketEvent event) {
		TaskManager.create(String.format("%s(date=\"%s\")", this.getClassName(), DateTime.string(event.getDate())),
				this, "run", event.getDate());
	}
	
	public static void main(String[] args) {
		Board board = Board.getInstance();
		BoardScreen.getInstance();
		board.run(new Date());
	}

}
