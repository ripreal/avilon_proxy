package ru.avilon.proxy.rest;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.qos.logback.core.rolling.RollingFileAppender;
import ru.avilon.proxy.entities.ConversionScript;
import ru.avilon.proxy.entities.FilterScript;
import ru.avilon.proxy.entities.ProxyObject;
import ru.avilon.proxy.entities.User;
import ru.avilon.proxy.repo.CassandraDataStore;
import ru.avilon.proxy.rest.configuration.ProxyLoggingFilter;
import ru.avilon.proxy.rest.configuration.annotations.CheckBasicAuthentication;
import ru.avilon.proxy.utils.GZIPFiles;
import ru.avilon.proxy.utils.TimeUUIDUtils;

@Path("/admin")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed("admin")
@CheckBasicAuthentication
public class Administration {
	
	Logger logger = LoggerFactory.getLogger(Administration.class);
	
	@Inject CassandraDataStore cassandraDataStore;
	@Inject ObjectMapper mapper;
	
	@GET
	@Path("/object")
	public Response getObjects(@QueryParam("f_clientapp") String clientApp,
			@QueryParam("f_objecttype") String objectType,
			@QueryParam("timestamp_from") Long timestamp_from, 
			@QueryParam("timestamp_to") Long timestamp_to,
			@QueryParam("pattern") String pattern,
			@QueryParam("limit") Integer limit) {
		
		Set<String> clientApps = StringUtils.isBlank(clientApp) ?  cassandraDataStore.getClientApps() : Collections.singleton(clientApp);
		
		Set<String> objectTypes = StringUtils.isBlank(objectType) ?  cassandraDataStore.getObjectTypes(clientApps) : Collections.singleton(objectType);
		UUID timeUUIDFrom = timestamp_from != null ? TimeUUIDUtils.getTimeUUID(timestamp_from) : null;
		
		UUID timeUUIDTo = timestamp_to != null ? TimeUUIDUtils.getTimeUUID(timestamp_to) : null;
		
		List<ProxyObject> objectList = null;
		if(pattern != null) {
			objectList = cassandraDataStore.getProxyObjectsByCriteria(clientApps, objectTypes, timeUUIDFrom, timeUUIDTo, null, null);
			objectList = objectList.stream().filter(t -> StringUtils.containsIgnoreCase(t.toString(), pattern)).collect(Collectors.toList());
			if(limit != null && limit > 0 && objectList.size() > limit)
				objectList = objectList.subList(0, limit - 1);
		} else {
			objectList = cassandraDataStore.getProxyObjectsByCriteria(clientApps, objectTypes, timeUUIDFrom, timeUUIDTo, null, limit);
		}
		
		try {
			String result = mapper.writeValueAsString(objectList);
			return Response.ok().entity(result).build();
		} catch (JsonProcessingException e) {
			logger.error(e.toString(), e);
			throw new WebApplicationException(e);
		}
	}
	
	
	@GET
	@Path("/object/{uuid}/{clientapp}/{objecttype}")
	public Response getObject(@PathParam("uuid") String uuid, @PathParam("clientapp") String clientapp, @PathParam("objecttype") String objecttype) {
		ProxyObject object = cassandraDataStore.getProxyObject(UUID.fromString(uuid), clientapp, objecttype);
		String result = null;
		try {
			result = mapper.writeValueAsString(object);
		} catch (JsonProcessingException e) {
			logger.error(e.toString(), e);
			throw new WebApplicationException(e);
		}
		return Response.ok().entity(result).build();
	}
	
	

