package ru.avilon.proxy.rest;

import static com.jayway.restassured.RestAssured.get;
import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.annotation.Annotation;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.oltu.oauth2.common.OAuth;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Header;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;

import ru.avilon.proxy.entities.ProxyObject;
import ru.avilon.proxy.entities.User;
import ru.avilon.proxy.repo.CassandraDataStore;
import ru.avilon.proxy.repo.MetadataRepository;
import ru.avilon.proxy.rest.configuration.TestDependencyBinder;

public class Rest_IT extends RestBase {
	Logger logger = LoggerFactory.getLogger(Rest_IT.class);
	
	static ServiceLocator serviceLocator = ServiceLocatorUtilities.bind(new TestDependencyBinder());
	
	static MetadataRepository metadataRepo = serviceLocator.getService(MetadataRepository.class, new Annotation[] {});
	
	static CassandraDataStore cassandraDataStore = serviceLocator.getService(CassandraDataStore.class, new Annotation[] {});
	
	static ObjectMapper mapper = serviceLocator.getService(ObjectMapper.class, new Annotation[] {});
	
	public static String doAuthenticate() throws URISyntaxException {
		
		Response resp = given().redirects().follow(false)
				.parameter("client_id", "test")
				.parameter("redirect_uri", "akitaproxy://authorize")
				.parameter("response_type", "code")
				.parameter("username", "11111")
				.parameter("password", "test")
				.parameter("scope", "task message geo")
				.parameter("state", "XaguDag2LN")
				.get("/oauth/authorize");
		
		URI uri = new URI(resp.getHeader("Location"));
		List<NameValuePair> parsed = URLEncodedUtils.parse(uri.getQuery(), Charset.defaultCharset());
		
		
		String code = null;
		for(NameValuePair nvp : parsed) {
			if(nvp.getName().equals("code"))
				code = nvp.getValue();
		}
		
		resp = given().redirects().follow(false)
				.parameter("client_id", "test")
				.parameter("client_secret", "test")
				.parameter("redirect_uri", "akitaproxy://authorize")
				.parameter("grant_type", "authorization_code")
				.parameter("code", code)
				.parameter("username", "test")
				.parameter("password", "password")
				.post("/oauth/access_token");
		
		
		
		JSONObject respJson = new JSONObject(resp.getBody().prettyPrint());
		String accessToken = respJson.getString("access_token");
		return accessToken;
	}
	
	@Test
	public void test_authentication() throws Exception {
		
		given().redirects().follow(false).get("/oauth/authorize").then().assertThat().statusCode(400);
		
		//test roles
		User user = cassandraDataStore.getUser("11111");
		user.getRoles().clear();
		cassandraDataStore.saveUser(user);
		
		given().redirects().follow(false)
			.parameter("client_id", "test")
			.parameter("redirect_uri", "akitaproxy://authorize")
			.parameter("response_type", "code")
			.parameter("username", "11111")
			.parameter("scope", "task message geo")
			.parameter("state", "XaguDag2LN")
			.get("/oauth/authorize").then().assertThat().statusCode(403);
		
		user.getRoles().add("user");
		cassandraDataStore.saveUser(user);
		
		given().redirects().follow(false)
				.parameter("client_id", "bad")
				.parameter("redirect_uri", "akitaproxy://authorize")
				.parameter("response_type", "code")
				.parameter("username", "11111")
				//.parameter("password", "test_password")
				.parameter("scope", "task message geo")
				.parameter("state", "XaguDag2LN")
				.get("/oauth/authorize").then().assertThat().statusCode(303).and().header("Location", containsString("?error"));
		
		String accessToken = doAuthenticate();
		
		Response resp = given().redirects().follow(false).parameter("token", accessToken).post("/oauth/introspect");
		resp.then().statusCode(200);
		JSONObject jsonResp = new JSONObject(resp.getBody().prettyPrint());
		assertTrue(jsonResp.length() > 0);
		
		//test roles
		user.getRoles().clear();
		cassandraDataStore.saveUser(user);
		
		Header authHeader = new Header(OAuth.HeaderType.AUTHORIZATION, "Bearer ".concat(accessToken));
		
		resp =  given().contentType(ContentType.JSON).header(authHeader).get("/changes");
		resp.then().statusCode(403);
		
		user.getRoles().add("user");
		cassandraDataStore.saveUser(user);
		
		resp =  given().contentType(ContentType.JSON).header(authHeader).get("/changes");
		resp.then().statusCode(200);
		
		RequestSpecification requestSpec = new RequestSpecBuilder().setContentType("application/xml; charset=UTF-8").setAccept(ContentType.XML).build();
		String encoding = Base64.getEncoder().encodeToString("user1c:user1c_password".getBytes());
		authHeader = new Header("Authorization", "Basic " + encoding);
		requestSpec.header(authHeader);
		
		User user1c = cassandraDataStore.getUser("user1c");
		user1c.getRoles().clear();
		cassandraDataStore.saveUser(user1c);
		
		given(requestSpec).get("/changes").then().statusCode(403);
		
		
		user1c.getRoles().add("user");
		cassandraDataStore.saveUser(user1c);
		given(requestSpec).get("/changes").then().statusCode(200);
		
	}
	
