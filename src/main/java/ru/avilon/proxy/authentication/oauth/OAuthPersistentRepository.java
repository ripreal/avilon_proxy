package ru.avilon.proxy.authentication.oauth;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.fasterxml.jackson.databind.ObjectMapper;

import ru.avilon.proxy.repo.CassandraDataStore;

@Singleton
public class OAuthPersistentRepository implements OAuthRepository {

	
	private CassandraDataStore cassandraDataStore;

	private ObjectMapper objectMapper;

	@Inject
	public OAuthPersistentRepository(CassandraDataStore cassandraDataStore, ObjectMapper objectMapper) {
		this.cassandraDataStore = cassandraDataStore;
		this.objectMapper = objectMapper;
	}
	
	@Override
	public void putCode(Code code) throws Exception {
		cassandraDataStore.saveOAuthObject(UUID.fromString(code.getCode()), "oauth_code", objectMapper.writeValueAsString(code), Math.toIntExact(code.getExpiresIn()));
	}
	
	@Override
	public Code getValidCode(String codeId) throws Exception {
		
		String marshalled = cassandraDataStore.getOAuthObjectData(UUID.fromString(codeId));
		if(marshalled == null) {
			return null;
		}
		Code code = objectMapper.readValue(marshalled, Code.class);
		if(code.isExpired()) {
			return null;
		}
		return code;
	}
	
	@Override
	public OAuthTokenImpl getValidToken(String tokenId) throws Exception {
		OAuthTokenImpl token = getToken(tokenId);
		if(token != null && token.isExpired()) {
			cassandraDataStore.removeOAuthObect(token.getAccessToken());
			return null;
		}
		return token;
	}
	

	@Override
	public OAuthTokenImpl getToken(String tokenId) throws Exception {
		String marshalled = cassandraDataStore.getOAuthObjectData(UUID.fromString(tokenId));
		if(marshalled == null) {
			return null;
		}
		return objectMapper.readValue(marshalled, OAuthTokenImpl.class);
	}

	@Override
	public void putToken(OAuthTokenImpl token) throws Exception {
		cassandraDataStore.saveOAuthObject(UUID.fromString(token.getAccessToken()), "access_token", objectMapper.writeValueAsString(token), Math.toIntExact(token.getExpiresIn()));
	}

	@Override
	public RefreshToken getRefreshToken(String tokenId) throws Exception {
		String marshalled = cassandraDataStore.getOAuthObjectData(UUID.fromString(tokenId));
		if(marshalled == null) {
			return null;
		}
		return objectMapper.readValue(marshalled, RefreshToken.class);
		
	}

	@Override
	public void putRefreshToken(RefreshToken refreshToken) throws Exception {
		cassandraDataStore.saveOAuthObject(UUID.fromString(refreshToken.getTokenId()), "refresh_token", objectMapper.writeValueAsString(refreshToken), 0);
	}

	
	
	
}
