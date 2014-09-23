package com.javath.mapping;

// Generated Sep 23, 2014 1:12:16 PM by Hibernate Tools 4.0.0

import java.util.List;
import javax.naming.InitialContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.LockMode;
import org.hibernate.SessionFactory;
import static org.hibernate.criterion.Example.create;

/**
 * Home object for domain model class BualuangQuoteDaily.
 * @see com.javath.mapping.BualuangQuoteDaily
 * @author Hibernate Tools
 */
public class BualuangQuoteDailyHome {

	private static final Log log = LogFactory
			.getLog(BualuangQuoteDailyHome.class);

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

	public void persist(BualuangQuoteDaily transientInstance) {
		log.debug("persisting BualuangQuoteDaily instance");
		try {
			sessionFactory.getCurrentSession().persist(transientInstance);
			log.debug("persist successful");
		} catch (RuntimeException re) {
			log.error("persist failed", re);
			throw re;
		}
	}

	public void attachDirty(BualuangQuoteDaily instance) {
		log.debug("attaching dirty BualuangQuoteDaily instance");
		try {
			sessionFactory.getCurrentSession().saveOrUpdate(instance);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void attachClean(BualuangQuoteDaily instance) {
		log.debug("attaching clean BualuangQuoteDaily instance");
		try {
			sessionFactory.getCurrentSession().lock(instance, LockMode.NONE);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void delete(BualuangQuoteDaily persistentInstance) {
		log.debug("deleting BualuangQuoteDaily instance");
		try {
			sessionFactory.getCurrentSession().delete(persistentInstance);
			log.debug("delete successful");
		} catch (RuntimeException re) {
			log.error("delete failed", re);
			throw re;
		}
	}

	public BualuangQuoteDaily merge(BualuangQuoteDaily detachedInstance) {
		log.debug("merging BualuangQuoteDaily instance");
		try {
			BualuangQuoteDaily result = (BualuangQuoteDaily) sessionFactory
					.getCurrentSession().merge(detachedInstance);
			log.debug("merge successful");
			return result;
		} catch (RuntimeException re) {
			log.error("merge failed", re);
			throw re;
		}
	}

	public BualuangQuoteDaily findById(
			com.javath.mapping.BualuangQuoteDailyId id) {
		log.debug("getting BualuangQuoteDaily instance with id: " + id);
		try {
			BualuangQuoteDaily instance = (BualuangQuoteDaily) sessionFactory
					.getCurrentSession().get(
							"com.javath.mapping.BualuangQuoteDaily", id);
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

	public List<BualuangQuoteDaily> findByExample(BualuangQuoteDaily instance) {
		log.debug("finding BualuangQuoteDaily instance by example");
		try {
			List<BualuangQuoteDaily> results = (List<BualuangQuoteDaily>) sessionFactory
					.getCurrentSession()
					.createCriteria("com.javath.mapping.BualuangQuoteDaily")
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