	@Test
	public void test_objects() throws Exception {
		
		String accessToken = doAuthenticate();
		Header authHeader = new Header(OAuth.HeaderType.AUTHORIZATION, "Bearer ".concat(accessToken));
		
		Response resp;
		resp = get("/changes");
		resp.then().statusCode(401);
		
		resp = given().contentType(ContentType.JSON).header(authHeader).get("/changes");
		
		
		JSONObject pk = new JSONObject(resp.getBody().prettyPrint());
		assertTrue(pk.get("timeUUID") != null);
		
		//создание пользователя
		
		JSONObject user = new JSONObject();
		user.put("uuid", UUID.randomUUID().toString());
		user.put("objectType", "user");
		user.put("name", "Sergeev");
		JSONArray objects = new JSONArray();
		objects.put(user);
		JSONObject pack = new JSONObject();
		pack.put("objects", objects);
		
		resp = given().header(authHeader).contentType(ContentType.JSON).content(pack.toString()).post("/changes");
		resp.then().statusCode(201);
		
		
		boolean found = false;
		resp = given().header(authHeader).contentType(ContentType.JSON).parameter("objectType", "user").get("/changes");
		resp.then().statusCode(200);
	
		JSONObject recievedPackage = new JSONObject(resp.getBody().prettyPrint());
		objects = recievedPackage.getJSONArray("objects");
		assertTrue(objects.length() > 0);
		
		for(int i = 0; i < objects.length(); i ++) {
			JSONObject objUser = objects.getJSONObject(i);
			assertEquals(objUser.getString("objectType"), "user");
			if(objUser.getString("uuid").equals(user.get("uuid"))) {
				found = true;
			}
		}
		
		assertTrue(found);

	
		user.put("name", "Vasin");
		resp = given().header(authHeader).contentType(ContentType.JSON).content(pack.toString()).post("/changes");
		resp.then().statusCode(201);
	
		found = false;
		resp = given().header(authHeader).contentType(ContentType.JSON).parameter("objectType", "user").get("/changes");
		resp.then().statusCode(200);
		
		recievedPackage = new JSONObject(resp.getBody().prettyPrint());
		objects = recievedPackage.getJSONArray("objects");
		assertTrue(objects.length() > 0);
		
		for(int i = 0; i < objects.length(); i ++) {
			JSONObject objUser = objects.getJSONObject(i);
			assertEquals(objUser.getString("objectType"), "user");
			if(objUser.getString("uuid").equals(user.get("uuid")) && "Vasin".equals(objUser.getString("name"))) {
				found = true;
			}
		}
		
		assertTrue(found);

		JSONObject deletedUser = new JSONObject();
		deletedUser.put("uuid", UUID.randomUUID().toString());
		deletedUser.put("objectType", "user");
		deletedUser.put("deleted", true);
		JSONArray deletedObjects = new JSONArray();
		deletedObjects.put(deletedUser);
		JSONObject deletedPackage = new JSONObject();
		deletedPackage.put("objects", deletedObjects);
//		JSONArray deletedPackages = new JSONArray();
//		deletedPackages.put(deletedPackage);
		
		resp = given().header(authHeader).contentType(ContentType.JSON).content(deletedPackage.toString()).post("/changes");
		resp.getBody().prettyPrint();
		resp.then().statusCode(201);
		
		JSONObject newUser = new JSONObject();
		newUser.put("objectType", "user");
		newUser.put("name", "Sidorov");
		JSONArray newObjects = new JSONArray();
		newObjects.put(newUser);
		JSONObject newPackage = new JSONObject();
		newPackage.put("objects", newObjects);
//		JSONArray newPackages = new JSONArray();
//		newPackages.put(newPackage);
		
		resp = given().header(authHeader).contentType(ContentType.JSON).content(newPackage.toString()).post("/changes");
		resp.getBody().prettyPrint();
		resp.then().statusCode(201);

		
		
//		получить пользователей
		resp = given().header(authHeader).parameter("objectType", "user").get("/changes");
		resp.getBody().prettyPrint();
		resp.then().statusCode(200);
		
//		получить задачи
		resp = given().header(authHeader).parameter("objectType", "task").get("/changes");
		String tasks = resp.getBody().prettyPrint();
		resp.then().statusCode(200);
		JSONObject j = new JSONObject(tasks);
		
		resp = given().header(authHeader).contentType(ContentType.JSON).content(j.toString(4)).post("/changes");
		resp.getBody().prettyPrint();
		resp.then().statusCode(201);

		
		//получить географическое положение
		resp = given().header(authHeader).parameter("objectType", "geo").get("/changes");
		resp.getBody().prettyPrint();
		resp.then().statusCode(200);
		
		//получить географическое положение
		resp = given().header(authHeader).parameter("objectType", "message").get("/changes");
		resp.getBody().prettyPrint();
		resp.then().statusCode(200);
		
//		получить группы задач
		resp = given().header(authHeader).parameter("objectType", "task_group").get("/changes");
		resp.getBody().prettyPrint();
		resp.then().statusCode(200);
		
		
	}
	
	
	@SuppressWarnings("unused")
	@Test
	public void test_rest_objects() throws Exception {
		
		String token =  doAuthenticate();
		
		Header authHeader = new Header(OAuth.HeaderType.AUTHORIZATION, "Bearer ".concat(token));
		Response resp = given().header(authHeader).parameter("objectType", "user").get("/changes");
		String users = resp.getBody().prettyPrint();
		resp.then().statusCode(200);
		
		resp = given().header(authHeader).parameter("objectType", "task").get("/changes");
		String tasks = resp.getBody().prettyPrint();
		resp.then().statusCode(200);
		
		resp = given().header(authHeader).contentType(ContentType.JSON).content(tasks).post("/changes");
		resp.getBody().prettyPrint();
		resp.then().statusCode(201);
		
		resp = given().header(authHeader).get("/changes");
		resp.getBody().prettyPrint();
		resp.then().statusCode(200);
		
		resp = given().header(authHeader).parameter("objectType", "message").get("/changes");
		String messages = resp.getBody().prettyPrint();
		resp.then().statusCode(200);
		
		resp = given().header(authHeader).parameter("objectType", "geo").get("/changes");
		String geo = resp.getBody().prettyPrint();
		resp.then().statusCode(200);
		
		resp = given().header(authHeader).parameter("objectType", "task_group").get("/changes");
		String task_group = resp.getBody().prettyPrint();
		resp.then().statusCode(200);
		
		resp = given().header(authHeader).get("/changes");
		resp.getBody().prettyPrint();
		resp.then().statusCode(200);
	}
	
	
	@Test
	public void test_geo() throws Exception {
		
		String accessToken = doAuthenticate();
		Header authHeader = new Header(OAuth.HeaderType.AUTHORIZATION, "Bearer ".concat(accessToken));
		
		Response resp;
		
		JSONObject geo = new JSONObject();
		geo.put("objectType", "geo");
		geo.put("latitude", 55.7703422);
		geo.put("longitude", 37.5955272);
		JSONArray objects = new JSONArray();
		objects.put(geo);
		JSONObject pack = new JSONObject();
		pack.put("objects", objects);
		
		resp = given().header(authHeader).contentType(ContentType.JSON).content(pack.toString()).post("/changes");
		resp.then().statusCode(201);
		
		
		boolean found = false;
		resp = given().header(authHeader).contentType(ContentType.JSON).parameter("objectType", "geo").get("/changes");
		resp.then().statusCode(200);
		
		JSONObject recievedPackage = new JSONObject(resp.getBody().prettyPrint());
		objects = recievedPackage.getJSONArray("objects");
		assertTrue(objects.length() > 0);
		
		for(int i = 0; i < objects.length(); i ++) {
			JSONObject geoObj = objects.getJSONObject(i);
			assertEquals(geoObj.getString("objectType"), "geo");
			if(geoObj.getDouble("latitude") == 55.7703422 &&  geoObj.has("uuid") && geoObj.has("timeUUID") && geoObj.has("userUUID")) {
				found = true;
			}
		}
		
		assertTrue(found);
	

	}
	
