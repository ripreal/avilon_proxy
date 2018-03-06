package ru.avilon.proxy.rest;

import java.lang.annotation.Annotation;
import java.util.List;

import javax.ws.rs.core.Response;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.avilon.proxy.rest.configuration.TestDependencyBinder;

public class Logs_IT {

	final static Logger logger = LoggerFactory.getLogger(Logs_IT.class);
	
	static ServiceLocator serviceLocator = ServiceLocatorUtilities.bind(new TestDependencyBinder());
	
	static Administration administration = serviceLocator.getService(Administration.class, new Annotation[] {});
	
	@BeforeClass
	public static void beforeClass() {
		System.setProperty("ru.avoilon.proxy.log.path", "test-logs");
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testLogs() {
		Response response = administration.getLogFiles();
		List<String> entity = (List<String>)response.getEntity();
		System.err.println(entity);
		
		for(String file : entity) {
			System.out.println(file);
			List<String> lines = (List<String>) administration.getLogFileContent(file, null, "WARN").getEntity();
			lines.stream().forEach(line -> System.out.println(line));
			
		}
	}
}
