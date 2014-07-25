package com.javath.mapping;

// Generated Jul 25, 2014 8:28:24 AM by Hibernate Tools 4.0.0

import java.util.List;
import javax.naming.InitialContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.LockMode;
import org.hibernate.SessionFactory;
import static org.hibernate.criterion.Example.create;

/**
 * Home object for domain model class SetSector.
 * @see com.javath.mapping.SetSector
 * @author Hibernate Tools
 */
public class SetSectorHome {

	private static final Log log = LogFactory.getLog(SetSectorHome.class);

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

	public void persist(SetSector transientInstance) {
		log.debug("persisting SetSector instance");
		try {
			sessionFactory.getCurrentSession().persist(transientInstance);
			log.debug("persist successful");
		} catch (RuntimeException re) {
			log.error("persist failed", re);
			throw re;
		}
	}

	public void attachDirty(SetSector instance) {
		log.debug("attaching dirty SetSector instance");
		try {
			sessionFactory.getCurrentSession().saveOrUpdate(instance);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void attachClean(SetSector instance) {
		log.debug("attaching clean SetSector instance");
		try {
			sessionFactory.getCurrentSession().lock(instance, LockMode.NONE);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void delete(SetSector persistentInstance) {
		log.debug("deleting SetSector instance");
		try {
			sessionFactory.getCurrentSession().delete(persistentInstance);
			log.debug("delete successful");
		} catch (RuntimeException re) {
			log.error("delete failed", re);
			throw re;
		}
	}

	public SetSector merge(SetSector detachedInstance) {
		log.debug("merging SetSector instance");
		try {
			SetSector result = (SetSector) sessionFactory.getCurrentSession()
					.merge(detachedInstance);
			log.debug("merge successful");
			return result;
		} catch (RuntimeException re) {
			log.error("merge failed", re);
			throw re;
		}
	}

	public SetSector findById(com.javath.mapping.SetSectorId id) {
		log.debug("getting SetSector instance with id: " + id);
		try {
			SetSector instance = (SetSector) sessionFactory.getCurrentSession()
					.get("com.javath.mapping.SetSector", id);
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

	public List<SetSector> findByExample(SetSector instance) {
		log.debug("finding SetSector instance by example");
		try {
			List<SetSector> results = (List<SetSector>) sessionFactory
					.getCurrentSession()
					.createCriteria("com.javath.mapping.SetSector")
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
