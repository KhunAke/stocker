package com.javath.set.company;

import java.util.EventObject;

public abstract class QuoteEvent extends EventObject {

	private static final long serialVersionUID = 1L;

	public QuoteEvent(Object source) {
		super(source);
	}
	
}
