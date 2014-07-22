package com.javath.settrade;

import java.util.Date;

import com.javath.logger.LOG;
import com.javath.util.DateTime;

public enum MarketStatus {
	/**
	 * time = H * 3600 + M * 60 + S
	 * 00:00 - 09:30 
	 * 09:30 - T1    Pre-Open(I)
	 *    T1 - 12:30 Open(I)
	 * 12:30 - 14:00 Intermission
	 * 14:00 - T2    Pre-Open(II)
	 *    T2 - 16:30 Open(II)
	 * 16:30 - T3    Pre-close
	 *    T3 - 17:00 OffHour
	 * 17:00 - 23:59 Closed
	 */
	Empty ("00:00:00:0", "09:30:00:0"),
	PreOpen_I ("09:30:00:0", "T1"),
	Open_I ("T1", "12:30:00:0"),
	Intermission ("12:30:00:0", "14:00:00:0"),
	PreOpen_II ("14:00:00:0", "T2"),
	Open_II ("T2", "16:30:00:0"),
	PreClose ("16:30:00:0", "T3"),
	OffHour ("T3", "17:00:00:0"),
	Closed ("17:00:00:0", "23:59:59:999"),
	Unknow ("00:00:00:0", "23:59:59:999");
	
	private long begin = 0;
	private long end = 0;
	private int range = 0;
	
	private MarketStatus(String begin, String end) {
		if (begin.charAt(0) == 'T')
			range = Integer.valueOf(begin.substring(1));
		else
			this.begin = timestamp(begin);
		if (end.charAt(0) == 'T')
			range = Integer.valueOf(end.substring(1));
		else
			this.end = timestamp(end);
    }
	private long timestamp(String date) {
		return DateTime.time(date).getTime();
	}
	
	public long getBegin(Date date) { 
		if (begin == 0 && range != 0)
			throw new RuntimeException(
					String.format("Market Status at {T%d}", range));
		return ((date.getTime() / 86400000) * 86400000) + begin;
	}
	public long getEnd(Date date) {
		if (end == 0 && range != 0)
			throw new RuntimeException(
					String.format("Market Status at {T%d}", range));
		return ((date.getTime() / 86400000) * 86400000) + end;
	}
	public int getRange() {
		return range;
	}
	
	public static MarketStatus getStatus(String status) {
		if (status.equals("Pre-Open1"))
			return PreOpen_I;
		else if (status.equals("Open(I)"))
			return Open_I;
		else if (status.equals("Intermission"))
			return Intermission;
		else if (status.equals("Pre-Open2"))
			return PreOpen_II;
		else if (status.equals("Open(II)"))
			return Open_II;
		else if (status.equals("Pre-close"))
			return PreClose;
		else if (status.equals("OffHour"))
			return OffHour;
		else if (status.equals("Closed"))
			return Closed;
		else if (status.equals("")) 
			return Empty;
		else {
			LOG.SEVERE("Unknow \"%s\"",status);
			return Unknow;
		}
	}
	
}
