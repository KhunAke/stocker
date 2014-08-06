package com.javath.settrade;

import java.util.EventObject;

public class SymbolEvent extends EventObject {

	private static final long serialVersionUID = 1L;
	
	private final SettradeBoard board;
	
	public SymbolEvent(Object source, SettradeBoard board) {
		super(source);
		this.board = board;
	}
	
	public SettradeBoard getBoard() {
		return board;
	}
	
}