	@POST
	@Path("/object")
	public Response saveObject(String body) {
		if(logger.isDebugEnabled())
			logger.debug("got json changes: {}", body);
		
		JSONObject jsonBody = new JSONObject(body);
		jsonBody.put("timeUUID", TimeUUIDUtils.getTimeUUID(jsonBody.getLong("timestamp")));
		jsonBody.remove("timeuuid");
		jsonBody.remove("timestamp");
		jsonBody.remove("time");
		
		ProxyObject proxyObject = null;
		try {
			proxyObject = mapper.readValue(jsonBody.toString(), ProxyObject.class);
			cassandraDataStore.saveObject(proxyObject);
		} catch (Exception e) {
			logger.error(e.toString(), e);
			throw new WebApplicationException(e);
		}
		return Response.created(null).build();
	}
	
	@DELETE
	@Path("/object/{uuid}/{clientapp}/{objecttype}")
	public Response deleteObject(@PathParam("uuid") String uuid, @PathParam("clientapp") String clientapp, @PathParam("objecttype") String objecttype) {
		cassandraDataStore.deleteObject(UUID.fromString(uuid), clientapp, objecttype);
		return Response.ok().build();
	}
	
	@GET
	@Path("/script")
	public Response getScripts(@QueryParam("client_app") String client_app, @QueryParam("direction") String direction  ) {
		List<ConversionScript> scripts = cassandraDataStore.getConversionScriptsByCriteria(client_app, direction);
		try {
			String result = mapper.writeValueAsString(scripts);
			return Response.ok().entity(result).build();
		} catch (JsonProcessingException e) {
			logger.error(e.toString(), e);
			throw new WebApplicationException(e);
		}
	}
	
	@POST
	@Path("/script")
	public Response saveScript(String body) {
		if(logger.isDebugEnabled())
			logger.debug("got script: {}", body);
		
		JSONObject jsonBody = new JSONObject(body);

		
		ConversionScript conversionScript = null;
		try {
			conversionScript = mapper.readValue(jsonBody.toString(), ConversionScript.class);
			cassandraDataStore.saveScript(conversionScript);
		} catch (Exception e) {
			logger.error(e.toString(), e);
			throw new WebApplicationException(e);
		}
		return Response.created(null).build();
	}
	
	@DELETE
	@Path("/script/{client_app}/{from_to}")
	public Response deleteScript(@PathParam("client_app") String client_app, @PathParam("from_to") String from_to) {
		cassandraDataStore.deleteScript(client_app, from_to);
		return Response.ok().build();
	}
	
	@GET
	@Path("/filterscript")
	public Response getFilterScripts(@QueryParam("client_app") String client_app) {
		List<FilterScript> scripts = cassandraDataStore.getFilterScriptsByCriteria(client_app);
		try {
			String result = mapper.writeValueAsString(scripts);
			return Response.ok().entity(result).build();
		} catch (JsonProcessingException e) {
			logger.error(e.toString(), e);
			throw new WebApplicationException(e);
		}
	}
	
	@POST
	@Path("/filterscript")
	public Response saveFilterScript(String body) {
		if(logger.isDebugEnabled())
			logger.debug("got script: {}", body);
		
		JSONObject jsonBody = new JSONObject(body);

		
		FilterScript filterScript = null;
		try {
			filterScript = mapper.readValue(jsonBody.toString(), FilterScript.class);
			cassandraDataStore.saveFilterScript(filterScript);
		} catch (Exception e) {
			logger.error(e.toString(), e);
			throw new WebApplicationException(e);
		}
		return Response.created(null).build();
	}
	
	@DELETE
	@Path("/filterscript/{client_app}")
	public Response deleteFilterScript(@PathParam("client_app") String client_app) {
		cassandraDataStore.deleteFilterScript(client_app);
		return Response.ok().build();
	}
	
	@GET
	@Path("/user")
	public Response getUsers() {
		List<User> scripts = cassandraDataStore.getUsers();
		try {
			String result = mapper.writeValueAsString(scripts);
			return Response.ok().entity(result).build();
		} catch (JsonProcessingException e) {
			logger.error(e.toString(), e);
			throw new WebApplicationException(e);
		}
	}
	
