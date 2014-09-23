package com.javath.set;

import java.util.HashMap;
import java.util.Map;

import org.hibernate.Query;
import org.hibernate.Session;

import com.javath.mapping.SetExtendsBroker;
import com.javath.mapping.SetExtendsBrokerHome;
import com.javath.util.Assign;
import com.javath.util.DataSet;
import com.javath.util.Instance;

public abstract class Broker extends Instance {
	
	// Key is username@extend_id
	private static Map<String,Broker> brokers = new HashMap<String,Broker>();
	
	static {
		brokers = new HashMap<String,Broker>();
		DataSet data_set = loadBrokers();
		while(data_set.hasNext()) {
			data_set.next();
			Broker broker = (Broker) Assign.forConstructor(
					data_set.fieldString(0), data_set.fieldString(1) , data_set.fieldString(2));
		}
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
	
	protected static Broker getInstance(String username, int extend_id) {
		return brokers.get(String.format("%s@%d", username, extend_id));
	}
	
	protected String username;
	protected String password;
	
	public boolean checkPassword(String password) {
		return this.password.equals(password);
	}
}
