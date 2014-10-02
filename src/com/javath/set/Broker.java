package com.javath.set;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.CookieStore;
import org.hibernate.Query;
import org.hibernate.Session;

import com.javath.http.Browser;
import com.javath.http.Cookie;
import com.javath.mapping.SetAccount;
import com.javath.mapping.SetAccountHome;
import com.javath.mapping.SetBroker;
import com.javath.mapping.SetBrokerHome;
import com.javath.mapping.SetPlan;
import com.javath.util.Assign;
import com.javath.util.DataSet;
import com.javath.util.Instance;

public abstract class Broker extends Instance {
	
	// Key is username@broker_id
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
					"account.id as id, " +
					"broker.classname as classname, " +
					"account.username as username, " +
					"account.password as password " +
					"FROM SetAccount as account, SetBroker as broker " +
					"WHERE account.brokerId = broker.id");
			DataSet data_set = new DataSet(query.list());
			session.getTransaction().commit();
			return data_set;
		} catch (Exception e) {
			session.getTransaction().rollback();
			throw e;
		}
	}
	
	protected static int getBrokerId(String classname) {
		Session session = Assign.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		try {
			Query query = session.createQuery(
					"SELECT broker.id as id " + 
					"FROM SetBroker as broker " +
					"WHERE broker.classname = :classname");
			query.setString("classname", classname);
			Integer id = (Integer) query.uniqueResult();
			if (id == null) {
				SetBrokerHome home = (SetBrokerHome) 
					Assign.borrowObject(SetBrokerHome.class);
				try {
					SetBroker broker = new SetBroker(classname);
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
	protected static Broker getBroker(String username, int broker_id) {
		try {
			return brokers.get(String.format("%s@%d", username, broker_id));
		} catch (NullPointerException e) {
			brokers = new HashMap<String,Broker>();
			DataSet data_set = loadBrokers();
			while(data_set.hasNext()) {
				data_set.next();
				int _id = data_set.fieldInteger(0);
				String _classname = data_set.fieldString(1);
				String _username = data_set.fieldString(2);
				String _password = Assign.decrypt(data_set.fieldString(3));
				Broker broker = (Broker) Assign.forConstructor(_classname, _username, _password);
				broker.setAccountId(_id);
				brokers.put(String.format("%s@%d", username, broker.getBrokerId()) , broker);
			}
			return brokers.get(String.format("%s@%d", username, broker_id));
		}
	}
	
	private int account_id;
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
	private void setAccountId(int id) {
		this.account_id = id;
		DataSet data_set = loadPlans();
		while(data_set.hasNext()) {
			data_set.next();
			int _id = data_set.fieldInteger(0);
			String _classname = data_set.fieldString(1);
			SetPlan _set_plan = (SetPlan) data_set.fieldObject(2);
			Plan plan = (Plan) Assign.forConstructor(_classname, String.valueOf(_id));
			plan.setBroker(this);
			plan.setSymbol(_set_plan.getSymbol());
		}
	}
	protected abstract int getBrokerId();
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
	private DataSet loadPlans() {
		Session session = Assign.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		try {
			Query query = session.createQuery(
					"SELECT " +
					"plan.id as id, " +
					"strategy.classname as classname, " +
					"plan as plan " +
					"FROM SetPlan as plan, SetAccount as account, SetStrategy as strategy " +
					"WHERE plan.strategyId = strategy.id " +
					"AND plan.accountId = :account_id");
			query.setInteger("account_id", account_id);
			DataSet data_set = new DataSet(query.list());
			session.getTransaction().commit();
			return data_set;
		} catch (Exception e) {
			session.getTransaction().rollback();
			throw e;
		}
	}
	public final void save() {
		Session session = Assign.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		try {
			SetAccountHome home = (SetAccountHome) 
				Assign.borrowObject(SetAccountHome.class);
			//SetBrokerId id = new SetBrokerId(this.getExtendId(), username);
			SetAccount account;
			try {
				account = home.findById(account_id);
				account.setPassword(Assign.encrypt(password));
				home.attachDirty(account);
				session.getTransaction().commit();
			} catch (NullPointerException e) {
				account = new SetAccount(this.getBrokerId(), username, Assign.encrypt(password));
				account = home.merge(account);
				this.setAccountId(account.getId());
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
