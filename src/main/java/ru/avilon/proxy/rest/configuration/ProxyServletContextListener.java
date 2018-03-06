package ru.avilon.proxy.rest.configuration;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class ProxyServletContextListener implements ServletContextListener {

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		
	}
	
	@Override
	public void contextDestroyed(ServletContextEvent sce) {
	
	}

}
