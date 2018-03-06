package ru.avilon.proxy.authentication.oauth;

import java.util.HashMap;
import java.util.Map;


public class ClientCredentialsInMemoryRepository implements ClientCredentialsRepository {

	private Map<String, String> credentials = new HashMap<>();

	public ClientCredentialsInMemoryRepository() {
		credentials.put("test", "test");
	}
	
	/* (non-Javadoc)
	 * @see ru.avilon.proxy.authentication.oauth.ClientCredentialsRepository#clientIdExists(java.lang.String)
	 */
	@Override
	public boolean clientIdExists(String clientId) {
		return credentials.containsKey(clientId);
	}
	
	/* (non-Javadoc)
	 * @see ru.avilon.proxy.authentication.oauth.ClientCredentialsRepository#clientAllowed(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean clientAllowed(String clientId, String clientSecret) {
		return credentials.containsKey(clientId) && credentials.get(clientId).equals(clientSecret);
	}
}
