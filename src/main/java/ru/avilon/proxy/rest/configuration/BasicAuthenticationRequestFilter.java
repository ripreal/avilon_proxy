package ru.avilon.proxy.rest.configuration;

import java.io.IOException;
import java.util.Base64;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.avilon.proxy.entities.User;
import ru.avilon.proxy.repo.CassandraDataStore;
import ru.avilon.proxy.rest.configuration.annotations.CheckBasicAuthentication;

@CheckBasicAuthentication
@Priority(Priorities.AUTHENTICATION)
public class BasicAuthenticationRequestFilter implements ContainerRequestFilter {

	Logger logger = LoggerFactory.getLogger(BasicAuthenticationRequestFilter.class);
	
	@Inject CassandraDataStore cassandraDataStore;
	
	public void filter(ContainerRequestContext requestContext) throws IOException {

		boolean authorized = false;
		String authHeader = StringUtils.defaultString(requestContext.getHeaderString("Authorization"));
		User user = null;
		if(authHeader.startsWith("Basic")) {
			String base64Credentials = authHeader.substring("Basic".length()).trim();
			String credentials = new String(Base64.getDecoder().decode(base64Credentials));
			final String[] values = credentials.split(":", 2);
			
			user = cassandraDataStore.getUser(values[0]);
			authorized = (user != null && StringUtils.equals(user.getPassword(), values[1]));
			
		}
		
		
		if(!authorized)
			requestContext.abortWith(
				Response.status(Response.Status.UNAUTHORIZED)
					.entity("User cannot access the resource.")
					.header("WWW-Authenticate", "Basic realm=\"myRealm\"")
					.build());
		
		else {
			try {
				requestContext.setSecurityContext(new ProxySecurityContext(user, requestContext.getSecurityContext().isSecure(), SecurityContext.BASIC_AUTH)); 
			} catch (Exception e) {
				logger.error(e.toString(), e);
			}
		}
	}
}
