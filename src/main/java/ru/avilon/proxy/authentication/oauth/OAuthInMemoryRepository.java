package ru.avilon.proxy.authentication.oauth;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class OAuthInMemoryRepository implements OAuthRepository {

	private Map<String, Code> codes = new ConcurrentHashMap<>();

	@Override
	public void putCode(Code code) {
		codes.put(code.getCode(), code);
	}
	
	@Override
	public Code getValidCode(String codeId) {
		Code code = codes.get(codeId);
		if(code == null) {
			return null;
		}
		else if(code.isExpired()) {
			codes.remove(codeId);
			return null;
		}
		return code;
	}
	
	
	
	
	private Map<String, OAuthTokenImpl> tokens = new ConcurrentHashMap<>();
	
	private Map<String, RefreshToken> refreshTokens = new ConcurrentHashMap<>();

	@Override
	public OAuthTokenImpl getValidToken(String tokenId) {
		OAuthTokenImpl token = tokens.get(tokenId);
		if(token == null) {
			return null;
		}
		else if(token.isExpired()) {
			tokens.remove(token);
			return null;
		}
		return token;
	}
	

	@Override
	public OAuthTokenImpl getToken(String tokenId) {
		return tokens.get(tokenId);
	}

	@Override
	public void putToken(OAuthTokenImpl token) {
		tokens.put(token.getAccessToken(), token);
		
	}

	@Override
	public RefreshToken getRefreshToken(String tokenId) {
		return refreshTokens.get(tokenId);
		
	}

	@Override
	public void putRefreshToken(RefreshToken refreshToken) {
		refreshTokens.put(refreshToken.getTokenId(), refreshToken);
		
	}

	
	
	
}
