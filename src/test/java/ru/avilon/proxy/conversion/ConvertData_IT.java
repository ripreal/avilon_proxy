package ru.avilon.proxy.conversion;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.core.SecurityContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.json.JSONObject;
import org.json.XML;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.ScriptableObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.restassured.internal.path.xml.XmlPrettifier;

import groovy.util.XmlParser;
import ru.avilon.proxy.entities.ProxyObject;
import ru.avilon.proxy.entities.User;
import ru.avilon.proxy.repo.CassandraDataStore;
import ru.avilon.proxy.repo.MetadataRepository;
import ru.avilon.proxy.rest.Changes;
import ru.avilon.proxy.rest.configuration.TestDependencyBinder;
import ru.avilon.proxy.setup.SetupCassandra;
import ru.avilon.proxy.utils.XMLUtils;

public class ConvertData_IT {
	
	final static Logger logger = LoggerFactory.getLogger(ConvertData_IT.class);
	
	static ServiceLocator serviceLocator = ServiceLocatorUtilities.bind(new TestDependencyBinder());
	
	static CassandraDataStore cassandraDataStore = serviceLocator.getService(CassandraDataStore.class, new Annotation[] {});
	
	static Converter converter = serviceLocator.getService(Converter.class, new Annotation[] {});
	
	static ObjectMapper mapper = serviceLocator.getService(ObjectMapper.class, new Annotation[] {});
	
	static SecurityContext securityContext = serviceLocator.getService(SecurityContext.class, new Annotation[] {});
	
	
	static Changes changes = serviceLocator.getService(Changes.class, new Annotation[] {});

	@BeforeClass
	public static void setUp() throws Exception {
		try {
			cassandraDataStore.getNewSession();
		}
		catch (Exception e) {
			SetupCassandra.doSetup();
		}
	}

	@AfterClass
	public static void destroy() throws Exception {
		//SetupCassandra.doClear();
	}

	@Test
	public void convert_data() throws Exception {
		URL url = MetadataRepository.class.getResource("/js/conversion_rules/conversion_rules.js");
		Path dir = Paths.get(url.toURI());
		String script = new String(Files.readAllBytes(dir));
		Context context = Context.enter();
		try {
			List<ProxyObject> users = cassandraDataStore.getProxyObjectsByCriteria("mobile_app", "user", null, null, null);
			
    	 	ScriptableObject scope = context.initStandardObjects();
    	 	context.evaluateString(scope, script, "script", 1, null);
    	 	
    	 	NativeObject obj = (NativeObject) scope.get("conversionFunctions");
    	 	obj = (NativeObject) obj.get("to_xml");
    	 	Function fct = (Function) obj.get("user");
    	 	
    	    Object result = fct.call(context, scope, scope, new Object[] {mapper.writeValueAsString(users.get(0))});
    	    String converted = (String) Context.jsToJava(result, String.class);
    	    JSONObject convertedJson = new JSONObject(converted);

    	    String xml = XML.toString(convertedJson);
    	    System.out.println(convertedJson);
    	    System.out.println(xml);
    	    
    	    DocumentBuilderFactory dbFactory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
    	    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
    	    
    	    
    	    Element element = XMLUtils.initNewDocumentAndCreateDataElement(0l);
    	    Document doc = element.getOwnerDocument();
    	    
    	    Document doc2 = dBuilder.parse(new ByteArrayInputStream(xml.getBytes()));
    	    Node imported = doc.importNode(doc2.getDocumentElement(), true);
    	    element.appendChild(imported);
    	    
    	    Document doc3 = dBuilder.parse(new ByteArrayInputStream(xml.getBytes()));
    	    Node imported2 = doc.importNode(doc3.getDocumentElement(), true);
    	    element.appendChild(imported2);
    	    
    	    
    	    String output = XMLUtils.serializeXml(doc);
    		XmlParser parser = new XmlParser();
    		System.out.println(XmlPrettifier.prettify(parser, output));
    		
    		obj = (NativeObject) scope.get("conversionFunctions");
    		obj = (NativeObject) obj.get("to_json");
    	 	Function fct2 = (Function)obj.get("v8:CatalogObject.Пользователи");
    		
    		Object resultJson = fct2.call(context, scope, scope, new Object[] {XML.toJSONObject(xml).toString()});
    		String convertedJSON = (String) Context.jsToJava(resultJson, String.class);
    		System.err.println(convertedJSON);
    	    JSONObject convertedJson2 = new JSONObject(convertedJSON);
    	    System.err.println(convertedJson2.toString());
    	    
        } finally {
            Context.exit();
        }
	}
	
