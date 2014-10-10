package com.javath.settrade;

import java.util.EventListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import com.javath.trigger.MulticastEvent;
import com.javath.util.DateTime;
import com.javath.util.Instance;
import com.javath.util.ObjectException;
import com.javath.util.TaskManager;

public abstract class QuoteFilter extends Instance implements QuoteSource, QuoteListener {
	protected final static Map<String,QuoteFilter> instances;
	static {
		instances = new HashMap<String, QuoteFilter>();
	}
	
	protected final String symbol;
	protected final Set<QuoteListener> listeners;
	protected QuoteFilter(String symbol) {
		this.symbol = symbol;
		listeners = new HashSet<QuoteListener>();
	}
	
	public String getSymbol() {
		return symbol;
	}
	
	@Override
	public void action(QuoteEvent event) {
		TaskManager.create(
				String.format("%s(symbol=\"%s\",date=\"%s\")", 
				this.getClassName(), event.getSymbol(), DateTime.string(event.getDate())),
				this, "run", event);
	}
	public abstract void run(QuoteEvent event);
	
	@Override
	public boolean addSymbolListener(String symbol, QuoteListener listener) {
		if (this.symbol.equals(symbol))
			return listeners.add(listener);
		else 
			return false;
	}
	@Override
	public boolean removeSymbolListener(String symbol, QuoteListener listener) {
		if (this.symbol.equals(symbol))
			return listeners.remove(listener);
		else 
			return false;
	}
	protected void send(QuoteEvent event) {
		try {
			EventListener[] listeners = this.listeners.toArray(new EventListener[] {});
			if (listeners.length > 0) { 
				event.appendFilter(this);
				MulticastEvent.send("action", listeners, event);
			}
		} catch (NoSuchElementException e) {
			throw new ObjectException(e);
		} catch (IllegalStateException e) {
			throw new ObjectException(e);
		}
	}
	
}
