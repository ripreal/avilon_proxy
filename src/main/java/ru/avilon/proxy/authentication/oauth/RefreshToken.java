package ru.avilon.proxy.authentication.oauth;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class RefreshToken {

	private String tokenId;
	private String accessToken;
	private String clientId;
	private String scope;
	
	private Map<String, String> properties = new HashMap<>();

	@JsonCreator
	public RefreshToken(@JsonProperty("tokenId") String tokenId,
			@JsonProperty("accessToken") String accessToken,
			@JsonProperty("clientId") String clientId,
			@JsonProperty("scope") String scope) {
		this.tokenId = tokenId;
		this.accessToken = accessToken;
		this.clientId = clientId;
		this.scope = scope;
	}
	
	public String getTokenId() {
		return tokenId;
	}
	
	public String getAccessToken() {
		return accessToken;
	}
	
	public String getClientId() {
		return clientId;
	}

	public String getScope() {
		return scope;
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
}
