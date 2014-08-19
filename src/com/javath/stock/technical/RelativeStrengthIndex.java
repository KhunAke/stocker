package com.javath.stock.technical;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Query;
import org.hibernate.Session;

import com.javath.util.Assign;
import com.javath.util.DateTime;
import com.javath.util.Instance;

public class RelativeStrengthIndex extends Instance {
	
	private static String hql = "SELECT quote.id.date as date, quote.close as close " +
			"FROM BualuangQuoteDaily as quote " +
			"WHERE quote.id.symbol = :symbol " +
			"AND quote.id.date <= :date " +
			"AND quote.close <> 0 " +
			"ORDER BY date DESC"; 
	
	private final String symbol;
	private final int periods;
	private Object[] data_set;
	private Date date;
	private double value;
	private Map<Date,Double> map_date;
	
	public RelativeStrengthIndex(String symbol, int periods, String date, int records) {
		this.symbol = symbol;
		this.periods = periods;
		map_date = new HashMap<Date, Double>();
		List<?> data_set = null;
		//data_set = null;
		Session session = Assign.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		try {
			Query query = session.createQuery(hql);
			query.setString("symbol", symbol);
			query.setDate("date", DateTime.date(date));
			query.setMaxResults(records);
			data_set = query.list();
			session.getTransaction().commit();
		} catch (Exception e) {
			session.getTransaction().rollback();
		}
		init(data_set.toArray());
	}
	public RelativeStrengthIndex(String symbol, int periods, int records) {
		this(symbol, periods, DateTime.date(new Date()), records);
	}
	public RelativeStrengthIndex(String symbol, int periods, Object[] data_set) {
		this.symbol = symbol;
		this.periods = periods;
		init(data_set);
	}
	private void init(Object[] data_set) {
		Date date = null;
		// first calculations for average gain and average loss are simple N period averages. 
		double sum_gain = 0.0;
		double sum_loss = 0.0;
		double period = (Double) ((Object[]) data_set[data_set.length - 1])[1];
		for (int index = data_set.length - 2; index > data_set.length - periods - 2; index--) {
			date = (Date) ((Object[]) data_set[index])[0];
			Double close = (Double) ((Object[]) data_set[index])[1];
			Double change = close - period;
			period = close;
			int compare =  Double.compare(change, 0.0);
			if (compare < 0)
				sum_loss += (change * (-1.0));
			else if (compare > 0)
				sum_gain += change;
			//System.out.printf("%s,%f,%f%n", DateTime.date(date), close, change);
		}
		double avg_gain = sum_gain / periods;
		double avg_loss = sum_loss / periods;
		double rsi = 100 - (100 / (1 + (avg_gain/avg_loss)));
		map_date.put(date, rsi);
		//System.out.printf("%s,%f,%f%n", DateTime.date(date), period, rsi);
		// calculations are based on the prior averages and the current gain loss
		for (int index = data_set.length - periods - 2; index > -1; index--) {
			date = (Date) ((Object[]) data_set[index])[0];
			Double close = (Double) ((Object[]) data_set[index])[1];
			Double change = close - period;
			period = close;
			int compare =  Double.compare(change, 0.0);
			if (compare < 0) {
				avg_gain = avg_gain * (periods - 1) / periods;
				avg_loss = (avg_loss * (periods - 1) + (change * (-1.0))) / periods;
				sum_loss += (change * (-1.0));
			} else if (compare > 0) {
				avg_gain = (avg_gain * (periods - 1) + change) / periods;
				avg_loss = avg_loss * (periods - 1) / periods;
			} else {
				avg_gain = avg_gain * (periods - 1) / periods;
				avg_loss = avg_loss * (periods - 1) / periods;
			}
			rsi = 100 - (100 / (1 + (avg_gain/avg_loss)));
			map_date.put(date, rsi);
			//System.out.printf("%s,%f,%f%n", DateTime.date(date), period,  rsi);
		}
		this.date = date;
		this.value = rsi;
	}
	
	public String getSymbol() {
		return symbol;
	}
	public Object[] getDataSet() {
		return data_set;
	}
	public Date getDate() {
		return date;
	}
	
	public double getValue() {
		return value;
	}
	public double getValue(String date) {
		return getValue(DateTime.date(date));
	}
	public double getValue(Date date) {
		return map_date.get(date);
	}
	
	public static void main(String[] args) {
		RelativeStrengthIndex RSI = new RelativeStrengthIndex("PTT", 14, "2014-08-18", 150);
		System.out.println(RSI.getValue("2014-08-18"));
		RSI = new RelativeStrengthIndex("PTT", 14, 150);
		System.out.println(RSI.getValue("2014-08-18"));
	}

}
