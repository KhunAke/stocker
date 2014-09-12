package com.javath.mapping;

// Generated Sep 10, 2014 9:53:58 AM by Hibernate Tools 4.0.0

import java.util.List;
import javax.naming.InitialContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.LockMode;
import org.hibernate.SessionFactory;
import static org.hibernate.criterion.Example.create;

/**
 * Home object for domain model class SettradeMarket.
 * @see com.javath.mapping.SettradeMarket
 * @author Hibernate Tools
 */
public class SettradeMarketHome {

	private static final Log log = LogFactory.getLog(SettradeMarketHome.class);

	private final SessionFactory sessionFactory = getSessionFactory();

	protected SessionFactory getSessionFactory() {
		try {
			return (SessionFactory) new InitialContext()
					.lookup("SessionFactory");
		} catch (Exception e) {
			log.error("Could not locate SessionFactory in JNDI", e);
			throw new IllegalStateException(
					"Could not locate SessionFactory in JNDI");
		}
	}

	public void persist(SettradeMarket transientInstance) {
		log.debug("persisting SettradeMarket instance");
		try {
			sessionFactory.getCurrentSession().persist(transientInstance);
			log.debug("persist successful");
		} catch (RuntimeException re) {
			log.error("persist failed", re);
			throw re;
		}
	}

	public void attachDirty(SettradeMarket instance) {
		log.debug("attaching dirty SettradeMarket instance");
		try {
			sessionFactory.getCurrentSession().saveOrUpdate(instance);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void attachClean(SettradeMarket instance) {
		log.debug("attaching clean SettradeMarket instance");
		try {
			sessionFactory.getCurrentSession().lock(instance, LockMode.NONE);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void delete(SettradeMarket persistentInstance) {
		log.debug("deleting SettradeMarket instance");
		try {
			sessionFactory.getCurrentSession().delete(persistentInstance);
			log.debug("delete successful");
		} catch (RuntimeException re) {
			log.error("delete failed", re);
			throw re;
		}
	}

	public SettradeMarket merge(SettradeMarket detachedInstance) {
		log.debug("merging SettradeMarket instance");
		try {
			SettradeMarket result = (SettradeMarket) sessionFactory
					.getCurrentSession().merge(detachedInstance);
			log.debug("merge successful");
			return result;
		} catch (RuntimeException re) {
			log.error("merge failed", re);
			throw re;
		}
	}

	public SettradeMarket findById(com.javath.mapping.SettradeMarketId id) {
		log.debug("getting SettradeMarket instance with id: " + id);
		try {
			SettradeMarket instance = (SettradeMarket) sessionFactory
					.getCurrentSession().get(
							"com.javath.mapping.SettradeMarket", id);
			if (instance == null) {
				log.debug("get successful, no instance found");
			} else {
				log.debug("get successful, instance found");
			}
			return instance;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}

	public List<SettradeMarket> findByExample(SettradeMarket instance) {
		log.debug("finding SettradeMarket instance by example");
		try {
			List<SettradeMarket> results = (List<SettradeMarket>) sessionFactory
					.getCurrentSession()
					.createCriteria("com.javath.mapping.SettradeMarket")
					.add(create(instance)).list();
			log.debug("find by example successful, result size: "
					+ results.size());
			return results;
		} catch (RuntimeException re) {
			log.error("find by example failed", re);
			throw re;
		}
	}
}
