package com.javath.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.NoSuchElementException;

import org.apache.commons.pool.BasePoolableObjectFactory;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.SoftReferenceObjectPool;

public class DateTime {
	
	/**
	 * @since "epoch" then serves as a reference point from which time is measured.
	 */
	public final static Date EPOCH;
	
	private final static ObjectPool<Calendar> pool;
	private final static String Default_Date_Format;
	private final static String Default_Time_Format;
	private final static String Default_DateTime_Format;
	private final static String Default_Timestamp_Format;
	private final static String Default_Date_Parse;
	private final static String Default_Time_Parse;
	private final static String Default_DateTime_Parse;
	private final static String Default_Timestamp_Parse;
	private final static Locale Default_Locale;
	
	static {
		EPOCH = new Date(0);
		pool = new SoftReferenceObjectPool<Calendar>(
				new BasePoolableObjectFactory<Calendar>() { 
				    // for makeObject we'll simply return a new buffer 
				    public Calendar makeObject() { 
				        return Calendar.getInstance(); 
				    } 
				     
				    // when an object is returned to the pool,  
				    // we'll clear it out 
				    public void passivateObject(Calendar calendar) { 
				    	calendar.clear();
				    } 
				     
				    // for all other methods, the no-op  
				    // implementation in BasePoolableObjectFactory 
				    // will suffice 
				});
		Default_Date_Format = "%1$tY-%1$tm-%1$td";
		Default_Time_Format = "%1$tH:%1$tM:%1$tS";
		Default_DateTime_Format = Default_Date_Format + " " + Default_Time_Format;
		Default_Timestamp_Format = Default_Date_Format + "T" + Default_Time_Format;
		Default_Date_Parse = "yyyy-MM-dd";
		Default_Time_Parse = "HH:mm:ss";
		Default_DateTime_Parse = Default_Date_Parse + " " + Default_Time_Parse;
		Default_Timestamp_Parse = Default_Date_Parse + "'T'" + Default_Time_Parse;
		Default_Locale = Locale.US;
	}
	
	public static Calendar borrowCalendar() {
		try {
			Calendar calendar = pool.borrowObject();
			calendar.setTime(new Date());
			return calendar;
		} catch (NoSuchElementException e) {
			throw new ObjectException(e);
		} catch (IllegalStateException e) {
			throw new ObjectException(e);
		} catch (Exception e) {
			throw new ObjectException(e);
		}
	}
	public static void returnCalendar(Calendar calendar) {
		try {
			pool.returnObject(calendar);
		} catch (Exception e) {
			throw new ObjectException(e);
		}
	}
	
