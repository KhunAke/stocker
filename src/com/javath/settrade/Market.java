package com.javath.settrade;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
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
import com.javath.mapping.SettradeMarket;
import com.javath.mapping.SettradeMarketHome;
import com.javath.mapping.SettradeMarketId;
import com.javath.trigger.MulticastEvent;
import com.javath.trigger.Oscillator;
import com.javath.trigger.OscillatorDivideFilter;
import com.javath.trigger.OscillatorEvent;
import com.javath.trigger.OscillatorLoader;
import com.javath.util.Assign;
import com.javath.util.DateTime;
import com.javath.util.Instance;
import com.javath.util.ObjectException;
import com.javath.util.TaskManager;

public class Market extends Instance implements OscillatorLoader, MarketListener, CustomHandler, Runnable {
	
	private final static Assign assign;
	private final static String storage_path;
	private final static String market_page;
	private final static String charset;
	private static Market instance;
	
	static {
		String default_Properties = Assign.etc + Assign.File_Separator +
				"settrade.properties";
		assign = Assign.getInstance(Market.class, default_Properties);
		String default_path = Assign.temp + Assign.File_Separator + "settrade";
		storage_path = assign.getProperty("storage_path", default_path);
		market_page = assign.getProperty("market_page",
				"http://www.settrade.com/C13_MarketSummary.jsp?detail=SET");
		charset = assign.getProperty("charset", "windows-874");
		instance = new Market();
	}

	public static Market getInstance() {
		return instance;
	}
	
	private final Cookie cookie;
	private final Set<MarketListener> market_listeners;
	private final Set<MarketStatusListener> status_listeners;
	private final long interval_update;
	
	private OscillatorDivideFilter oscillator;
	private MarketStatus status;
	private Date last_update;
	private String[][] data_set ;
	
	private Market() {
		cookie = new Cookie(); 
		market_listeners = new HashSet<MarketListener>();
		status_listeners = new HashSet<MarketStatusListener>();
		//
		last_update = new Date(0);
		status = MarketStatus.Unknow;
		//
		interval_update = assign.getLongProperty("interval_update", 16000);
		if (assign.getBooleanProperty("market_upload", true))
			this.addMarketListener(this);
	}
	
	public boolean addMarketListener(MarketListener listener) {
		return market_listeners.add(listener);
	}
	public boolean removeMarketListener(MarketListener listener) {
		return market_listeners.remove(listener);
	}
	private void sendMarketEvent(Date date, MarketStatus status, String[][] data) {
		try {
			EventListener[] listeners = new EventListener[] {};
			listeners = this.market_listeners.toArray(listeners);
			if (listeners.length > 0) { 
				MarketEvent event = new MarketEvent(this, date, status, data);
				MulticastEvent.send("action", listeners, event);
			}
		} catch (NoSuchElementException e) {
			throw new ObjectException(e);
		} catch (IllegalStateException e) {
			throw new ObjectException(e);
		}
	}
	public boolean addStatusListener(MarketStatusListener listener) {
		return status_listeners.add(listener);
	}
	public boolean removeStatusListener(MarketStatusListener listener) {
		return status_listeners.remove(listener);
	}
	private void sendStatusEvent(Date date, MarketStatus status) {
		try {
			EventListener[] listeners = new EventListener[] {};
			listeners = this.status_listeners.toArray(listeners);
			if (listeners.length > 0) { 
				MarketStatusEvent event = new MarketStatusEvent(this, date, status);
				MulticastEvent.send("action", listeners, event);
			}
		} catch (NoSuchElementException e) {
			throw new ObjectException(e);
		} catch (IllegalStateException e) {
			throw new ObjectException(e);
		}
	}
	
