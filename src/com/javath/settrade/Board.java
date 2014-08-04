package com.javath.settrade;

import java.util.ArrayList;
import java.util.Date;
import java.util.EventListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
import com.javath.trigger.MulticastEvent;
import com.javath.util.Assign;
import com.javath.util.DateTime;
import com.javath.util.Instance;
import com.javath.util.ObjectException;
import com.javath.util.TaskManager;

public abstract class Board extends Instance implements BoardListener, MarketListener, CustomHandler{
	
	protected final static Assign assign;
	protected final static String charset;
	private final static Map<String,Board> instances;
	private final static Set<BoardListener> listeners;
	
	static {
		String default_Properties = Assign.etc + Assign.File_Separator +
				"settrade.properties";
		assign = Assign.getInstance(Board.class, default_Properties);
		charset = assign.getProperty("charset", "windows-874");
		instances = new HashMap<String,Board>();
		listeners = new HashSet<BoardListener>();
	}
	
	public static boolean addListener(BoardListener listener) {
		return listeners.add(listener);
	}
	public static boolean removeListener(BoardListener listener) {
		return listeners.remove(listener);
	}
	private static void sendEvent(Object source, Date date, String[][] data) {
		try {
			EventListener[] listeners = 
					Board.listeners.toArray(new EventListener[] {});
			if (listeners.length > 0) { 
				BoardEvent event = new BoardEvent(source, date, data);
				MulticastEvent.send("action", listeners, event);
			}
		} catch (NoSuchElementException e) {
			throw new ObjectException(e);
		} catch (IllegalStateException e) {
			throw new ObjectException(e);
		}
	}
	
	protected static Board get(String key) {
		return instances.get(key);
	}
	protected static Board put(String key, Board value) {
		return instances.put(key, value);
	}
	
	//private final Set<BoardListener> listeners;
	protected final Cookie cookie;
	
	private Date last_update;
	private String[][] data_set;
	private Date current_date;
	private int max_length; 
	
	public Board() {
		cookie = new Cookie();
		//
		last_update = new Date(0);
		data_set = new String[][] {};
		current_date = DateTime.splitDate(last_update);
		max_length = data_set.length;
		//
		Market.getInstance().addMarketListener(this);
	}
	
	/**
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
	/**/
	
	@Override
	public void action(MarketEvent event) {
		TaskManager.create(String.format("%s(date=\"%s\")", this.getClassName(), DateTime.string(event.getDate())),
				this, "run", event.getDate());
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
	protected Response getWebPage() {
		Browser browser = (Browser) Assign.borrowObject(Browser.class);
		browser.setCookie(cookie.getCookieStore());
		try {
			browser.address(getURI());
			return browser.get();
		} finally {
			Assign.returnObject(browser);
		}
	}
	protected abstract String getURI();
	protected void parser(Date date, Response response) {
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
	protected void parser(Date date, TextNode text_node) {
		//text_node.print();
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
					WARNING(new RuntimeException(
								String.format("%s,\"%s\" has data loss later \"%s\"", 
								getKey(), DateTime.string(date), rows.get(rows.size() - 1)[0]), e));
					sendEvent(this, date, rows.toArray(new String[][] {}));
					return;
				}
			}
		}
		String[][] data_set = rows.toArray(new String[][] {});
		Date last_update = getLastUpdate();
		if (date.after(last_update))
			if (!update(date, data_set))
				WARNING(new RuntimeException(
						String.format("%s,\"%s\" has data loss later \"%s\"", 
						getKey(), DateTime.string(date), data_set[data_set.length - 1][0])));
		else if (date.before(last_update))//before(when)
			WARNING("Server delayed because request of \"%s\" but received after \"%s\"", 
					DateTime.string(date), DateTime.string(last_update));
		sendEvent(this, date, data_set);
	}
	
	public Date getLastUpdate() {
		synchronized (this) {
			return last_update;
		}
	}
	private boolean update(Date date, String[][] data_set) {
		synchronized (this) {
			last_update = date;
			this.data_set = data_set;
			if (current_date.equals(DateTime.splitDate(last_update))) {
				if (data_set.length < max_length)
					return false;
				else {
					max_length = data_set.length;
					return true;
				}
			} else {
				current_date = DateTime.splitDate(last_update);
				max_length = data_set.length;
				return true;
			}
		}
	}
	public String[][] getDataSet() {
		synchronized (this) {
			return data_set;
		}
	}
	protected int getMaxLength() {
		synchronized (this) {
			return max_length;
		}
	}
	public abstract String getKey();
	
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
							data_set[index][BoardEvent.SYMBOL],
							event.getDate());
					board = new SettradeBoard(id);
					try {
						board.setOpen(Double.valueOf(data_set[index][BoardEvent.OPEN]));
					} catch (NumberFormatException e) {}
					try {
						board.setHigh(Double.valueOf(data_set[index][BoardEvent.HIGH]));
					} catch (NumberFormatException e) {}
					try {
						board.setLow(Double.valueOf(data_set[index][BoardEvent.LOW]));
					} catch (NumberFormatException e) {}
					try {
						board.setLast(Double.valueOf(data_set[index][BoardEvent.LAST]));
					} catch (NumberFormatException e) {}
					try {
						board.setChangePrior(Double.valueOf(data_set[index][BoardEvent.CHANGE]));
					} catch (NumberFormatException e) {}
					try {
						board.setBid(Double.valueOf(data_set[index][BoardEvent.BID]));
					} catch (NumberFormatException e) {}
					try {
						board.setOffer(Double.valueOf(data_set[index][BoardEvent.OFFER]));
					} catch (NumberFormatException e) {}
					try {
						board.setVolume(Long.valueOf(data_set[index][BoardEvent.VOLUME]));
					} catch (NumberFormatException e) {}
					try {
						board.setValue(Double.valueOf(data_set[index][BoardEvent.VALUE]));
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

}
