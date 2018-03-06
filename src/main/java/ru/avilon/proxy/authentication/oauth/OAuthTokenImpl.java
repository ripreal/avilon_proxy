package ru.avilon.proxy.authentication.oauth;

import java.util.HashMap;
import java.util.Map;

import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.token.OAuthToken;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class OAuthTokenImpl implements OAuthToken {

	private String token;
	private String clientId;
	private long expiresIn = Integer.parseInt(System.getProperty(Code.class.getName().concat(".expiresIn"), String.valueOf(24 * 60 * 60))); //one day
	private long issued;
	private String scope;
	private String refreshToken;
	
	private Map<String, String> properties = new HashMap<>();
	

	public OAuthTokenImpl(String token, 
			String clientId, 
			String scope, 
			String refreshToken,
			Long issued) throws OAuthProblemException {
		this.token = token;
		this.clientId = clientId;
		this.scope = scope;
		this.refreshToken = refreshToken;
		this.issued = issued;
	}
	
	@JsonCreator
	public OAuthTokenImpl(@JsonProperty("accessToken") String token, 
			@JsonProperty("clientId") String clientId, 
			@JsonProperty("scope") String scope, 
			@JsonProperty("refreshToken") String refreshToken) throws OAuthProblemException {
		
		this(token, clientId, scope, refreshToken, System.currentTimeMillis());
	}

	
	@Override
	public String getAccessToken() {
		return token;
	}

	@Override
	public Long getExpiresIn() {
		return expiresIn;
	}

	@Override
	public String getRefreshToken() {
		return refreshToken;
	}

	@Override
	public String getScope() {
		return scope;
	}
	
	@JsonIgnore
	public boolean isExpired() {
		return issued + expiresIn * 1000 < System.currentTimeMillis();
	}
	
	@JsonIgnore
	public long getExpiresTimeStamp() {
		return issued + expiresIn;
	}
	
	
	public String getClientId() {
		return clientId;
	}
	
	public long getIssued() {
		return issued;
	}
	
	public void setIssued(long issued) {
		this.issued = issued;
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
