package ru.avilon.proxy.rest;

import static com.jayway.restassured.RestAssured.given;
import static org.junit.Assert.assertTrue;

import java.lang.annotation.Annotation;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import org.apache.oltu.oauth2.common.OAuth;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.internal.path.xml.XmlPrettifier;
import com.jayway.restassured.response.Header;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;

import groovy.util.XmlParser;
import ru.avilon.proxy.entities.ProxyObject;
import ru.avilon.proxy.repo.CassandraDataStore;
import ru.avilon.proxy.repo.MetadataRepository;
import ru.avilon.proxy.rest.configuration.DependencyBinder;
import ru.avilon.proxy.utils.XMLUtils;

public class RestXML_IT extends RestBase {
	Logger logger = LoggerFactory.getLogger(RestXML_IT.class); 
	
	static ServiceLocator serviceLocator = ServiceLocatorUtilities.bind(new DependencyBinder());
	
	static CassandraDataStore cassandraDataStore = serviceLocator.getService(CassandraDataStore.class, new Annotation[] {});


	static String encoding = Base64.getEncoder().encodeToString("user1c:user1c_password".getBytes());
			
	static Header authHeader = new Header("Authorization", "Basic " + encoding);
	
	
	
	@Test
	public void test_objects() throws Exception {
		RestAssured.config().getDecoderConfig().defaultCharsetForContentType(ContentType.XML);
		Response resp;
		RequestSpecification requestSpec = new RequestSpecBuilder().setContentType("application/xml; charset=UTF-8").setAccept(ContentType.XML).build();
		
		given(requestSpec).get("/changes").then().statusCode(401);
		given(requestSpec).post("/changes").then().statusCode(401);
		
		
		requestSpec.header(authHeader);
		resp = given(requestSpec).param("packageNumber", "1").get("/changes");
		resp.getBody().prettyPrint();
		resp.then().statusCode(200);
		
		Element V8Exch_Data = XMLUtils.initNewDocumentAndCreateDataElement(new Long(0));
		Document doc = V8Exch_Data.getOwnerDocument();

		List<ProxyObject> users = cassandraDataStore.getProxyObjectsByCriteria("mobile_app", "user", null, null, null);
		for (ProxyObject user : users) {
			Element v8_CatalogObject_Пользователи = doc.createElement("v8:CatalogObject.Пользователи");

			XMLUtils.setObjectProperty(doc, v8_CatalogObject_Пользователи, UUID.randomUUID().toString(), "v8:Ref");
			XMLUtils.setObjectProperty(doc, v8_CatalogObject_Пользователи, String.valueOf(user.getProperty("deleted")),"v8:DeletionMark");
			XMLUtils.setObjectProperty(doc, v8_CatalogObject_Пользователи, String.valueOf(user.getUuid()), "v8:Code");
			XMLUtils.setObjectProperty(doc, v8_CatalogObject_Пользователи, String.valueOf(user.getProperty("name")), "v8:Description");
			XMLUtils.setObjectProperty(doc, v8_CatalogObject_Пользователи, String.valueOf(user.getProperty("department")), "v8:Отдел");
			XMLUtils.setObjectProperty(doc, v8_CatalogObject_Пользователи, String.valueOf(user.getProperty("position")), "v8:Должность");
			XMLUtils.setObjectProperty(doc, v8_CatalogObject_Пользователи, String.valueOf(user.getProperty("tabnum")), "v8:ТабельныйНомер");
			XMLUtils.setObjectProperty(doc, v8_CatalogObject_Пользователи, String.valueOf("test"), "v8:Пароль");

			V8Exch_Data.appendChild(v8_CatalogObject_Пользователи);
		}
		
		String output = XMLUtils.serializeXml(doc);
		XmlParser parser = new XmlParser();
		resp = given(requestSpec).content(output).post("/changes");
		resp.getBody().prettyPrint();
		resp.then().statusCode(201);
		System.out.println(XmlPrettifier.prettify(parser, output));
		
	}
	
	
	@Test
	public void test_complex_objects() throws Exception {
		
		RestAssured.config().getDecoderConfig().defaultCharsetForContentType(ContentType.XML);
		Response resp;
		RequestSpecification requestSpec = new RequestSpecBuilder().setContentType("application/xml; charset=UTF-8").setAccept(ContentType.XML).build();
		requestSpec.header(authHeader);
		
		URL url = MetadataRepository.class.getResource("/test-packages/1c_pack2.txt");
		Path packageFile = Paths.get(url.toURI());
		String packageString = new String(Files.readAllBytes(packageFile));
	
		resp = given(requestSpec).content(packageString).post("/changes");
		resp.getBody().prettyPrint();
		resp.then().statusCode(201);
		
		String accessToken = Rest_IT.doAuthenticate();
		Header authHeaderOAuth= new Header(OAuth.HeaderType.AUTHORIZATION, "Bearer ".concat(accessToken));
		
		resp = given().header(authHeaderOAuth).contentType(ContentType.JSON).parameter("objectType", "user").get("/changes");
		
		resp.then().statusCode(200);
		JSONObject recievedPackage = new JSONObject(resp.getBody().prettyPrint());
		JSONArray objects = recievedPackage.getJSONArray("objects");
		
		boolean found = false;
		
		for(int i = 0; i < objects.length(); i ++) {
			JSONObject object = objects.getJSONObject(i);
			if(object.getString("uuid").equals("93dae5a0-0bd3-11e6-80d9-40a8f0260033")) {
				found = true;
			}
		}
		
		assertTrue(found);
	}
	
}