	@Test
	public void test_conversion_rules() throws Exception {

		String packageString = getResourceAsString("/test-packages/tasks_full_new.xml");
		changes.sendChangesXml(packageString);

		List<ProxyObject> tasks = cassandraDataStore.getProxyObjectsByCriteria("mobile_app", "task_result", null, null, 1);

		String objectBody = mapper.writeValueAsString(tasks.get(0));
		System.err.println(objectBody);
		String to = converter.convertTo("mobile_app", objectBody);
		System.err.println(to);
		    
		String from = converter.convertFrom("mobile_app", to);
		System.err.println(from);
	}
	
	@Test
	public void test_dissmiss() throws Exception {

		SetupCassandra.doSetupConversionScripts();
		
		String packageString = getResourceAsString("/test-packages/staff_pack.xml");
		changes.sendChangesXml(packageString);
		
		User user = cassandraDataStore.getUser("3333");
		assertTrue(user.getRoles().contains("user"));
		
		packageString = getResourceAsString("/test-packages/staffdismiss.xml");
		changes.sendChangesXml(packageString);
		
		user = cassandraDataStore.getUser("3333");
		assertFalse(user.getRoles().contains("user"));
		
	}
	
	@Test
	public void test_task_deletion_mark() throws Exception {
		SetupCassandra.doSetupConversionScripts();
		String packageString = getResourceAsString("/test-packages/tasks_full_new.xml");
		changes.sendChangesXml(packageString);
		
		ProxyObject proxyObject = cassandraDataStore.getProxyObject(UUID.fromString("804aacb0-0d3e-11e6-942a-005056ac0e86"), "mobile_app", "task");
		assertNotNull(proxyObject);

		/* It is not impletnted in default brhaviour
		String packageDeletionMarkString = getResourceAsString("/test-packages/task_deletion_mark.xml");
		changes.sendChangesXml(packageDeletionMarkString);
		
		ProxyObject proxyObjectDeleted = cassandraDataStore.getProxyObject(UUID.fromString("db4ae32c-6db4-11e6-80e9-40a8f0260031"), "mobile_app", "task");
		//assertNull(proxyObjectDeleted);
		*/
		
	}

	private String getResourceAsString(String fileName) throws URISyntaxException, IOException {
		URL url = MetadataRepository.class.getResource(fileName);
		Path packageFile = Paths.get(url.toURI());
		return new String(Files.readAllBytes(packageFile));
	}
	
	
	
	
	@Test
	public void test_conversion() throws Exception {
		SetupCassandra.doSetupConversionScripts();

		
		Changes changes = serviceLocator.getService(Changes.class, new Annotation[] {});
		URL url = null;
		Path packageFile = null;
		String packageString = null;
		
//		url = MetadataRepository.class.getResource("/test-packages/1c_pack2.txt");
//		packageFile = Paths.get(url.toURI());
//		packageString = new String(Files.readAllBytes(packageFile));
//		changes.sendChangesXml(packageString);
//		
		url = MetadataRepository.class.getResource("/test-packages/1c_pack3.xml");
		packageFile = Paths.get(url.toURI());
		packageString = new String(Files.readAllBytes(packageFile));
		changes.sendChangesXml(packageString);
		
		
		url = MetadataRepository.class.getResource("/test-packages/staff_pack.xml");
		packageFile = Paths.get(url.toURI());
		packageString = new String(Files.readAllBytes(packageFile));
		changes.sendChangesXml(packageString);
///		
		url = MetadataRepository.class.getResource("/test-packages/tasks_full.xml");
		packageFile = Paths.get(url.toURI());
		packageString = new String(Files.readAllBytes(packageFile));
		changes.sendChangesXml(packageString);
//		
		url = MetadataRepository.class.getResource("/test-packages/task_personal.xml");
		packageFile = Paths.get(url.toURI());
		packageString = new String(Files.readAllBytes(packageFile));
		changes.sendChangesXml(packageString);
		
		url = MetadataRepository.class.getResource("/test-packages/tasks_full_new.xml");
		packageFile = Paths.get(url.toURI());
		packageString = new String(Files.readAllBytes(packageFile));
		changes.sendChangesXml(packageString);
		
//		url = MetadataRepository.class.getResource("/bug_packages/staffmoves2015.xml");
//		packageFile = Paths.get(url.toURI());
//		packageString = new String(Files.readAllBytes(packageFile));
//		changes.sendChangesXml(packageString);
		
//		url = MetadataRepository.class.getResource("/bug_packages/test.json");
//		packageFile = Paths.get(url.toURI());
//		packageString = new String(Files.readAllBytes(packageFile));
//		changes.sendChanges(packageString);
		
		
//		XmlParser parser = new XmlParser();
//		groovy.util.Node docNode = parser.parseText(packageString);
//		groovy.util.NodeList dataNodeList = (groovy.util.NodeList)docNode.get("V8Exch:Data");
//		if(dataNodeList.size() != 1)
//			throw new Exception("invalid xml data");
//		groovy.util.Node dataNode = (groovy.util.Node) dataNodeList.get(0);
//		List<groovy.util.Node> childNodes = dataNode.children();
//		List<ProxyObject> recievedProxyObjectList = new ArrayList<>();
//		
//		for(groovy.util.Node xmlObject : childNodes) {
//			String objectBody = XmlUtil.serialize(xmlObject);
//			String jsonObject = converter.convertFrom("1c", XML.toJSONObject(objectBody).toString());
//			ProxyObject proxyObject = mapper.readValue(jsonObject, ProxyObject.class);
//			proxyObject.setClientapp("1c");
//			cassandraDataStore.saveObject(proxyObject);
//			recievedProxyObjectList.add(proxyObject);
//		}
//		converter.conversionTrigger(recievedProxyObjectList, "1c");
	}

	
	@Test
	public void testFilter() throws Exception {
		SetupCassandra.doSetupFilterScripts();
		User user = ((User)securityContext.getUserPrincipal());
		User cassanraUser = cassandraDataStore.getUser("3333");
		user.setJson_object_uuid(cassanraUser.getJson_object_uuid());
		user.setName(cassanraUser.getName());
		user.setRoles(cassanraUser.getRoles());
		
		
		
		List<ProxyObject> objects = cassandraDataStore.getProxyObjectsByCriteria("mobile_app", Collections.singleton("task"), null, true, null);
//		objects.addAll(objects);
//		objects.addAll(objects);
//		objects.addAll(objects);
		
		long start = System.currentTimeMillis();		
		List<ProxyObject> newObjects = converter.filter("mobile_app", objects);
		logger.info("{} ms {}, {}", System.currentTimeMillis() - start, objects.size(), newObjects.size());
//		
//		objects = cassandraDataStore.getProxyObjectsByCriteria("mobile_app", Collections.singleton("task"), null, null);
//		converter.filter("1c", objects);
	}
	
