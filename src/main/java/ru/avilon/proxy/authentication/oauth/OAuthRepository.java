package ru.avilon.proxy.authentication.oauth;

public interface OAuthRepository {
	
	public void putCode(Code code) throws Exception;
	
	public Code getValidCode(String codeId) throws Exception;

	public OAuthTokenImpl getValidToken(String tokenId) throws Exception;
	
	public OAuthTokenImpl getToken(String tokenId) throws Exception;
	
	public void putToken(OAuthTokenImpl token) throws Exception;
	
	public RefreshToken getRefreshToken(String tokenId) throws Exception;

	public void putRefreshToken(RefreshToken refreshToken) throws Exception;
	
}