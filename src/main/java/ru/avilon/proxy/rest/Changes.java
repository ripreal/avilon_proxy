package ru.avilon.proxy.rest;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.fasterxml.jackson.databind.ObjectMapper;

import groovy.util.Node;
import groovy.util.NodeList;
import groovy.util.XmlParser;
import groovy.xml.XmlUtil;
import ru.avilon.proxy.conversion.Converter;
import ru.avilon.proxy.entities.FilterScript;
import ru.avilon.proxy.entities.ProxyObject;
import ru.avilon.proxy.repo.CassandraDataStore;
import ru.avilon.proxy.rest.configuration.annotations.CheckBasicAuthentication;
import ru.avilon.proxy.rest.configuration.annotations.CheckOAuthAuthentication;
import ru.avilon.proxy.rest.configuration.annotations.UseLogger;
import ru.avilon.proxy.utils.TimeUUIDUtils;
import ru.avilon.proxy.utils.XMLUtils;

@Path("/changes")
@RolesAllowed({"admin", "user"})
@UseLogger
public class Changes {
	
	Logger logger = LoggerFactory.getLogger(Changes.class);
	
	@Inject ObjectMapper mapper;
	
	@Inject CassandraDataStore cassandraDataStore;
	
	@Inject Converter converter;
	
	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@CheckOAuthAuthentication
	public Response getChanges(@QueryParam("objectType") String objectType, 
			@QueryParam("timeUUID") String timeUUID, @QueryParam("size") Integer size) throws Exception {
		
		final String clienApp = "mobile_app";
		
		if(logger.isDebugEnabled())
			logger.debug("request json changes timeUUID: {} objectType: {} size: {}", timeUUID, objectType, size);
		
		JSONObject pack = new JSONObject();
		JSONArray objects = new JSONArray();
		
		if(timeUUID != null)
			UUID.fromString(timeUUID);
		
		FilterScript script = cassandraDataStore.getFilterScript(clienApp);
		if(script != null)
			size = null;
		
		List<ProxyObject> objectList = cassandraDataStore.getProxyObjectsByCriteria(clienApp, objectType, timeUUID, true, size);
		
		objectList = converter.filter(clienApp, objectList);
		
		for(ProxyObject proxyObject : objectList) {
			objects.put(new JSONObject(proxyObject.getData()));
		}
		
		
		Optional<ProxyObject> latestObject = objectList.stream().max(new Comparator<ProxyObject>() {
			@Override
			public int compare(ProxyObject o1, ProxyObject o2) {
				return Long.compare(TimeUUIDUtils.getTimeFromUUID(o2.getTimeUUID()), TimeUUIDUtils.getTimeFromUUID(o1.getTimeUUID()));
			}
			
		});
		
		pack.put("timeUUID", latestObject.isPresent() ? latestObject.get().getTimeUUID() : TimeUUIDUtils.getCurrentTimeUUID().toString());
		
		pack.put("objects", objects);
		return Response.ok().entity(pack.toString()).build();
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@CheckOAuthAuthentication
	public Response sendChanges(String body) throws Exception {
		JSONObject pack = new JSONObject(body);
		if(logger.isDebugEnabled())
			logger.debug("got json changes: {}", pack);
		JSONArray objects = pack.getJSONArray("objects");
		
		List<ProxyObject> recievedProxyObjectList = new ArrayList<>();
		for(int i =  0; i < objects.length(); i++) {
			JSONObject jo = (JSONObject) objects.get(i);
			String convertedObject = converter.convertFrom("mobile_app", String.valueOf(jo));
			ProxyObject proxyObject = mapper.readValue(convertedObject, ProxyObject.class);
			if(StringUtils.isBlank(proxyObject.getClientapp())) {
				proxyObject.setClientapp("mobile_app");
			}
			cassandraDataStore.saveObject(proxyObject);
			recievedProxyObjectList.add(proxyObject);
		}
		converter.conversionTrigger(recievedProxyObjectList, "mobile_app");
		return Response.created(null).build();
	}

	@GET
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	@CheckBasicAuthentication
	public Response getChangesXml(@QueryParam("timestamp") Long timestamp, @QueryParam("packageNumber") Long packageNumber) {
		if(logger.isDebugEnabled())
			logger.debug("request xml changes packageNumber: {} ", packageNumber);
		
		final String clienApp = "1c";

		try {
			Element V8Exch_Data = XMLUtils.initNewDocumentAndCreateDataElement(packageNumber);
			
			String objectType = null;
			
			String timeUUIDStr = null;
			if(timestamp != null) {
				UUID timeUUID = TimeUUIDUtils.getTimeUUID(timestamp);
				timeUUIDStr = timeUUID.toString();
			}
			
			List<ProxyObject> objectList = cassandraDataStore.getProxyObjectsByCriteria(clienApp, objectType, timeUUIDStr, true, null);
			
			Document doc = V8Exch_Data.getOwnerDocument();
			for (ProxyObject object : objectList) {
				JSONObject json = new JSONObject(object.getData());
				logger.info("parsing: {}", json.toString(4));
				String xml = XML.toString(json);
				XMLUtils.appendNodeFromString(xml, V8Exch_Data);
			}
			
			XMLUtils.moveNamespaceNodesToAttributes(doc.getDocumentElement());
			
			String output = XMLUtils.serializeXml(doc);
			return Response.ok().entity(output).build();
		} catch(Exception e) {
			logger.error(e.toString(), e);
			throw new WebApplicationException(e.toString(), e);
		}
	}
	
	@SuppressWarnings("unchecked")
	@POST
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	@CheckBasicAuthentication
	public Response sendChangesXml(String objects) {
		if(logger.isDebugEnabled())
			logger.debug("got xml changes: {}", objects);
		try {
			XmlParser parser = new XmlParser();
			Node docNode = parser.parseText(objects.replaceAll("\\p{C}", ""));
			NodeList dataNodeList = (NodeList)docNode.get("V8Exch:Data");
			if(dataNodeList.size() != 1)
				throw new Exception("invalid xml data");
			Node dataNode = (Node) dataNodeList.get(0);
			List<Node> childNodes = dataNode.children();
			List<ProxyObject> recievedProxyObjectList = new ArrayList<>();
			
			for(Node xmlObject : childNodes) {
				String objectBody = XmlUtil.serialize(xmlObject);
				String jsonObject = converter.convertFrom("1c", XML.toJSONObject(objectBody).toString());
				if(StringUtils.isNotBlank(jsonObject)) {
					ProxyObject proxyObject = mapper.readValue(jsonObject, ProxyObject.class);
					proxyObject.setClientapp("1c");
					cassandraDataStore.saveObject(proxyObject);
					recievedProxyObjectList.add(proxyObject);
				}
			}
			converter.conversionTrigger(recievedProxyObjectList, "1c");
			
			return Response.created(null).build();
		} catch(Exception e) {
			logger.error("error processing xml data {}", objects, e);
			throw new WebApplicationException(e.toString(), e);
		}
	}
}
