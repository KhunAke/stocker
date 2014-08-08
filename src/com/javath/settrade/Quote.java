package com.javath.settrade;

import java.util.Date;
import java.util.EventListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.hibernate.Session;
import org.hibernate.exception.ConstraintViolationException;

import com.javath.logger.LOG;
import com.javath.mapping.SettradeQuote;
import com.javath.mapping.SettradeQuoteHome;
import com.javath.mapping.SettradeQuoteId;
import com.javath.trigger.MulticastEvent;
import com.javath.util.Assign;
import com.javath.util.DateTime;
import com.javath.util.Instance;
import com.javath.util.ObjectException;
import com.javath.util.TaskManager;

public class Quote extends Instance implements QuoteSource, StockListener {
	
	private final static Map<String,SettradeQuote> map_quote;
	private final static Quote instance;
	
	static {
		map_quote = new HashMap<String, SettradeQuote>();
		instance = new Quote();
	}
	
	public static Quote getInstance() {
		return instance;
	}
	
	private final Map<String,Set<QuoteListener>> map_listeners;

	public Quote() {
		map_listeners = new HashMap<String,Set<QuoteListener>>();
		Stock.addListener(this);
	}

	@Override
	public void action(StockEvent event) {
		TaskManager.create(
				String.format("%s.upload(date=\"%s\")", this.getClassName(), DateTime.string(event.getDate())),
				this, "upload", event);
	}
	public void upload(StockEvent event) {
		String[][] data_set = event.getDataSet();
		SettradeQuoteHome home = (SettradeQuoteHome)
				Assign.borrowObject(SettradeQuoteHome.class);
		try {
			Session session = Assign.getSessionFactory().getCurrentSession();
			session.beginTransaction();
			SettradeQuote quote = null;
			try {
				for (int index = 0; index < data_set.length; index++) {
					SettradeQuoteId id = new SettradeQuoteId(
							data_set[index][StockEvent.SYMBOL],
							event.getDate());
					quote = new SettradeQuote(id);
					try {
						quote.setOpen(Double.valueOf(data_set[index][StockEvent.OPEN]));
					} catch (NumberFormatException e) {}
					try {
						quote.setHigh(Double.valueOf(data_set[index][StockEvent.HIGH]));
					} catch (NumberFormatException e) {}
					try {
						quote.setLow(Double.valueOf(data_set[index][StockEvent.LOW]));
					} catch (NumberFormatException e) {}
					try {
						quote.setLast(Double.valueOf(data_set[index][StockEvent.LAST]));
					} catch (NumberFormatException e) {}
					try {
						quote.setChangePrior(Double.valueOf(data_set[index][StockEvent.CHANGE]));
					} catch (NumberFormatException e) {}
					try {
						quote.setBid(Double.valueOf(data_set[index][StockEvent.BID]));
					} catch (NumberFormatException e) {}
					try {
						quote.setOffer(Double.valueOf(data_set[index][StockEvent.OFFER]));
					} catch (NumberFormatException e) {}
					try {
						quote.setVolume(Long.valueOf(data_set[index][StockEvent.VOLUME]));
					} catch (NumberFormatException e) {}
					try {
						quote.setValue(Double.valueOf(data_set[index][StockEvent.VALUE]));
					} catch (NumberFormatException e) {}
					if (quotation(id.getSymbol(), id.getDate(), quote)) {
						send(id.getSymbol(), quote);
						home.persist(quote);
					}
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
	private boolean quotation(String symbol, Date date, SettradeQuote quote) {
		SettradeQuote data = map_quote.get(symbol);
		try {
			Date id_date = data.getId().getDate();
			if (date.before(id_date)) {
				//System.out.printf("%s: %s after %s%n", symbol, DateTime.string(date), DateTime.string(id_date));
				return false;
			} else
				map_quote.put(symbol, quote);
		} catch (NullPointerException e) {
			map_quote.put(symbol, quote);
		}
		return true;
	}
	
	public boolean addSymbolListener(String symbol, QuoteListener listener) {
		Set<QuoteListener> set_listener = map_listeners.get(symbol);
		try {
			return set_listener.add(listener);
		} catch (NullPointerException e) {
			set_listener = new HashSet<QuoteListener>();
			map_listeners.put(symbol, set_listener);
			return set_listener.add(listener);
		}
	}
	public boolean removeSymbolListener(String symbol, QuoteListener listener) {
		Set<QuoteListener> set_listener = map_listeners.get(symbol);
		try {
			return set_listener.remove(listener);
		} catch (NullPointerException e) {
			return false;
		}
	}
	private void send(String symbol, SettradeQuote quote) {
		Set<QuoteListener> set_listener = map_listeners.get(symbol);
		EventListener[] listeners = new EventListener[] {};
		try {
			listeners = set_listener.toArray(new EventListener[] {});
		} catch (NullPointerException e) {}
		try {
			if (listeners.length > 0) { 
				QuoteEvent event = new QuoteEvent(this, quote);
				MulticastEvent.send("action", listeners, event);
			}
		} catch (NoSuchElementException e) {
			throw new ObjectException(e);
		} catch (IllegalStateException e) {
			throw new ObjectException(e);
		}
	}

}
