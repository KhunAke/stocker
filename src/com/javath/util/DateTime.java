package com.javath.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.TimeZone;

import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;

public class DateTime {
	
	/**
	 * @since "epoch" then serves as a reference point from which time is measured.
	 */
	public final static Date EPOCH;
	
	private final static ObjectPool<Calendar> pool;
	public final static String Default_Date_Format;
	public final static String Default_Time_Format;
	private final static String Default_DateTime_Format;
	public final static String Default_Timestamp_Format;
	public final static String Default_Date_Parse;
	public final static String Default_Time_Parse;
	public final static String Default_DateTime_Parse;
	public final static String Default_Timestamp_Parse;
	public final static Locale Default_Locale;
	
	static {
		EPOCH = new Date(0);
		
		pool = new GenericObjectPool<Calendar>(
				new PoolableObjectFactory<Calendar>() {
					@Override
					public Calendar makeObject() 
							throws Exception {
						return Calendar.getInstance();
					}
					@Override
					public void activateObject(Calendar calendar) 
							throws Exception {
						calendar.setTime(new Date());
					}
					@Override
					public void passivateObject(Calendar calendar) 
							throws Exception {}
					@Override
					public boolean validateObject(Calendar calendar) {
						return true;
					}
					@Override
					public void destroyObject(Calendar calendar) 
							throws Exception {}
				});
		Default_Date_Format = "%1$tY-%1$tm-%1$td";
		Default_Time_Format = "%1$tH:%1$tM:%1$tS";
		Default_DateTime_Format = Default_Date_Format + " " + Default_Time_Format;
		Default_Timestamp_Format = Default_Date_Format + "T" + Default_Time_Format + ".%1$tL";
		Default_Date_Parse = "yyyy-MM-dd";
		Default_Time_Parse = "HH:mm:ss";
		Default_DateTime_Parse = Default_Date_Parse + " " + Default_Time_Parse;
		Default_Timestamp_Parse = Default_Date_Parse + "'T'" + Default_Time_Parse + ".SSS";
		Default_Locale = Locale.US;
	}
	
	public static Calendar borrowCalendar() {
		try {
			return pool.borrowObject();
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
			calendar.clear();
			pool.returnObject(calendar);
		} catch (Exception e) {
			throw new ObjectException(e);
		}
	}
	
	public static Date splitTime(Date time) {
		Calendar calendar = borrowCalendar();
		calendar.setTime(time);
		calendar.set(Calendar.DAY_OF_MONTH, 0);
		calendar.set(Calendar.MONTH, 0);
		calendar.set(Calendar.YEAR, 0);
		Date result = calendar.getTime();
		returnCalendar(calendar);
		return result;
	}
	public static Date splitTime(long time) {
		Calendar calendar = borrowCalendar();
		calendar.setTimeInMillis(time);
		calendar.set(Calendar.DAY_OF_MONTH, 0);
		calendar.set(Calendar.MONTH, 0);
		calendar.set(Calendar.YEAR, 0);
		Date result = calendar.getTime();
		returnCalendar(calendar);
		return result;
	}
	public static Date splitDate(Date date) {
		Calendar calendar = borrowCalendar();
		calendar.setTime(date);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		Date result = calendar.getTime();
		returnCalendar(calendar);
		return result;
	}
	public static Date splitDate(long date) {
		Calendar calendar = borrowCalendar();
		calendar.setTimeInMillis(date);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		Date result = calendar.getTime();
		returnCalendar(calendar);
		return result;
	}
	public static Date merge(Date date, Date time) {
		return merge(date.getTime(), time.getTime());
	}
	public static Date merge(long date, long time) {
		Calendar calendar = borrowCalendar();
		TimeZone  timezone = calendar.getTimeZone();
		returnCalendar(calendar);
		return new Date(date + time + (long) timezone.getRawOffset());
	}
	
	public static long differenceDate(Date arg1, Date arg2) {
		long date1 = arg1.getTime() / (86400000);
		long date2 = arg2.getTime() / (86400000);
		return date1 - date2;
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
	
	// Method name : string -> using "Default_DateTime_[Format|Parse]"
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

	// Method name : date -> using "Default_Date_[Format|Parse]"
	public static Date date() {
		Calendar calendar = borrowCalendar();
		//calendar.setTime(new Date());
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

	// Method name : time -> using "Default_Time_[Format|Parse]"
	public static Date time() {
		Calendar calendar = borrowCalendar();
		//calendar.setTime(new Date());
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
	
	// Method name : timestamp -> using "Default_Timestamp_[Format|Parse]"
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