	@Test
	public void test_task() throws Exception {
		
		String accessToken = doAuthenticate();
		Header authHeader = new Header(OAuth.HeaderType.AUTHORIZATION, "Bearer ".concat(accessToken));
		
		Response resp;
		
//		resp = given().header(authHeader).contentType(ContentType.JSON).parameter("objectType", "task").get("/changes");
//		resp.then().statusCode(200);
//		JSONObject recievedPackage = new JSONObject(resp.getBody().prettyPrint());
//		JSONArray objects = recievedPackage.getJSONArray("objects");
//		assertEquals(objects.length(), 1);
		
		ProxyObject proxyObjectTask = cassandraDataStore.getProxyObject(UUID.fromString("21f7ed6d-0d3e-11e6-942a-005056ac0e86"), "mobile_app", "task");
		JSONArray objects = new JSONArray();
			
		
		JSONObject task = new JSONObject(proxyObjectTask.getData());
		
		long changeTime = System.currentTimeMillis() / 1000;
		
		objects.put(task);
		
		String taskResultUUID = UUID.randomUUID().toString();
		String taskUUID = task.getString("uuid");
		task.put("taskUUID", taskUUID);
		task.put("uuid", taskResultUUID);
		task.put("objectType", "task_result");
		
		JSONObject pack = new JSONObject();
		pack.put("objects", objects);
		
		resp = given().header(authHeader).contentType(ContentType.JSON).content(pack.toString()).post("/changes");
		resp.then().statusCode(201);
		
		resp = given().header(authHeader).contentType(ContentType.JSON).parameter("objectType", "task_result").get("/changes");
		resp.then().statusCode(200);
		JSONObject recievedPackage = new JSONObject(resp.getBody().prettyPrint());
		objects = recievedPackage.getJSONArray("objects");
		assertTrue(objects.length() > 0);
		
		boolean found = false;
		for(int i = 0; i < objects.length(); i ++) {
			JSONObject taskResult = objects.getJSONObject(i);
			assertEquals(taskResult.getString("objectType"), "task_result");
			if(taskResult.getString("uuid").equals(taskResultUUID) &&  taskResult.has("taskUUID") && taskResult.getString("taskUUID").equals(taskUUID)) {
				found = true;
			}
		}
		
		String encoding = Base64.getEncoder().encodeToString("user1c:user1c_password".getBytes());
		assertTrue(found);
		Header authHeader1c = new Header("Authorization", "Basic " + encoding);
		RestAssured.config().getDecoderConfig().defaultCharsetForContentType(ContentType.XML);
		RequestSpecification requestSpec = new RequestSpecBuilder().setContentType("application/xml; charset=UTF-8").setAccept(ContentType.XML).build();
		requestSpec.header(authHeader1c);
		resp = given(requestSpec).parameter("timestamp", changeTime).get("/changes");
		resp.getBody().prettyPrint();
		resp.then().statusCode(200);

	}

}
