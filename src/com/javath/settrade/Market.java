package com.javath.settrade;

import java.text.ParseException;
import java.util.Date;
import java.util.EventListener;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.w3c.dom.Node;

import com.javath.bualuang.BoardDaily;
import com.javath.html.CustomFilter;
import com.javath.html.CustomHandler;
import com.javath.html.HtmlParser;
import com.javath.html.TextNode;
import com.javath.http.Browser;
import com.javath.http.Cookie;
import com.javath.http.Response;
import com.javath.trigger.MulticastEvent;
import com.javath.trigger.Oscillator;
import com.javath.trigger.OscillatorDivideFilter;
import com.javath.trigger.OscillatorEvent;
import com.javath.trigger.OscillatorLoader;
import com.javath.util.Assign;
import com.javath.util.DateTime;
import com.javath.util.Instance;
import com.javath.util.NotificationAdaptor;
import com.javath.util.ObjectException;
import com.javath.util.TaskManager;

public class Market extends Instance implements OscillatorLoader, CustomHandler, Runnable {
	
	private final static Assign assign;
	private final static String storage_path;
	private final static String market_page;
	
	static {
		String default_Properties = Assign.etc + Assign.File_Separator +
				"settrade.properties";
		assign = Assign.getInstance(Market.class, default_Properties);
		String default_path = Assign.temp + Assign.File_Separator + "settrade";
		storage_path = assign.getProperty("storage_path", default_path);
		market_page = assign.getProperty("market_page",
				"http://www.settrade.com/C13_MarketSummary.jsp?detail=SET");
	}

	private final Cookie cookie;
	private OscillatorDivideFilter oscillator;
	
	private final Set<MarketListener> market_listeners;
	private final Set<MarketStatusListener> status_listeners;
	
	private Date last_update;
	private MarketStatus status;
	
	private Market() {
		cookie = new Cookie();
		// 
		market_listeners = new HashSet<MarketListener>();
		status_listeners = new HashSet<MarketStatusListener>();
	}
	
	public boolean addMarketListener(MarketListener listener) {
		return market_listeners.add(listener);
	}
	public boolean removeMarketListener(MarketListener listener) {
		return market_listeners.remove(listener);
	}
	public boolean addStatusListener(MarketStatusListener listener) {
		return status_listeners.add(listener);
	}
	public boolean removeStatusListener(MarketStatusListener listener) {
		return status_listeners.remove(listener);
	}
	
	@Override
	public void initOscillator() {
		if (oscillator != null)
			return;
		long clock = assign.getLongProperty("clock", 1000); // 1s
		Oscillator source = Oscillator.getInstance(clock);
		long date = new Date().getTime();
		long time = DateTime.time(
				assign.getProperty("schedule", "19:30:00")).getTime();
		//System.out.printf("Schedule: \"%s\"%n", DateTime.timestamp(time));
		long datetime = DateTime.merge(date, time).getTime();
		//System.out.printf("Schedule: \"%s\"%n", DateTime.timestamp(datetime));
		long try_again = (long) Math.ceil(assign.getLongProperty("try_again", clock * 5) / (double) clock);
		if (try_again == 0)
			try_again = 1;
		oscillator = new OscillatorDivideFilter(source, this, try_again, datetime);
	}
	@Override
	public void action(OscillatorEvent event) {
		TaskManager.create(
				String.format("%s[timestamp=%d]", 
						this.getClassName(), event.getTimestamp()), 
				this);
	}
	
	@Override
	public void run() {
		Response response = getWebPage();
		response.printHeaders();
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
		return "http://www.settrade.com/C13_MarketSummary.jsp?detail=SET";
	}

	
	
	private void parser(Response response) {
		HtmlParser parser = new HtmlParser(response.getContent(), response.getCharset());
		CustomFilter filter = new CustomFilter(parser.parse());
		filter.setHandler(this);
		List<Node> nodes = filter.filter(6);
		
		TextNode text = null;
		try {
			text = new TextNode(nodes.get(0));
		} catch (IndexOutOfBoundsException e) {
			throw new ObjectException(e);
		}
		
		Date date = null;
		try {
			date = DateTime.format("ข้อมูลล่าสุด dd/MM/yyyy HH:mm:ss", text.getString(0, 2));
		} catch (ObjectException e) {
			if (e.getCause() instanceof ParseException) {
				WARNING(e.getMessage());
				date = DateTime.date();
			} else {
				throw e;
			}
		}
		
		synchronized(last_update) {
			if (date.after(last_update)) {
				last_update = date;
				MarketStatus status = MarketStatus.getStatus(text.getString(8, 4));
				if (!status.equals(this.status)) {
					INFO("Status at %s is \"%s\"", DateTime.timestamp(date), status);
					this.status = status;
					//sendStatusEvent();
				}
				//assignNextSchedule();
				//updateRows(text);
				//sendMarketEvent();
				//printRows();
			} else if (date.before(last_update)) { //before(when)
				WARNING("Server delayed because request of \"%s\" but received after \"%s\"", 
						DateTime.string(date), DateTime.string(last_update));
			}
		}
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
	private void assignNextSchedule() {
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
			timestamp = last_update.getTime() + interval_update;
			break;
		case Intermission:
			timestamp = MarketStatus.PreOpen_II.getBegin(new Date());
			break;
		case Closed:
			long update = last_update.getTime() % 86400000;
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
			timestamp = last_update.getTime() + interval_update;
			WARNING("Unknow Status at \"%s\"", DateTime.string(last_update));
			break;
		}
		FINE("Next schedule at \"%s\"", DateTime.timestamp(timestamp));
		oscillator.setSchedule(timestamp);
	}
	private void sendMarketEvent() {
		try {
			EventListener[] listeners = new EventListener[] {};
			listeners = this.market_listeners.toArray(listeners);
			if (listeners.length > 0) { 
				MarketEvent event = new MarketEvent(this, last_update, status, rows);
				MulticastEvent.send("action", listeners, event);
			}
		} catch (NoSuchElementException e) {
			throw new ObjectException(e);
		} catch (IllegalStateException e) {
			throw new ObjectException(e);
		}
	}
	private void sendStatusEvent() {
		try {
			EventListener[] listeners = new EventListener[] {};
			listeners = this.status_listeners.toArray(listeners);
			if (listeners.length > 0) { 
				MarketStatusEvent event = new MarketStatusEvent(this, last_update, status);
				MulticastEvent.send("action", listeners, event);
			}
		} catch (NoSuchElementException e) {
			throw new ObjectException(e);
		} catch (IllegalStateException e) {
			throw new ObjectException(e);
		}
	}
	
	
	public static void main(String[] args) {
		Market stock = new Market();
		stock.run();
	}
	
}
