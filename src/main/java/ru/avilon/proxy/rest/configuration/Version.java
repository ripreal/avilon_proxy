package ru.avilon.proxy.rest.configuration;

import java.io.InputStream;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.util.ContextInitializer;


public class Version {
	private static final Logger logger = LoggerFactory.getLogger(Version.class);
	
	static String Revision; 

	static boolean test=(System.getProperty("test")!=null);
	
	public static boolean isTest(){
		return test;
	}
	
	public static String getRevision(ServletContext context){
		if (Revision==null){
			synchronized (Version.class) {
				if (Revision==null){
					Manifest mf = new Manifest();
					try {
						InputStream is=context.getResourceAsStream("/META-INF/MANIFEST.MF");
						if (is!=null){
							mf.read(is);
							Attributes atts = mf.getMainAttributes();
							Revision=atts.getValue("Implementation-Build");
						}
						if (Revision==null)
							Revision="dev";
						LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
						loggerContext.reset();
						loggerContext.putProperty("REV",Revision);	
						ContextInitializer ci = new ContextInitializer(loggerContext);
						ci.autoConfig();
					} catch (Exception e) {
						logger.error("Version",e);
						Revision="ERR";
					}
				}
			}
		}
		return (Revision.equals("dev"))?Revision.concat(""+System.currentTimeMillis()):Revision;
	}
	
	public static String getRevision(HttpServlet servlet){
		return getRevision(servlet.getServletContext());
	}
	
	public static String getRevision(HttpServletRequest request){
		if (request==null)
			return null;
		return getRevision((Revision==null)?request.getSession().getServletContext():null);
	}
	
	public static String getRevision(HttpServletRequest request,HttpServletResponse response){
		final String rev=getRevision(request);
		if (response!=null&&!response.isCommitted()){
			response.setHeader("X-rev", rev);
			response.setHeader("Server", "openam.org.ru/".concat(rev));
		}
		return rev;
	}
}