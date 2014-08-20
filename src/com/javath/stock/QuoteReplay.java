package com.javath.stock;

import java.util.Date;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;

import com.javath.mapping.SettradeQuote;
import com.javath.mapping.SettradeQuoteId;
import com.javath.util.Assign;
import com.javath.util.DateTime;
import com.javath.util.Instance;

public class QuoteReplay extends Instance {
	
	private static String hql = 
			"FROM SettradeQuote as quote " +
			"WHERE quote.id.symbol = :symbol " +
			"AND quote.id.date BETWEEN :begin_date AND :end_date " +
			"AND quote.last != null " +
			"ORDER BY date";
	
	private SettradeQuote[] quotation;
	
	public QuoteReplay(String symbol, Date date) {
		Session session = Assign.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		List<?> list = null;
		try {
			Query query = session.createQuery(hql);
			query.setString("symbol", "PTT");
			query.setString("begin_date", String.format("%s 00:00:00.000", DateTime.date(date)));
			query.setString("end_date", String.format("%s 23:59:59.999", DateTime.date(date)));
			//query.setMaxResults(records);
			list = query.list();
			quotation = list.toArray(new SettradeQuote[] {});
			session.getTransaction().commit();
		} catch (Exception e) {
			session.getTransaction().rollback();
		}
	}
	
	public void play() {
		long prior = 0;
		for (int index = 0; index < quotation.length; index++) {
			SettradeQuote quote = quotation[index];
			SettradeQuoteId id = quote.getId();
			long volume = quote.getVolume();
			if (prior != volume) {
				System.out.printf("%s,%f,%d%n",
						DateTime.string(id.getDate()), quote.getLast(), volume);
				prior = volume;
			}
		}
	}
	
	public static void main(String[] args) {
		QuoteReplay replay = new QuoteReplay("PTT", DateTime.date("2014-08-18"));
		replay.play();
	}
	
}
