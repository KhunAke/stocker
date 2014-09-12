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
 * Home object for domain model class SetBroker.
 * @see com.javath.mapping.SetBroker
 * @author Hibernate Tools
 */
public class SetBrokerHome {

	private static final Log log = LogFactory.getLog(SetBrokerHome.class);

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

	public void persist(SetBroker transientInstance) {
		log.debug("persisting SetBroker instance");
		try {
			sessionFactory.getCurrentSession().persist(transientInstance);
			log.debug("persist successful");
		} catch (RuntimeException re) {
			log.error("persist failed", re);
			throw re;
		}
	}

	public void attachDirty(SetBroker instance) {
		log.debug("attaching dirty SetBroker instance");
		try {
			sessionFactory.getCurrentSession().saveOrUpdate(instance);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void attachClean(SetBroker instance) {
		log.debug("attaching clean SetBroker instance");
		try {
			sessionFactory.getCurrentSession().lock(instance, LockMode.NONE);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void delete(SetBroker persistentInstance) {
		log.debug("deleting SetBroker instance");
		try {
			sessionFactory.getCurrentSession().delete(persistentInstance);
			log.debug("delete successful");
		} catch (RuntimeException re) {
			log.error("delete failed", re);
			throw re;
		}
	}

	public SetBroker merge(SetBroker detachedInstance) {
		log.debug("merging SetBroker instance");
		try {
			SetBroker result = (SetBroker) sessionFactory.getCurrentSession()
					.merge(detachedInstance);
			log.debug("merge successful");
			return result;
		} catch (RuntimeException re) {
			log.error("merge failed", re);
			throw re;
		}
	}

	public SetBroker findById(java.lang.Integer id) {
		log.debug("getting SetBroker instance with id: " + id);
		try {
			SetBroker instance = (SetBroker) sessionFactory.getCurrentSession()
					.get("com.javath.mapping.SetBroker", id);
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

	public List<SetBroker> findByExample(SetBroker instance) {
		log.debug("finding SetBroker instance by example");
		try {
			List<SetBroker> results = (List<SetBroker>) sessionFactory
					.getCurrentSession()
					.createCriteria("com.javath.mapping.SetBroker")
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
