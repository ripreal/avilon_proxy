package ru.avilon.proxy.rest.configuration;

import java.io.IOException;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.utils.OAuthUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.avilon.proxy.authentication.oauth.OAuthRepository;
import ru.avilon.proxy.authentication.oauth.OAuthTokenImpl;
import ru.avilon.proxy.entities.User;
import ru.avilon.proxy.repo.CassandraDataStore;
import ru.avilon.proxy.rest.configuration.annotations.CheckOAuthAuthentication;
import ru.avilon.proxy.utils.Constants;

@CheckOAuthAuthentication
@Priority(Priorities.AUTHENTICATION) 
public class OAuthRequestFilter implements ContainerRequestFilter {
	
	Logger logger = LoggerFactory.getLogger(OAuthRequestFilter.class);
	
	@Inject OAuthRepository oauthRepository;
	@Inject CassandraDataStore cassandraDataStore;
	
	public void filter(ContainerRequestContext requestContext) throws IOException {

		boolean authorized = false;
		String authHeader = StringUtils.defaultString(requestContext.getHeaderString(OAuth.HeaderType.AUTHORIZATION));
		OAuthTokenImpl oauthToken = null;
		if (authHeader.startsWith(OAuthUtils.AUTH_SCHEME)) {
			String token = OAuthUtils.getAuthHeaderField(authHeader);
			if(StringUtils.isBlank(token) || !token.matches(Constants.UUID_PATTERN)) {
				requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).entity("User cannot access the resource.").build());
				return;
			}
				
				
			try {
				oauthToken = oauthRepository.getValidToken(token);
				authorized = oauthToken != null;
			} catch (Exception e) {
				logger.error("error getting valid token ", e);
				throw new RuntimeException(e);
			}
		}
		if(!authorized)
			requestContext.abortWith(
				Response.status(Response.Status.UNAUTHORIZED).entity("User cannot access the resource.").build());
		
		else {
			try {
				User user = cassandraDataStore.getUser(oauthToken.getProperty("username"));
				requestContext.setSecurityContext(new ProxySecurityContext(user, requestContext.getSecurityContext().isSecure(), "OAUTH")); 
			} catch (Exception e) {
				logger.error(e.toString(), e);
			}
		}
	}
}