	@POST
	@Path("/user")
	public Response saveUser(String body) {
		if(logger.isDebugEnabled())
			logger.debug("got script: {}", body);
		
		JSONObject jsonBody = new JSONObject(body);

		User user = null;
		try {
			user = mapper.readValue(body, User.class);
			user.setPassword(jsonBody.has("password") ? jsonBody.getString("password") : "");
			cassandraDataStore.saveUser(user);
		} catch (Exception e) {
			logger.error(e.toString(), e);
			throw new WebApplicationException(e);
		}
		return Response.created(null).build();
	}
	
	@DELETE
	@Path("/user/{name}/")
	public Response deleteUser(@PathParam("name") String name) {
		cassandraDataStore.deleteUser(name);
		return Response.ok().build();
	}
	
	
	@GET
	@Path("/log")
	public Response getLogFiles() {
		java.nio.file.Path path = getLogfilePath();
		
		List<String> fileList = new ArrayList<>();
		
		try (DirectoryStream<java.nio.file.Path> stream = Files.newDirectoryStream(path, "ru.avilon.proxy*.{log,gz}")) {
			for (java.nio.file.Path entry : stream) {
				fileList.add(entry.getFileName().toString());
			}
		} catch (Exception e) {
			logger.error(e.toString() ,e);
			throw new WebApplicationException(e);
		}
		fileList.sort((p1, p2) -> p1.compareTo(p2));
		return Response.ok().entity(fileList).build();
	}
	
	@GET
	@Path("/log/{fileName}")
	public Response getLogFileContent(@PathParam("fileName") String fileName, @QueryParam("limit") Integer limit, @QueryParam("regexp") String regexp) {
		final List<String> lines = new ArrayList<>();
		try {
			java.nio.file.Path path = getLogfilePath();
			java.nio.file.Path entry = Paths.get(new URI(path.toUri().toString().concat(fileName)));
			StringBuilder strBuilder = new StringBuilder();
			try (Stream<String> fileStream = entry.getFileName().toString().endsWith(".gz") ? GZIPFiles.lines(entry) : Files.lines(entry, StandardCharsets.UTF_8)) {
				fileStream.forEach(new Consumer<String>() {
					@Override
					public void accept(String t) {
						if(t.matches("^\\d{2}:\\d{2}:\\d{2}\\.\\d{3}.*") && strBuilder.length() > 0) {
							lines.add(strBuilder.toString());
							strBuilder.setLength(0);
						}
						strBuilder.append(t).append(System.lineSeparator());
					}
				});
				if(strBuilder.length() > 0)
					lines.add(strBuilder.toString());
		}
		} catch (Exception e) {
			logger.error(e.toString() ,e);
			throw new WebApplicationException(e);
		}
		
		if (regexp != null) {
			final Pattern p = Pattern.compile(regexp);
			final List<String> filtered = lines.stream().filter(new Predicate<String>() {
					@Override
					public boolean test(String t) {
						Matcher m = p.matcher(t);
						return m.find();
					}
				}).collect(Collectors.toList());
			lines.clear();
			lines.addAll(filtered);
		}
		
		if(limit != null) {
			final List<String> limited = lines.stream().limit(limit).collect(Collectors.toList());
			lines.clear();
			lines.addAll(limited);
		}
		
		return Response.ok().entity(lines).build();
	}


	@SuppressWarnings("rawtypes")
	private java.nio.file.Path getLogfilePath() {
		java.nio.file.Path path = null;
		String logPathStr = System.getProperty("ru.avoilon.proxy.log.path");
		if(StringUtils.isBlank(logPathStr)) {;
			ch.qos.logback.classic.Logger reqRespLogger = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(ProxyLoggingFilter.class);
			RollingFileAppender appender = (RollingFileAppender)reqRespLogger.getAppender("REQ_RESP");
			path = Paths.get(appender.getFile()).getParent();
		} else {
			path = Paths.get(logPathStr, new String[] {}); 
		}
		return path;
	}
	

}
