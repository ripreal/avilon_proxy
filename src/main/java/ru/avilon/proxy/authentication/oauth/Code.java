package ru.avilon.proxy.authentication.oauth;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.apache.oltu.oauth2.common.exception.OAuthProblemException;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Code {
	
	private Map<String, String> properties = new HashMap<>();
	
	private String code;
	private String clientId;
	private long expiresIn = Integer.parseInt(System.getProperty(Code.class.getName().concat(".expiresIn"), String.valueOf(10 * 60))); //default 10 mi
	private long issued;
	private String state;
	private String redirect_uri;
	private String scope;

	
	@JsonCreator
	public Code(@JsonProperty("code") String code,
			@JsonProperty("clientId") String clientId,
			@JsonProperty("state") String state,
			@JsonProperty("redirectUri") String redirect_uri,
			@JsonProperty("scope") String scope) throws OAuthProblemException {

		this.code = code;
		this.clientId = clientId;
		this.state = state;
		try {
			this.redirect_uri = new URI(redirect_uri).toString();
		} catch (URISyntaxException e) {
			throw OAuthProblemException.error("got wrong redirect_uri", e.toString());
		}
		
		this.scope = scope;
		issued = System.currentTimeMillis();
	}
	
	public String getCode() {
		return code;
	}

	public long getExpiresIn() {
		return expiresIn;
	}

	public String getState() {
		return state;
	}

	public String getRedirectUri() {
		return redirect_uri;
	}

	public String getScope() {
		return scope;
	}
	
	public String getClientId() {
		return clientId;
	}

	public void setProperty(String prop, String value) {
		properties.put(prop, value);
	}
	
	public String getProperty(String prop) {
		return properties.get(prop);
	}
	
	public Map<String, String> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}

	@JsonIgnore
	public boolean isExpired() {
		return issued + expiresIn * 1000 < System.currentTimeMillis();
	}
	
	
	
}
