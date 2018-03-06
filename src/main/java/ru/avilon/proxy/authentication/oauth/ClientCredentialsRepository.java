package ru.avilon.proxy.authentication.oauth;


public interface ClientCredentialsRepository {

	boolean clientIdExists(String clientId);

	boolean clientAllowed(String clientId, String clientSecret);

}