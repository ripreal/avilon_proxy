package ru.avilon.proxy.rest.configuration;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.glassfish.jersey.server.mvc.jsp.JspMvcFeature;
import org.glassfish.jersey.servlet.ServletProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.avilon.proxy.rest.Administration;
import ru.avilon.proxy.rest.Authentication;
import ru.avilon.proxy.rest.Changes;
import ru.avilon.proxy.rest.configuration.annotations.CheckBasicAuthentication;
import ru.avilon.proxy.rest.configuration.annotations.CheckOAuthAuthentication;
import ru.avilon.proxy.ui.Console;

@ApplicationPath("/")
public class ProxyApplication extends Application {
	
	final static Logger logger = LoggerFactory.getLogger(ProxyApplication.class); 
	
	public ProxyApplication() {
		
    }
	
	@Override
	public Set<Class<?>> getClasses()
	{
		final Set<Class<?>> returnValue = new HashSet<Class<?>>( );
		returnValue.add(Authentication.class);
		returnValue.add(Changes.class);
		returnValue.add(Administration.class);
		
		//configure mvc
		returnValue.add(Console.class);
		returnValue.add(JspMvcFeature.class);

		
		//filters
		returnValue.add( OAuthRequestFilter.class );
		returnValue.add( CheckBasicAuthentication.class );
		returnValue.add( BasicAuthenticationRequestFilter.class );
		returnValue.add( CheckOAuthAuthentication.class );
		
		returnValue.add( ProxyLoggingFilter.class);
		
		//features
		returnValue.add( DependencyBinderFeature.class );
		returnValue.add(RolesAllowedDynamicFeature.class);
		
		returnValue.add( JerseyExceptionMapper.class );
		
		return returnValue;
	}
	
	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = new HashMap<>();
		properties.put(JspMvcFeature.TEMPLATE_BASE_PATH, "/WEB-INF/jsp");
		properties.put(ServletProperties.FILTER_STATIC_CONTENT_REGEX, "/assets/.*");
		return properties;
	}
	
}
