package com.javath.mapping;
// Generated Aug 6, 2014 8:47:30 AM by Hibernate Tools 4.0.0


import java.util.List;
import javax.naming.InitialContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.LockMode;
import org.hibernate.SessionFactory;
import static org.hibernate.criterion.Example.create;

/**
 * Home object for domain model class SetMarket.
 * @see com.javath.mapping.SetMarket
 * @author Hibernate Tools
 */
public class SetMarketHome {

    private static final Log log = LogFactory.getLog(SetMarketHome.class);

    private final SessionFactory sessionFactory = getSessionFactory();
    
    protected SessionFactory getSessionFactory() {
        try {
            return (SessionFactory) new InitialContext().lookup("SessionFactory");
        }
        catch (Exception e) {
            log.error("Could not locate SessionFactory in JNDI", e);
            throw new IllegalStateException("Could not locate SessionFactory in JNDI");
        }
    }
    
    public void persist(SetMarket transientInstance) {
        log.debug("persisting SetMarket instance");
        try {
            sessionFactory.getCurrentSession().persist(transientInstance);
            log.debug("persist successful");
        }
        catch (RuntimeException re) {
            log.error("persist failed", re);
            throw re;
        }
    }
    
    public void attachDirty(SetMarket instance) {
        log.debug("attaching dirty SetMarket instance");
        try {
            sessionFactory.getCurrentSession().saveOrUpdate(instance);
            log.debug("attach successful");
        }
        catch (RuntimeException re) {
            log.error("attach failed", re);
            throw re;
        }
    }
    
    public void attachClean(SetMarket instance) {
        log.debug("attaching clean SetMarket instance");
        try {
            sessionFactory.getCurrentSession().lock(instance, LockMode.NONE);
            log.debug("attach successful");
        }
        catch (RuntimeException re) {
            log.error("attach failed", re);
            throw re;
        }
    }
    
    public void delete(SetMarket persistentInstance) {
        log.debug("deleting SetMarket instance");
        try {
            sessionFactory.getCurrentSession().delete(persistentInstance);
            log.debug("delete successful");
        }
        catch (RuntimeException re) {
            log.error("delete failed", re);
            throw re;
        }
    }
    
    public SetMarket merge(SetMarket detachedInstance) {
        log.debug("merging SetMarket instance");
        try {
            SetMarket result = (SetMarket) sessionFactory.getCurrentSession()
                    .merge(detachedInstance);
            log.debug("merge successful");
            return result;
        }
        catch (RuntimeException re) {
            log.error("merge failed", re);
            throw re;
        }
    }
    
    public SetMarket findById( short id) {
        log.debug("getting SetMarket instance with id: " + id);
        try {
            SetMarket instance = (SetMarket) sessionFactory.getCurrentSession()
                    .get("com.javath.mapping.SetMarket", id);
            if (instance==null) {
                log.debug("get successful, no instance found");
            }
            else {
                log.debug("get successful, instance found");
            }
            return instance;
        }
        catch (RuntimeException re) {
            log.error("get failed", re);
            throw re;
        }
    }
    
    public List<SetMarket> findByExample(SetMarket instance) {
        log.debug("finding SetMarket instance by example");
        try {
            List<SetMarket> results = (List<SetMarket>) sessionFactory.getCurrentSession()
                    .createCriteria("com.javath.mapping.SetMarket")
                    .add( create(instance) )
            .list();
            log.debug("find by example successful, result size: " + results.size());
            return results;
        }
        catch (RuntimeException re) {
            log.error("find by example failed", re);
            throw re;
        }
    } 
}

