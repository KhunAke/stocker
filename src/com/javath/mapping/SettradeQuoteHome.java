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
 * Home object for domain model class SettradeQuote.
 * @see com.javath.mapping.SettradeQuote
 * @author Hibernate Tools
 */
public class SettradeQuoteHome {

    private static final Log log = LogFactory.getLog(SettradeQuoteHome.class);

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
    
    public void persist(SettradeQuote transientInstance) {
        log.debug("persisting SettradeQuote instance");
        try {
            sessionFactory.getCurrentSession().persist(transientInstance);
            log.debug("persist successful");
        }
        catch (RuntimeException re) {
            log.error("persist failed", re);
            throw re;
        }
    }
    
    public void attachDirty(SettradeQuote instance) {
        log.debug("attaching dirty SettradeQuote instance");
        try {
            sessionFactory.getCurrentSession().saveOrUpdate(instance);
            log.debug("attach successful");
        }
        catch (RuntimeException re) {
            log.error("attach failed", re);
            throw re;
        }
    }
    
    public void attachClean(SettradeQuote instance) {
        log.debug("attaching clean SettradeQuote instance");
        try {
            sessionFactory.getCurrentSession().lock(instance, LockMode.NONE);
            log.debug("attach successful");
        }
        catch (RuntimeException re) {
            log.error("attach failed", re);
            throw re;
        }
    }
    
    public void delete(SettradeQuote persistentInstance) {
        log.debug("deleting SettradeQuote instance");
        try {
            sessionFactory.getCurrentSession().delete(persistentInstance);
            log.debug("delete successful");
        }
        catch (RuntimeException re) {
            log.error("delete failed", re);
            throw re;
        }
    }
    
    public SettradeQuote merge(SettradeQuote detachedInstance) {
        log.debug("merging SettradeQuote instance");
        try {
            SettradeQuote result = (SettradeQuote) sessionFactory.getCurrentSession()
                    .merge(detachedInstance);
            log.debug("merge successful");
            return result;
        }
        catch (RuntimeException re) {
            log.error("merge failed", re);
            throw re;
        }
    }
    
    public SettradeQuote findById( com.javath.mapping.SettradeQuoteId id) {
        log.debug("getting SettradeQuote instance with id: " + id);
        try {
            SettradeQuote instance = (SettradeQuote) sessionFactory.getCurrentSession()
                    .get("com.javath.mapping.SettradeQuote", id);
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
    
    public List<SettradeQuote> findByExample(SettradeQuote instance) {
        log.debug("finding SettradeQuote instance by example");
        try {
            List<SettradeQuote> results = (List<SettradeQuote>) sessionFactory.getCurrentSession()
                    .createCriteria("com.javath.mapping.SettradeQuote")
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