	@Test
	public void testConvertToXML() throws Exception {
		ProxyObject object = cassandraDataStore.getProxyObject(UUID.fromString("21f7ed6d-0d3e-11e6-942a-005056ac0e86"), "1c", "v8:DocumentObject.ЗаказНарядНаРаботы");
		JSONObject json = new JSONObject(object.getData());
		String xml = XML.toString(json);
		
		String attrspattern = "<([xmlns:|xsi:][^>]+)>([^<]*)<\\/[^>]+>";
		
		
		
		
//		xml = xml.replaceFirst(">", " xmlns:v8=\"http://v8.1c.ru/8.1/data/enterprise/current-config\">");
		System.err.println(xml);
		
		Pattern pattern = Pattern.compile(attrspattern);
	    Matcher matcher = pattern.matcher(xml);
	    // Check all occurrences
	    while (matcher.find()) {
	        System.out.print("Start index: " + matcher.start());
	        System.out.print(" End index: " + matcher.end());
	        System.out.print(" tag: " + matcher.group(1));
	        System.out.print(" value: " + matcher.group(2));
	        System.out.println(" Found: " + matcher.group());
	        
	        String attr = matcher.group(1) + "=\"" +matcher.group(2)+ "\"";
	        System.err.println("attr: " + attr);
	        StringBuilder sb = new StringBuilder(xml);	        
	        
	        sb.delete(matcher.start(),  matcher.end());
	        
	        sb.insert(matcher.start()-1, " " + attr);
	        
	        xml = sb.toString();
	        
	        matcher = pattern.matcher(xml);
	    }
	    
	    XmlParser parser = new XmlParser();
		parser.parseText(xml);
		System.out.println(XmlPrettifier.prettify(parser, xml));
		
	}
	
	@Test
	public void testChangesXML() throws Exception {
		String objectType = null;
		XmlParser parser = new XmlParser();
		
		
		List<ProxyObject> objectList = cassandraDataStore.getProxyObjectsByCriteria("1c", objectType, null, null, null);
		
		
		for (ProxyObject object : objectList) {
			Element V8Exch_Data = XMLUtils.initNewDocumentAndCreateDataElement(0l);
			
			Document doc = V8Exch_Data.getOwnerDocument();
			
			JSONObject json = new JSONObject(object.getData());
			logger.info("parsing: {}", json.toString(4));
			String xml = XML.toString(json);

			//xml = XMLUtils.moveAttributesFromeNodes(xml);
			
			XMLUtils.appendNodeFromString(xml, V8Exch_Data);
			
			XMLUtils.moveNamespaceNodesToAttributes(doc.getDocumentElement());
			
			String output = XMLUtils.serializeXml(doc);
			System.err.println(output);
			System.err.println(XmlPrettifier.prettify(parser, output));
			
		}
		
		
	}
}
