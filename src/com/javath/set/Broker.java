package com.javath.set;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.http.client.CookieStore;
import org.hibernate.Query;
import org.hibernate.Session;

import com.javath.http.Browser;
import com.javath.http.Cookie;
import com.javath.util.Assign;
import com.javath.util.DataSet;
import com.javath.util.DateTime;
import com.javath.util.Instance;

public abstract class Broker extends Instance {

	// Key is username@classname
	private static Map<String,Broker> brokers;
	public final static Broker dummy = new Broker() {
			@Override
			public boolean checkPassword(String password) {
				return true;
			}
			@Override
			protected CookieStore login(Browser browser) {
				return null;
			}
			@Override
			public void save() {}
		};	

	private static void initMapBrokers() {
		DataSet data_set = null;
		String hql ="SELECT broker.classname, broker.username, broker.password " + 
					"FROM SetBroker as broker";
		Session session = Assign.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		try {
			Query query = session.createQuery(hql);
			data_set = new DataSet(query.list());
			session.getTransaction().commit();
		} catch (Exception e) {
			session.getTransaction().rollback();
		}
		brokers = new HashMap<String,Broker>();
		try {
			for (Iterator<Object[]> broker = data_set.iterator(); broker.hasNext();) {
				//Object[] current = broker.next();
				broker.next();
				String classname = data_set.stringField(0);
				String username = data_set.stringField(1);
				String password = Assign.decrypt(data_set.stringField(2));
				Broker object = (Broker) Assign.forMethod(classname, "getBroker", username, password);
				putBroker(object, username);
			}
		} catch (NullPointerException e) {}
	}
		
	protected static Broker getInstance(Class<? extends Broker> classname, String username) {
		try {
			return brokers.get(String.format("%s@%s", username, classname.getCanonicalName()));
		} catch (NullPointerException e) {
			initMapBrokers();
			return brokers.get(String.format("%s@%s", username, classname.getCanonicalName()));
		}
	}
	public static Broker getInstance(Class<? extends Broker> classname, String username, String password) {
		Broker result = getInstance(classname, username);
		try {
			if (result.checkPassword(password))
				return result;
			else
				return dummy;
		} catch (NullPointerException e) {
			return dummy;
		}
	}
	protected static void putBroker(Broker broker, String username) {
		brokers.put(
				String.format("%s@%s", username, broker.getClass().getCanonicalName()), 
				broker);
	}
	
	protected Cookie cookie;
	
	public abstract boolean checkPassword(String password);
	protected CookieStore authentication(Cookie cookie) {
		Browser browser = (Browser) Assign.borrowObject(Browser.class);
		try {
			if (cookie.acquire(true))
				try {
					browser.setCookie(cookie.getCookieStore());
					cookie.setCookieStore(login(browser));
				} finally {
					cookie.release(true);
				}
			return cookie.getCookieStore();
		} finally {
			Assign.returnObject(browser);
		}
	}
	protected abstract CookieStore login(Browser browser);
	// Save to DB
	public abstract void save();
	
}
