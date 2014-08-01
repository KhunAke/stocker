package com.javath.settrade;

public interface BoardSource {
	public boolean addListener(BoardListener listener);
	public boolean removeListener(BoardListener listener);
}
