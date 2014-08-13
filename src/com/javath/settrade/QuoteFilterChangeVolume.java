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
import com.javath.util.Instance;
import com.javath.util.ObjectException;
import com.javath.util.TaskManager;

public class QuoteFilterChangeVolume extends Instance implements QuoteSource, QuoteListener {
	
	private final static Map<String,QuoteFilterChangeVolume> instances;
	
	static {
		instances = new HashMap<String, QuoteFilterChangeVolume>();
	}
	
	public static QuoteFilterChangeVolume getInstance(String symbol) {
		QuoteFilterChangeVolume result = instances.get(symbol);
		if (result == null) {
			result = new QuoteFilterChangeVolume(symbol);
			instances.put(symbol, result);
		}
		return result;
	}

	private final Set<QuoteListener> listeners;
	private SettradeQuote data;
	
	private QuoteFilterChangeVolume(String symbol) {
		listeners = new HashSet<QuoteListener>();
		//
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
				data = quote;
				send(event);
			}
		} catch (NullPointerException e) {
			data = quote;
			send(event);
		}
	}

	@Override
	public boolean addSymbolListener(String symbol, QuoteListener listener) {
		return listeners.add(listener);
	}
	@Override
	public boolean removeSymbolListener(String symbol, QuoteListener listener) {
		return listeners.remove(listener);
	}
	private void send(QuoteEvent event) {
		try {
			EventListener[] listeners = this.listeners.toArray(new EventListener[] {});
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
