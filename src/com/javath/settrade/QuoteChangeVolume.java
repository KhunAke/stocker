package com.javath.settrade;

import java.util.EventListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import com.javath.mapping.SettradeQuote;
import com.javath.trigger.MulticastEvent;
import com.javath.util.DateTime;
import com.javath.util.FlagEvent;
import com.javath.util.FlagListener;
import com.javath.util.FlagSource;
import com.javath.util.Instance;
import com.javath.util.ObjectException;
import com.javath.util.TaskManager;

public class QuoteChangeVolume extends Instance implements FlagSource, QuoteSource, QuoteListener {
	
	private final static Map<String,QuoteChangeVolume> instances;
	
	static {
		instances = new HashMap<String, QuoteChangeVolume>();
	}
	
	public static QuoteChangeVolume getInstance(String symbol) {
		QuoteChangeVolume result = instances.get(symbol);
		if (result == null) {
			result = new QuoteChangeVolume(symbol);
			instances.put(symbol, result);
		}
		return result;
	}
	
	private final String symbol;
	private final Set<FlagListener> flag_listeners;
	private final Set<QuoteListener> quote_listeners;
	private SettradeQuote data;
	
	private QuoteChangeVolume(String symbol) {
		this.symbol = symbol;
		flag_listeners = new HashSet<FlagListener>();
		quote_listeners = new HashSet<QuoteListener>();
		Quote.getInstance().addSymbolListener(symbol, this);
	}

	@Override
	public void action(QuoteEvent event) {
		TaskManager.create(
				String.format("%s(symbol=\"%s\",date=\"%s\")", 
				this.getClassName(), event.getSymbol(), DateTime.string(event.getDate())),
				this, "run", event);
	}
	public void run(QuoteEvent event) {
		SettradeQuote quote = event.getQuote();
		try {
			if (!quote.getVolume().equals(data.getVolume())) {
				send(event);
				send(new FlagEvent(this, true));
			} else
				send(new FlagEvent(this, false));
		} catch (NullPointerException e) {
			send(event);
			send(new FlagEvent(this, true));
		}
		data = quote;
	}
	
	public String getSymbol() {
		return symbol;
	}
	
	@Override
	public boolean addListener(FlagListener listener) {
		return flag_listeners.add(listener);
	}
	@Override
	public boolean removeListener(FlagListener listener) {
		return flag_listeners.remove(listener);
	}
	private void send(FlagEvent event) {
		try {
			EventListener[] listeners = flag_listeners.toArray(new EventListener[] {});
			System.out.printf("%s,%b%n", 
					((QuoteChangeVolume) event.getSource()).getSymbol(), event.getBoolean());
			if (listeners.length > 0) { 
				MulticastEvent.send("action", listeners, event);
			}
		} catch (NoSuchElementException e) {
			throw new ObjectException(e);
		} catch (IllegalStateException e) {
			throw new ObjectException(e);
		}
	}

	@Override
	public boolean addSymbolListener(String symbol, QuoteListener listener) {
		if (this.symbol.equals(symbol))
			return quote_listeners.add(listener);
		else 
			return false;
	}
	@Override
	public boolean removeSymbolListener(String symbol, QuoteListener listener) {
		if (this.symbol.equals(symbol))
			return quote_listeners.remove(listener);
		else 
			return false;
	}
	private void send(QuoteEvent event) {
		try {
			EventListener[] listeners = quote_listeners.toArray(new EventListener[] {});
			if (listeners.length > 0) { 
				MulticastEvent.send("action", listeners, event);
			}
		} catch (NoSuchElementException e) {
			throw new ObjectException(e);
		} catch (IllegalStateException e) {
			throw new ObjectException(e);
		}
	}

}
