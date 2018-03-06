package ru.avilon.proxy.conversion;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.ws.rs.core.SecurityContext;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.jvnet.hk2.annotations.Optional;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.ScriptableObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ru.avilon.proxy.entities.ConversionScript;
import ru.avilon.proxy.entities.FilterScript;
import ru.avilon.proxy.entities.ProxyObject;
import ru.avilon.proxy.entities.User;
import ru.avilon.proxy.repo.CassandraDataStore;
import ru.avilon.proxy.repo.CassandraDataStore.ConversionDirection;
import ru.avilon.proxy.repo.MetadataRepository;
import ru.avilon.proxy.rest.configuration.DependencyBinder;

public class Converter {
	public static Logger logger = LoggerFactory.getLogger(Converter.class); 
	
	static ServiceLocator serviceLocator = ServiceLocatorUtilities.bind(new DependencyBinder());

	static CassandraDataStore cassandraDataStore = serviceLocator.getService(CassandraDataStore.class, new Annotation[] {});;
	
	static ObjectMapper objectMapper = serviceLocator.getService(ObjectMapper.class, new Annotation[] {});;
	
	@Inject @Optional SecurityContext securityContext;
	
	
	Context context;
	ScriptableObject scope;
	
	static String commonsScript = null; 
	static {
		try {
			URL url = MetadataRepository.class.getResource("/js/common_utils.js");
			Path scriptFile = Paths.get(url.toURI());
			commonsScript = new String(Files.readAllBytes(scriptFile));
		} catch(Exception e) {
			logger.error("error initialising common utils script", e);
		}
	}
	
	public String getCurrentUserUUID() {
		return securityContext.getUserPrincipal().getName();
	}
	
	public static void log(String msg) {
//		if(logger.isDebugEnabled());
			logger.info(msg);
	}
	
	public static String getObjectByUUID(String uuid) {
		throw new RuntimeException("not implemented!!");
	}
	
	public static String getObject(String uuid, String clientapp, String type, boolean dataOnly) {
		ProxyObject proxyObject = cassandraDataStore.getProxyObject(UUID.fromString(uuid), clientapp, type);
		try {
			return proxyObject != null ? (dataOnly ? proxyObject.getData() : objectMapper.writeValueAsString(proxyObject)) : null;
		} catch (JsonProcessingException e) {
			logger.error(e.toString(), e);
			throw new RuntimeException(e);
			
		}
	}
	public static void saveObject(String objectBody) {
		try {
			ProxyObject proxyObject = objectMapper.readValue(objectBody, ProxyObject.class);
			cassandraDataStore.saveObject(proxyObject);
		} catch (Exception e) {
			logger.error(e.toString(), e);
			throw new RuntimeException(e);
		}
	}
	
	public static void deleteObject(String uuid, String clientApp, String type) {
		try {
			cassandraDataStore.deleteObject(UUID.fromString(uuid), clientApp, type);
		} catch (Exception e) {
			logger.error(e.toString(), e);
			throw new RuntimeException(e);
		}
	}
	
	
	public static void createUser(String login, String password, String[] roles, String objectUUID) {
		User user = new User(login);
		user.setPassword(password);
		user.setRoles(new HashSet<String>(Arrays.asList(roles)));
		user.setJson_object_uuid(UUID.fromString(objectUUID));
		cassandraDataStore.saveUser(user);
	}
	
	public static void deleteUser(String login) {
		cassandraDataStore.deleteUser(login);
	}
	
	public static String getUserByUUID(String json_obj_uuid) {
		User user = cassandraDataStore.getUserByUUID(UUID.fromString(json_obj_uuid));
		try {
			return user != null ? objectMapper.writeValueAsString(user) : null;
		} catch (JsonProcessingException e) {
			logger.error(e.toString(), e);
			throw new RuntimeException(e);
		}
	}
	
	public String convertFrom(String clientApp, String object) {
		logger.debug("executing convertFrom script for {} {}",  clientApp, object);
		Function conversionFunction = getCoversionFunction(clientApp, ConversionDirection.FROM);
		Object result = conversionFunction.call(context, scope, scope, new Object[] {object});
		return result != null ? (String) Context.jsToJava(result, String.class) : null;
	}
	
