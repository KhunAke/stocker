package com.javath.settrade;

import java.util.ArrayList;
import java.util.Date;
import java.util.EventListener;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.hibernate.Session;
import org.hibernate.exception.ConstraintViolationException;
import org.w3c.dom.Node;

import com.javath.html.CustomFilter;
import com.javath.html.CustomHandler;
import com.javath.html.HtmlParser;
import com.javath.html.TextNode;
import com.javath.http.Browser;
import com.javath.http.Cookie;
import com.javath.http.Response;
import com.javath.logger.LOG;
import com.javath.mapping.SettradeBoard;
import com.javath.mapping.SettradeBoardHome;
import com.javath.mapping.SettradeBoardId;
import com.javath.mapping.SettradeMarket;
import com.javath.mapping.SettradeMarketHome;
import com.javath.mapping.SettradeMarketId;
import com.javath.trigger.MulticastEvent;
import com.javath.util.Assign;
import com.javath.util.DateTime;
import com.javath.util.Instance;
import com.javath.util.ObjectException;
import com.javath.util.TaskManager;

public class Board extends Instance implements BoardListener, MarketListener, CustomHandler{
	
	public final static int SYMBOL = 0;
	public final static int OPEN = 1;
	public final static int HIGH = 2;
	public final static int LOW = 3;
	public final static int LAST = 4;
	public final static int CHANGE = 5;
	public final static int BID = 6;
	public final static int OFFER = 7;
	public final static int VOLUME = 8;
	public final static int VALUE = 9;
	
	private final static Assign assign;
	private final static String storage_path;
	private final static String board_page;
	private final static String board_data_check;
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
		board_data_check = assign.getProperty("board_data_check", "หมายเหตุ: ข้อมูลการซื้อขายแบบ Auto Matching เท่านั้น");
		instance = new Board();
	}
	
	public static Board getInstance() {
		return instance;
	}
	
	private final Set<BoardListener> listeners;
	
	private final Cookie cookie;
	private Date last_update;
	private String[][] data_set;
	
	public Board() {
		listeners = new HashSet<BoardListener>();
		cookie = new Cookie();
		
		last_update = new Date(0); 
		if (assign.getBooleanProperty("board_upload", true))
			this.addListener(this);
	}
	
	public boolean addListener(BoardListener listener) {
		return listeners.add(listener);
	}
	public boolean removeListener(BoardListener listener) {
		return listeners.remove(listener);
	}
	private void sendEvent(Date date, String[][] data) {
		try {
			EventListener[] listeners = new EventListener[] {};
			listeners = this.listeners.toArray(listeners);
			if (listeners.length > 0) { 
				BoardEvent event = new BoardEvent(this, date, data);
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
	private void parser(Date date, TextNode text_node) {
		ArrayList<String[]> rows = new ArrayList<String[]>();
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
					//text_node.getStringArray(0);
					String[] root = text_node.getStringArray(0);
					if (root[root.length - 2]
							.equals(board_data_check))
						throw new ObjectException(e,
								"Date=\"%s\", Symbol=\"%s\", index=%s", 
								DateTime.string(date), symbol, e.getMessage());
					else {
						WARNING(new RuntimeException(
								String.format("\"%s\" has data loss at Symbol=\"%s\", index=%s", 
								DateTime.string(date), symbol, e.getMessage()), e));
						sendEvent(date, rows.toArray(new String[][] {}));
						return;
					}
				}
			}
		}
		String[] root = text_node.getStringArray(0);
		if (!root[root.length - 2]
				.equals(board_data_check))
			WARNING("\"%s\" has data loss", DateTime.string(date));
		Date last_update = getLastUpdate();
		String[][] data_set = rows.toArray(new String[][] {});
		if (date.after(last_update))
			update(date, data_set);
		else //before(when)
			WARNING("Server delayed because request of \"%s\" but received after \"%s\"", 
					DateTime.string(date), DateTime.string(last_update));
		sendEvent(date, data_set);
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
		parser(date, text_node);
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
	@Override
	public void action(BoardEvent event) {
		TaskManager.create(String.format("%s.upload(date=\"%s\")", this.getClassName(), DateTime.string(event.getDate())),
				this, "upload", event);
	}
	public void upload(BoardEvent event) {
		String[][] data_set = event.getDataSet();
		SettradeBoardHome home = (SettradeBoardHome)
				Assign.borrowObject(SettradeBoardHome.class);
		try {
			Session session = Assign.getSessionFactory().getCurrentSession();
			session.beginTransaction();
			SettradeBoard board = null;
			try {
				for (int index = 0; index < data_set.length; index++) {
					SettradeBoardId id = new SettradeBoardId(
							data_set[index][SYMBOL],
							event.getDate());
					board = new SettradeBoard(id);
					try {
						board.setOpen(Double.valueOf(data_set[index][OPEN]));
					} catch (NumberFormatException e) {}
					try {
						board.setHigh(Double.valueOf(data_set[index][HIGH]));
					} catch (NumberFormatException e) {}
					try {
						board.setLow(Double.valueOf(data_set[index][LOW]));
					} catch (NumberFormatException e) {}
					try {
						board.setLast(Double.valueOf(data_set[index][LAST]));
					} catch (NumberFormatException e) {}
					try {
						board.setChangePrior(Double.valueOf(data_set[index][CHANGE]));
					} catch (NumberFormatException e) {}
					try {
						board.setBid(Double.valueOf(data_set[index][BID]));
					} catch (NumberFormatException e) {}
					try {
						board.setOffer(Double.valueOf(data_set[index][OFFER]));
					} catch (NumberFormatException e) {}
					try {
						board.setVolume(Long.valueOf(data_set[index][VOLUME]));
					} catch (NumberFormatException e) {}
					try {
						board.setValue(Double.valueOf(data_set[index][VALUE]));
					} catch (NumberFormatException e) {}
					home.persist(board);	
				}
				session.getTransaction().commit();
			} catch (ConstraintViolationException e) {
				LOG.WARNING(new ObjectException(e.getCause(), "%s; %s", 
						e.getMessage(), e.getCause().getMessage()));
				session.getTransaction().rollback();
			} catch (Exception e) {
				session.getTransaction().rollback();
				throw e;
			}
		} finally {
			Assign.returnObject(home);
		}
	}
	
	public Date getLastUpdate() {
		synchronized (instance) {
			return last_update;
		}
	}
	private void update(Date date, String[][] data_set) {
		synchronized (instance) {
			last_update = date;
			this.data_set = data_set;
		}
	}
	public String[][] getDataSet() {
		synchronized (instance) {
			return data_set;
		}
	}
	
	public static void main(String[] args) {
		Board board = Board.getInstance();
		BoardScreen.getInstance();
		board.run(new Date());
	}


}
