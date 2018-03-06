package ru.avilon.proxy.rest;

import java.io.File;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.restassured.RestAssured;

public class RestBase {

	static ObjectMapper mapper = new ObjectMapper();
	static Tomcat tomcat;
	
	static boolean useProd = false; 
	static boolean startLocal = true;
	
	@BeforeClass
	public static void beforeClass() throws Exception {
		if (!useProd) {
			if (startLocal && tomcat == null) {
				tomcat = new Tomcat();
				tomcat.setPort(8080);
				tomcat.setBaseDir("target/tomcat_test");
				tomcat.addWebapp("/", (new File("src/main/webapp")).getAbsolutePath());
				tomcat.start();
			}
		} else {
			RestAssured.baseURI = "https://avilon-stage.3a-systems.ru";
			RestAssured.useRelaxedHTTPSValidation();
		}
		RestAssured.basePath = "/";
	}
	
	@AfterClass
	public static void afterClass() {
		if(tomcat != null)
			try {
				tomcat.stop();
				tomcat.destroy();
				tomcat = null;
			} catch (LifecycleException e) {
			}
	}

	public RestBase() {
		super();
	}

}