	// Method name : format
	public static Date format(String format, String datetime) {
		return format(Default_Locale, format, datetime);
	}
	public static Date format(Locale locale, String format, String datetime) {
		Date result;
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat(format, locale);
			result = dateFormat.parse(datetime);
		} catch (ParseException e) {
			throw new ObjectException(e);
		}
		return result;
	}
	
	public static String format(String format, Date datetime) {
		return format(Default_Locale , format, datetime);
	}
	public static String format(Locale locale, String format, Date datetime) {
		return String.format(locale, format, datetime);
	}
	
	public static String format(String format, long datetime) {
		return format(Default_Locale , format, datetime);
	}
	public static String format(Locale locale, String format, long datetime) {
		return String.format(locale, format, datetime);
	}
	
	public static String format(String format, Calendar datetime) {
		return format(Default_Locale, format, datetime.getTimeInMillis());
	}
	public static String format(Locale locale, String format, Calendar datetime) {
		return format(locale, format, datetime.getTimeInMillis());
	}
	
	// Method name : string
	public static Date string(String datetime) {
		return string(Default_Locale, datetime);
	}
	public static Date string(Locale locale, String datetime) {
		return format(locale, Default_DateTime_Parse, datetime);
	}
	
	public static String string(Date datetime) {
		return string(Default_Locale, datetime);
	}
	public static String string(Locale locale, Date datetime) {
		return format(locale, Default_DateTime_Format, datetime);
	}
	
	public static String string(long datetime) {
		return string(Default_Locale, datetime);
	}
	public static String string(Locale locale, long datetime) {
		return format(locale, Default_DateTime_Format, datetime);
	}
	
	public static String string(Calendar datetime) {
		return string(Default_Locale, datetime.getTimeInMillis());
	}
	public static String string(Locale locale, Calendar datetime) {
		return format(locale, Default_DateTime_Format, datetime.getTimeInMillis());
	}

	// Method name : date
	public static Date date() {
		Calendar calendar = borrowCalendar();
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		Date result = calendar.getTime();
		returnCalendar(calendar);
		return result;
	}
	public static Date date(String datetime) {
		return date(Default_Locale, datetime);
	}
	public static Date date(Locale locale, String datetime) {
		return format(locale, Default_Date_Parse, datetime);
	}
	
	public static String date(Date datetime) {
		return date(Default_Locale, datetime);
	}
	public static String date(Locale locale, Date datetime) {
		return format(locale, Default_Date_Format, datetime);
	}
	
	public static String date(long datetime) {
		return date(Default_Locale, datetime);
	}
	public static String date(Locale locale, long datetime) {
		return format(locale, Default_Date_Format, datetime);
	}
	
	public static String date(Calendar datetime) {
		return date(Default_Locale, datetime.getTimeInMillis());
	}
	public static String date(Locale locale, Calendar datetime) {
		return format(locale, Default_Date_Format, datetime.getTimeInMillis());
	}

	// Method name : time
	public static Date time() {
		Calendar calendar = borrowCalendar();
		calendar.set(Calendar.DAY_OF_MONTH, 0);
		calendar.set(Calendar.MONTH, 0);
		calendar.set(Calendar.YEAR, 0);
		Date result = calendar.getTime();
		returnCalendar(calendar);
		return result;
	}
	public static Date time(String datetime) {
		return time(Default_Locale, datetime);
	}
	public static Date time(Locale locale, String datetime) {
		return format(locale, Default_Time_Parse, datetime);
	}
		
	public static String time(Date datetime) {
		return time(Default_Locale, datetime);
	}
	public static String time(Locale locale, Date datetime) {
		return format(locale, Default_Time_Format, datetime);
	}
	
	public static String time(long datetime) {
		return time(Default_Locale, datetime);
	}
	public static String time(Locale locale, long datetime) {
		return format(locale, Default_Time_Format, datetime);
	}
	
	public static String time(Calendar datetime) {
		return time(Default_Locale, datetime.getTimeInMillis());
	}
	public static String time(Locale locale, Calendar datetime) {
		return format(locale, Default_Time_Format, datetime.getTimeInMillis());
	}
	
	// Method name : timestamp
	public static Date timestamp() {
		return new Date();
	}
	public static Date timestamp(String datetime) {
		return timestamp(Default_Locale, datetime);
	}
	public static Date timestamp(Locale locale, String datetime) {
		return format(locale, Default_Timestamp_Parse, datetime);
	}
	
	public static String timestamp(Date datetime) {
		return timestamp(Default_Locale, datetime);
	}
	public static String timestamp(Locale locale, Date datetime) {
		return format(locale, Default_Timestamp_Format, datetime);
	}
	
	public static String timestamp(long datetime) {
		return timestamp(Default_Locale, datetime);
	}
	public static String timestamp(Locale locale, long datetime) {
		return format(locale, Default_Timestamp_Format, datetime);
	}
	
	public static String timestamp(Calendar datetime) {
		return timestamp(Default_Locale, datetime.getTimeInMillis());
	}
	public static String timestamp(Locale locale, Calendar datetime) {
		return format(locale, Default_Timestamp_Format, datetime.getTimeInMillis());
	}
	
}
