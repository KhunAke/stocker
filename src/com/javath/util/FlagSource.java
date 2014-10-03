package com.javath.util;

public interface FlagSource {
	public boolean addListener(FlagListener listener);
	public boolean removeListener(FlagListener listener);
}
