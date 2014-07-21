package com.javath.mapping;
// Generated Jul 21, 2014 12:58:19 PM by Hibernate Tools 4.0.0


import java.util.List;
import javax.naming.InitialContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.LockMode;
import org.hibernate.SessionFactory;
import static org.hibernate.criterion.Example.create;

/**
 * Home object for domain model class SetIndustry.
 * @see com.javath.mapping.SetIndustry
 * @author Hibernate Tools
 */
public class SetIndustryHome {

    private static final Log log = LogFactory.getLog(SetIndustryHome.class);

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
    
    public void persist(SetIndustry transientInstance) {
        log.debug("persisting SetIndustry instance");
        try {
            sessionFactory.getCurrentSession().persist(transientInstance);
            log.debug("persist successful");
        }
        catch (RuntimeException re) {
            log.error("persist failed", re);
            throw re;
        }
    }
    
    public void attachDirty(SetIndustry instance) {
        log.debug("attaching dirty SetIndustry instance");
        try {
            sessionFactory.getCurrentSession().saveOrUpdate(instance);
            log.debug("attach successful");
        }
        catch (RuntimeException re) {
            log.error("attach failed", re);
            throw re;
        }
    }
    
    public void attachClean(SetIndustry instance) {
        log.debug("attaching clean SetIndustry instance");
        try {
            sessionFactory.getCurrentSession().lock(instance, LockMode.NONE);
            log.debug("attach successful");
        }
        catch (RuntimeException re) {
            log.error("attach failed", re);
            throw re;
        }
    }
    
    public void delete(SetIndustry persistentInstance) {
        log.debug("deleting SetIndustry instance");
        try {
            sessionFactory.getCurrentSession().delete(persistentInstance);
            log.debug("delete successful");
        }
        catch (RuntimeException re) {
            log.error("delete failed", re);
            throw re;
        }
    }
    
    public SetIndustry merge(SetIndustry detachedInstance) {
        log.debug("merging SetIndustry instance");
        try {
            SetIndustry result = (SetIndustry) sessionFactory.getCurrentSession()
                    .merge(detachedInstance);
            log.debug("merge successful");
            return result;
        }
        catch (RuntimeException re) {
            log.error("merge failed", re);
            throw re;
        }
    }
    
    public SetIndustry findById( com.javath.mapping.SetIndustryId id) {
        log.debug("getting SetIndustry instance with id: " + id);
        try {
            SetIndustry instance = (SetIndustry) sessionFactory.getCurrentSession()
                    .get("com.javath.mapping.SetIndustry", id);
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
    
    public List<SetIndustry> findByExample(SetIndustry instance) {
        log.debug("finding SetIndustry instance by example");
        try {
            List<SetIndustry> results = (List<SetIndustry>) sessionFactory.getCurrentSession()
                    .createCriteria("com.javath.mapping.SetIndustry")
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

