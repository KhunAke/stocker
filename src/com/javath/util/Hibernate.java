package com.javath.util;

import java.io.File;
import java.util.Properties;

import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

public class Hibernate {

	private static final SessionFactory sessionFactory;
	private static final Assign assign;
	
	static {
		String default_Properties = Assign.etc + Assign.FILE_SEPARATOR +
				"util" + Assign.FILE_SEPARATOR +
				"Hibernate.properties";
		assign = Assign.getInstance(Hibernate.class.getCanonicalName(), default_Properties);
		String default_configuration = Assign.etc + Assign.FILE_SEPARATOR + 
				"hibernate.cfg.xml";
		String hibernate_cfg = assign.getProperty("configuration", default_configuration);
		Configuration configuration = new Configuration();
		configuration.configure(new File(hibernate_cfg));

		Properties properties = configuration.getProperties();
		properties.put("hibernate.current_session_context_class", "thread");
		// Hibernate 4.3.5
		ServiceRegistry service_registry = new StandardServiceRegistryBuilder()
				.applySettings(configuration.getProperties()).build();
		sessionFactory = configuration.buildSessionFactory(service_registry);
		// Hibernate 4.0.1 deprecated
		//ServiceRegistry service_registry = new ServiceRegistryBuilder()
		//		.applySettings(properties).buildServiceRegistry();
		//sessionFactory = configuration.buildSessionFactory(service_registry);
		// Hibernate 3.6.10 deprecated
		//sessionFactory = configuration.buildSessionFactory();
	}
	
	public static SessionFactory getSessionFactory() {
		return sessionFactory;
	}
	
}
