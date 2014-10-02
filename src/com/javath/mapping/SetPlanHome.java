package com.javath.mapping;

// Generated Sep 29, 2014 1:56:13 PM by Hibernate Tools 4.0.0

import java.util.List;
import javax.naming.InitialContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.LockMode;
import org.hibernate.SessionFactory;
import static org.hibernate.criterion.Example.create;

/**
 * Home object for domain model class SetPlan.
 * @see com.javath.mapping.SetPlan
 * @author Hibernate Tools
 */
public class SetPlanHome {

	private static final Log log = LogFactory.getLog(SetPlanHome.class);

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

	public void persist(SetPlan transientInstance) {
		log.debug("persisting SetPlan instance");
		try {
			sessionFactory.getCurrentSession().persist(transientInstance);
			log.debug("persist successful");
		} catch (RuntimeException re) {
			log.error("persist failed", re);
			throw re;
		}
	}

	public void attachDirty(SetPlan instance) {
		log.debug("attaching dirty SetPlan instance");
		try {
			sessionFactory.getCurrentSession().saveOrUpdate(instance);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void attachClean(SetPlan instance) {
		log.debug("attaching clean SetPlan instance");
		try {
			sessionFactory.getCurrentSession().lock(instance, LockMode.NONE);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void delete(SetPlan persistentInstance) {
		log.debug("deleting SetPlan instance");
		try {
			sessionFactory.getCurrentSession().delete(persistentInstance);
			log.debug("delete successful");
		} catch (RuntimeException re) {
			log.error("delete failed", re);
			throw re;
		}
	}

	public SetPlan merge(SetPlan detachedInstance) {
		log.debug("merging SetPlan instance");
		try {
			SetPlan result = (SetPlan) sessionFactory.getCurrentSession()
					.merge(detachedInstance);
			log.debug("merge successful");
			return result;
		} catch (RuntimeException re) {
			log.error("merge failed", re);
			throw re;
		}
	}

	public SetPlan findById(java.lang.Integer id) {
		log.debug("getting SetPlan instance with id: " + id);
		try {
			SetPlan instance = (SetPlan) sessionFactory.getCurrentSession()
					.get("com.javath.mapping.SetPlan", id);
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

	public List<SetPlan> findByExample(SetPlan instance) {
		log.debug("finding SetPlan instance by example");
		try {
			List<SetPlan> results = (List<SetPlan>) sessionFactory
					.getCurrentSession()
					.createCriteria("com.javath.mapping.SetPlan")
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