	public String convertTo(String clientApp, String object) {
		logger.debug("executing convertTo script for {} {}", clientApp, object);
		Function conversionFunction = getCoversionFunction(clientApp, ConversionDirection.TO);
		Object result = conversionFunction.call(context, scope, scope, new Object[] {object});
		return result != null ? (String) Context.jsToJava(result, String.class) : null;
	}
	
	protected Function getCoversionFunction(String script, String scriptName) {
		context = Context.enter();
		context.setLanguageVersion(Context.VERSION_ES6);
		scope = context.initStandardObjects();

		try {
			if(securityContext != null && securityContext.getUserPrincipal() != null) {
				Object sc = Context.javaToJS((String)securityContext.getUserPrincipal().getName(), scope);
				ScriptableObject.putProperty(scope, "currentUser", sc);
			}
			context.evaluateString(scope, commonsScript, "commonsScript", 1, null);
			context.evaluateString(scope, script, scriptName, 1, null);
			return (Function) scope.get("convertData");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	protected Function getCoversionFunction(String clientApp, ConversionDirection direction) {
		String script = cassandraDataStore.getConversionScript(clientApp,  direction);
		String scriptName =  clientApp.concat("_").concat(direction.toString());
		return getCoversionFunction(script, scriptName);
	}
	
	public void conversionTrigger(List<ProxyObject> proxyObjectList, String clientApp) {
		List<ConversionScript> conversionScripts = cassandraDataStore.getConversionScriptsByCriteria(null, null);
		for(ConversionScript conversionScript : conversionScripts) {
			if(conversionScript.getFrom_to().equals(CassandraDataStore.ConversionDirection.FROM.toString()) || conversionScript.getClient_app().equals(clientApp))
				continue;
			String scriptName =  conversionScript.getClient_app() .concat("_").concat(conversionScript.getFrom_to().toString());
			Function conversionFunction = getCoversionFunction(conversionScript.getScript(), scriptName);
			
			for(ProxyObject proxyObject : proxyObjectList) {
				try {
					String object = objectMapper.writeValueAsString(proxyObject);
					Object result = conversionFunction.call(context, scope, scope, new Object[] {object});
					if(result == null) {
						continue;
					}
					String convertedProxy = (String) Context.jsToJava(result, String.class);
					ProxyObject converted = null;
					converted = objectMapper.readValue(convertedProxy, ProxyObject.class);
					converted.setClientapp(conversionScript.getClient_app());
					cassandraDataStore.saveObject(converted);
				} catch (Exception e) {
					logger.error("error processing object: {} : {}", e.toString(), proxyObject, e);
					throw new RuntimeException("error processing object: " + proxyObject.toString(),  e);
				}
			}
		}
	}
	
	//filter functions
		
	
	public List<ProxyObject> filter(String clientApp, List<ProxyObject> proxyObjectList) {
		FilterScript script = cassandraDataStore.getFilterScript(clientApp);
		if(script == null)
			return proxyObjectList;
		
		Function function = getFilterFunction(script.getScript(), "filter_".concat(clientApp));
		
		List<ProxyObject> resultList = new ArrayList<>(); 
		
		for(ProxyObject proxyObject : proxyObjectList) {
			String object;
			try {
				object = objectMapper.writeValueAsString(proxyObject);
				Object result = function.call(context, scope, scope, new Object[] {object});
				if(result != null) {
					String convertedProxy = (String) Context.jsToJava(result, String.class);
					resultList.add(objectMapper.readValue(convertedProxy, ProxyObject.class));
				}
			} catch (IOException e) {
				logger.error(e.toString(), e);
				throw new RuntimeException(e);
			}
		}
				
		return resultList;
	}
	
	
	protected Function getFilterFunction(String script, String scriptName) {
		context = Context.enter();
		context.setLanguageVersion(Context.VERSION_ES6);
		scope = context.initStandardObjects();

		try {
			if(securityContext != null && securityContext.getUserPrincipal() != null) {
				Object userName = Context.javaToJS((String)securityContext.getUserPrincipal().getName(), scope);
				Object userUUID= Context.javaToJS(((User)securityContext.getUserPrincipal()).getJson_object_uuid().toString(), scope);
				ScriptableObject.putProperty(scope, "currentUser", userName);
				ScriptableObject.putProperty(scope, "currentUserUUID", userUUID);
			}
			context.evaluateString(scope, commonsScript, "commonsScript", 1, null);
			context.evaluateString(scope, script, scriptName, 1, null);
			return (Function) scope.get("filter");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
}
