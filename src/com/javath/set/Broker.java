package com.javath.set;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.CookieStore;
import org.hibernate.Query;
import org.hibernate.Session;

import com.javath.http.Browser;
import com.javath.http.Cookie;
import com.javath.mapping.SetBroker;
import com.javath.mapping.SetBrokerHome;
import com.javath.mapping.SetBrokerId;
import com.javath.mapping.SetExtendsBroker;
import com.javath.mapping.SetExtendsBrokerHome;
import com.javath.settrade.flash.DataProvider;
import com.javath.util.Assign;
import com.javath.util.DataSet;
import com.javath.util.Instance;

public abstract class Broker extends Instance {
	
	// Key is username@extend_id
	private static Map<String,Broker> brokers;
	
	static {
		/**
		brokers = new HashMap<String,Broker>();
		DataSet data_set = loadBrokers();
		while(data_set.hasNext()) {
			data_set.next();
			String classname = data_set.fieldString(0);
			String username = data_set.fieldString(1);
			String password = Assign.decrypt(data_set.fieldString(2));
			Broker broker = (Broker) Assign.forConstructor(classname, username, password);
			brokers.put(String.format("%s@%d", username, getExtendId(classname)) , broker);
		}
		/**/
	}
	private static DataSet loadBrokers() {
		Session session = Assign.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		try {
			Query query = session.createQuery(
					"SELECT " + 
					"extends.classname as classname, " +
					"broker.id.username as username, " +
					"broker.password as password " + 
					"FROM SetBroker as broker, SetExtendsBroker as extends " +
					"WHERE broker.id.extendId = extends.id");
			DataSet data_set = new DataSet(query.list());
			session.getTransaction().commit();
			return data_set;
		} catch (Exception e) {
			session.getTransaction().rollback();
			throw e;
		}
	}
	
	protected static int getExtendId(String classname) {
		Session session = Assign.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		try {
			Query query = session.createQuery(
					"select broker.id as id " + 
					"from SetExtendsBroker as broker " +
					"where broker.classname = :classname");
			query.setString("classname", classname);
			Integer id = (Integer) query.uniqueResult();
			if (id == null) {
				SetExtendsBrokerHome home = (SetExtendsBrokerHome) 
					Assign.borrowObject(SetExtendsBrokerHome.class);
				try {
					SetExtendsBroker broker = new SetExtendsBroker(classname);
					home.attachDirty(broker);
				} catch (Exception e) {
					throw e;
				} finally {
					id = (Integer) query.uniqueResult();
					Assign.returnObject(home);
				}	
			}
			session.getTransaction().commit();
			return id;
		} catch (Exception e) {
			session.getTransaction().rollback();
			throw e;
		}
	}
	
	protected static Broker getBroker(String username, int extend_id) {
		try {
			return brokers.get(String.format("%s@%d", username, extend_id));
		} catch (NullPointerException e) {
			brokers = new HashMap<String,Broker>();
			DataSet data_set = loadBrokers();
			while(data_set.hasNext()) {
				data_set.next();
				String _classname = data_set.fieldString(0);
				String _username = data_set.fieldString(1);
				String _password = Assign.decrypt(data_set.fieldString(2));
				Broker broker = (Broker) Assign.forConstructor(_classname, _username, _password);
				brokers.put(String.format("%s@%d", username, broker.getExtendId()) , broker);
			}
			return brokers.get(String.format("%s@%d", username, extend_id));
		}
	}
	
	protected String username;
	protected String password;
	protected final Cookie cookie;
	
	protected Broker() {
		cookie = new Cookie();
	}
	protected void initBroker(String username, String password) {
		this.username = username;
		this.password = password;
		INFO("Initial Broker: %s", this);
	}
	protected abstract int getExtendId();
	public final boolean checkPassword(String password) {
		return this.password.equals(password);
	}
	protected void authentication(Browser browser, Cookie cookie) {
		if (cookie.acquire(true))
			try {
				if (browser.getCookie().equals(cookie.getCookieStore()))
					cookie.setCookieStore(login(browser));
				else
					browser.setCookie(cookie.getCookieStore());
			} finally {
				cookie.release(true);
			}
		else
			browser.setCookie(cookie.getCookieStore());
	}
	protected abstract CookieStore login(Browser browser);
	public final void save() {
		Session session = Assign.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		try {
			SetBrokerHome home = (SetBrokerHome) 
				Assign.borrowObject(SetBrokerHome.class);
			SetBrokerId id = new SetBrokerId(this.getExtendId(), username);
			SetBroker broker;
			try {
				broker = home.findById(id);
				broker.setPassword(Assign.encrypt(password));
				home.attachDirty(broker);
				session.getTransaction().commit();
			} catch (NullPointerException e) {
				broker = new SetBroker(id, Assign.encrypt(password));
				home.attachDirty(broker);
				session.getTransaction().commit();
			}
		} catch (Exception e) {
			session.getTransaction().rollback();
			throw e;
		}		
	}
	
	public abstract long buy(String symbol, double price, long volume);
	public abstract long sell(String symbol, double price, long volume);
	public abstract boolean cancel(String symbol, long order_no);
	
	@Override
	public String toString() {
		return String.format("%s[username=\"%s\"]",
				getClassName(), username);
	}
}