	@Override
	public void initOscillator() {
		if (oscillator != null)
			return;
		long clock = assign.getLongProperty("clock", 1000); // 1 seconds
		Oscillator source = Oscillator.getInstance(clock);
		long try_again = (long) Math.ceil( // 5 seconds
				assign.getLongProperty("try_again", clock * 5) / (double) clock);
		try_again = (try_again == 0) ? 1 : try_again;
		long datetime = DateTime.date().getTime();
		oscillator = new OscillatorDivideFilter(source, this, try_again, datetime);
	}
	@Override
	public void action(OscillatorEvent event) {
		TaskManager.create(
				String.format("%s[timestamp=%s]", 
						this.getClassName(), DateTime.timestamp(event.getTimestamp())), 
				this);
	}
	@Override
	public void action(MarketEvent event) {
		TaskManager.create(String.format("%s.upload(date=\"%s\")", this.getClassName(), DateTime.string(event.getDate())),
				this, "upload", event);
	}
	public void upload(MarketEvent event) {
		String[][] data_set = event.getDataSet();
		SettradeMarketHome home = (SettradeMarketHome)
				Assign.borrowObject(SettradeMarketHome.class);
		try {
			Session session = Assign.getSessionFactory().getCurrentSession();
			session.beginTransaction();
			SettradeMarket market = null;
			try {
				for (int index = 0; index < data_set.length; index++) {
					SettradeMarketId id = new SettradeMarketId(
							data_set[index][MarketEvent.NAME],
							event.getDate());
					market = new SettradeMarket(id);
					try {
						market.setLast(Double.valueOf(data_set[index][MarketEvent.LAST]));
					} catch (NumberFormatException e) {}
					try {
						market.setChangePrior(Double.valueOf(data_set[index][MarketEvent.CHANGE]));
					} catch (NumberFormatException e) {}
					try {
						market.setHigh(Double.valueOf(data_set[index][MarketEvent.HIGH]));
					} catch (NumberFormatException e) {}
					try {
						market.setLow(Double.valueOf(data_set[index][MarketEvent.LOW]));
					} catch (NumberFormatException e) {}
					try {
						market.setVolume(Long.valueOf(data_set[index][MarketEvent.VOLUME]));
					} catch (NumberFormatException e) {}
					try {
						market.setValue(Double.valueOf(data_set[index][MarketEvent.VALUE]));
					} catch (NumberFormatException e) {}
					home.persist(market);	
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
	
	@Override
	public void run() {
		Response response = getWebPage();
		parser(response);
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
		return market_page;
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
	private void assignNextSchedule(Date date, MarketStatus status) {
		long timestamp = 0;
		switch (status) {
		case Empty:
			timestamp = MarketStatus.PreOpen_I.getBegin(new Date());
			break;
		case PreOpen_I:
		case Open_I:
		case PreOpen_II:
		case Open_II:
		case PreClose:
		case OffHour:
			timestamp = date.getTime() + interval_update;
			break;
		case Intermission:
			timestamp = MarketStatus.PreOpen_II.getBegin(new Date());
			break;
		case Closed:
			long update = date.getTime() % 86400000;
			Calendar calendar = DateTime.borrowCalendar();
			try {
				// Trading quotation will be officially updated at around 18:30
				// 9:30 - 18:30
				if ((update > 9000000) && (update < 41400000) ) {
					calendar.set(Calendar.HOUR_OF_DAY, 18);
					calendar.set(Calendar.MINUTE, 30);
					calendar.set(Calendar.SECOND, 0);
					calendar.set(Calendar.MILLISECOND, 0);
				} else if (update <= 9000000) { // next task at 09:30 of today
					calendar.set(Calendar.HOUR_OF_DAY, 9);
					calendar.set(Calendar.MINUTE, 30);
					calendar.set(Calendar.SECOND, 0);
					calendar.set(Calendar.MILLISECOND, 0);
				} else { // next task at 09:30 of tomorrow
					calendar.add(Calendar.DATE, 1);
					calendar.set(Calendar.HOUR_OF_DAY, 9);
					calendar.set(Calendar.MINUTE, 30);
					calendar.set(Calendar.SECOND, 0);
					calendar.set(Calendar.MILLISECOND, 0);
					if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY)
						calendar.add(Calendar.DATE, 2);
					else if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)
						calendar.add(Calendar.DATE, 1);
				}
				timestamp = calendar.getTimeInMillis();
			} finally {
				DateTime.returnCalendar(calendar);
			}
			break;
		default:
			timestamp = date.getTime() + interval_update;
			WARNING("Unknow Status at \"%s\"", DateTime.string(date));
			break;
		}
		FINE("Next schedule at \"%s\"", DateTime.timestamp(timestamp));
		oscillator.setSchedule(timestamp);
	}
	
	private void parser(Response response) {
		HtmlParser parser = new HtmlParser(response.getContent(), charset);
		CustomFilter filter = new CustomFilter(parser.parse());
		filter.setHandler(this);
		List<Node> nodes = filter.filter(6);
		
		TextNode text_node = null;
		try {
			text_node = new TextNode(nodes.get(0));
		} catch (IndexOutOfBoundsException e) {
			throw new ObjectException(e);
		}
		
		Date date = null;
		try {
			date = DateTime.format("ข้อมูลล่าสุด dd/MM/yyyy HH:mm:ss", text_node.getString(0, 2));
		} catch (ObjectException e) {
			if (e.getCause() instanceof ParseException) {
				WARNING(e.getMessage());
				date = DateTime.date();
			} else {
				throw e;
			}
		}
		
		Date last_update = getLastUpdate();
		if (date.after(last_update)) {
			MarketStatus status = MarketStatus.getStatus(text_node.getString(8, 4));
			if (!status.equals(this.status)) {
				INFO("Status at \"%s\" is \"%s\"", DateTime.string(date), status);
				sendStatusEvent(date, status);
			}
			assignNextSchedule(date, status);
			parser(date, status, text_node);
		} else if (date.before(last_update)) { //before(when)
			WARNING("Server delayed because request of \"%s\" but received after \"%s\"", 
					DateTime.string(date), DateTime.string(last_update));
		}
	}
	private void parser(Date date, MarketStatus status, TextNode text_node) {
		ArrayList<String[]> rows = new ArrayList<String[]>();
		for (int index = 1; index < text_node.length(); index++) {
			String[] data = text_node.getStringArray(index);
			if (data[0].equals("2") && (data[2].length() > 0)) {
				String name = data[2].substring(0, data[2].indexOf(" Index"));
				String last = replace(data[4]);
				String change = replace(data[6]);
				String high = replace(data[10]);
				String low = replace(data[12]);
				String volume = replace(data[14]);
				String value = replace(data[16]);
				//NAME, LAST, CHANGE, HIGH, LOW, VOLUME, VALUE
				rows.add(new String[]
						{name, last, change, high, low, volume, value});
			}
		}
		String[][] data_set = rows.toArray(new String[][] {});
		update(date, status, data_set);
		sendMarketEvent(date, status, data_set);
	}
	public static String replace(String data) {
		return data.replace(",", "");
	}
	
	public Date getLastUpdate() {
		synchronized (instance) {
			return last_update;
		}
	}
	private void update(Date date, MarketStatus status, String[][] data_set) {
		synchronized (instance) {
			last_update = date;
			this.status = status;
			this.data_set = data_set;
		}
	}
	public MarketStatus getStatus() {
		synchronized (instance) {
			return status;
		}
	}
	public String[][] getDataSet() {
		synchronized (instance) {
			return data_set;
		}
	}
	
 	public static void main(String[] args) {
		Market stock = Market.getInstance();
		//MarketScreen.getInstance();
		MarketStatusScreen.getInstance();
		
		//StockMethod.getInstance("AOM");
		//
		StockIndustry.getInstance("", "AGRO");
		StockIndustry.getInstance("", "CONSUMP");
		StockIndustry.getInstance("", "FINCIAL");
		StockIndustry.getInstance("", "INDUS");
		StockIndustry.getInstance("", "PROPCON");
		StockIndustry.getInstance("", "RESOURC");
		StockIndustry.getInstance("", "SERVICE");
		StockIndustry.getInstance("", "TECH");
		//
		StockCommand.getInstance("MEDSIZE");
		//
		StockType.getInstance("W");
		StockType.getInstance("V");
		StockType.getInstance("L");
		//
		//StockScreen.getInstance();
		Quote.getInstance();
		//QuoteScreen.getInstance("PTT");
		QuoteFilterChangeLastScreen.getInstance("PTT");
		QuoteFilterChangeVolumeScreen.getInstance("PTT");
		
		stock.initOscillator();
		Oscillator.startAll();
		
		stock.run();
	}
	
}
