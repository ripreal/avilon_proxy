package ru.avilon.proxy.rest;

import java.net.URI;
import java.net.URISyntaxException;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.StringUtils;
import org.apache.oltu.oauth2.as.issuer.OAuthIssuer;
import org.apache.oltu.oauth2.as.request.OAuthAuthzRequest;
import org.apache.oltu.oauth2.as.response.OAuthASResponse;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.OAuthResponse;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import ru.avilon.proxy.authentication.oauth.ClientCredentialsRepository;
import ru.avilon.proxy.authentication.oauth.Code;
import ru.avilon.proxy.authentication.oauth.OAuthRepository;
import ru.avilon.proxy.authentication.oauth.OAuthTokenImpl;
import ru.avilon.proxy.authentication.oauth.RefreshToken;
import ru.avilon.proxy.authentication.oauth.ScopeChecker;
import ru.avilon.proxy.conversion.Converter;
import ru.avilon.proxy.entities.ProxyObject;
import ru.avilon.proxy.entities.User;
import ru.avilon.proxy.repo.CassandraDataStore;
import ru.avilon.proxy.rest.configuration.annotations.UseLogger;
import ru.avilon.proxy.utils.Constants;

@UseLogger
@Path("/oauth")
public class Authentication {
	
	static final Logger logger = LoggerFactory.getLogger(Authentication.class);
	
	static final String redirectUrl = "akitaproxy://authorize";
	
	@Inject ClientCredentialsRepository clientCredentialsRepository; 
	@Inject OAuthRepository oAuthRepository;
	@Inject OAuthIssuer oauthIssuerImpl;
	@Inject CassandraDataStore cassandraDataStore;
	@Inject ScopeChecker scopeChecker;
	@Inject Converter converter;
//	ScopeChecker scopeChecker =  new ScopeChecker();
	
	@GET
	@Path("/authorize")
	public Response authorize(@QueryParam("response_type") String responseType, 
			@QueryParam("client_id") String clientId, 
			@QueryParam("redirect_uri") String redirect_uri,
			@QueryParam("username") String username,
			@QueryParam("password") String password,
			@QueryParam("device_id") String device_id,
			@QueryParam("state") String state,
			@QueryParam("scope") String scope,
			@QueryParam("access_type") String access_type,
			@Context HttpServletRequest request, @Context HttpServletResponse response)
			throws OAuthSystemException, URISyntaxException {
//		
//		if(true)
//			throw new RuntimeException("unknown error");

		OAuthResponse resp = null;
		try {
			new URI(redirect_uri);
		} catch (Exception e) {
			return Response.status(Response.Status.BAD_REQUEST).build();
		}

		try {
			
			if(!scopeChecker.isScopeValid(scope)) {
				throw OAuthProblemException.error("Requested scope is invalid");
			}
			
			User user = cassandraDataStore.getUser(username);
			
			if(user == null)  
				throw OAuthProblemException.error("Invalid credentials");
			
			if(user.getRoles().isEmpty()) {
				throw new ForbiddenException();
			}
			
			if(StringUtils.isNotBlank(user.getPassword()) || StringUtils.isNotBlank(password)) {
				if(StringUtils.equals(user.getPassword(), password))
					throw OAuthProblemException.error("Invalid credentials");
			}
			
			new OAuthAuthzRequest(request);
			
			if(clientCredentialsRepository.clientIdExists(clientId)) {
				Code code = new Code(oauthIssuerImpl.authorizationCode(), clientId, state, redirect_uri, scope);
				code.setProperty("username", username);
				oAuthRepository.putCode(code);
				resp = OAuthASResponse.authorizationResponse(request, HttpServletResponse.SC_FOUND)
					.setCode(code.getCode())
					.setExpiresIn(code.getExpiresIn())
					.setParam("state", code.getState())
					.location(code.getRedirectUri())
					.buildQueryMessage();
			} else {
				throw OAuthProblemException.error("Unknown client id");
			}
			
			//return Response.status(resp.getResponseStatus()).entity(resp.getBody().toString()).build();
		} catch (OAuthProblemException ex) {
			resp = OAuthASResponse.errorResponse(HttpServletResponse.SC_FOUND).error(ex)
					.location(redirect_uri)
					//.buildJSONMessage();
					.buildQueryMessage();
			
		} catch(WebApplicationException e) {
			throw e;
		} catch (Exception e) {
			logger.warn(e.toString(), e);
			throw new WebApplicationException(e);
		}
		return Response.seeOther(new URI(resp.getLocationUri())).build();
	}
	
	@POST
	@Path("/access_token")
	public Response getToken(@FormParam("redirect_uri") String redirect_uri,
			@FormParam("grant_type") String grant_type,
			@FormParam("code") String code,
			@FormParam("client_id") String client_id,
			@FormParam("client_secret") String client_secret,
			@Context HttpServletRequest request, @Context HttpServletResponse response) throws OAuthSystemException, URISyntaxException {
		OAuthResponse resp = null;
		try {
			if(StringUtils.isBlank(code))
				return Response.status(Response.Status.BAD_REQUEST).build();
			
			if(!code.matches(Constants.UUID_PATTERN))
				return Response.status(Response.Status.BAD_REQUEST).build();
			
			
			Code issuedCode = oAuthRepository.getValidCode(code);
			
			if(issuedCode == null) {
				throw OAuthProblemException.error("Code expired");
			} else if(!issuedCode.getRedirectUri().equals(redirect_uri)) {
				throw OAuthProblemException.error("Invalid redirect uri");
			} else if(!"authorization_code".equals(grant_type)) {
				throw OAuthProblemException.error("Invalid grant type");
			} else if(!issuedCode.getClientId().equals(client_id)) {
				throw OAuthProblemException.error("Invalid client_id");
			} else if(!clientCredentialsRepository.clientAllowed(client_id, client_secret)) {
				throw OAuthProblemException.error("Invalid credentials");
			}
			
			OAuthTokenImpl token = new OAuthTokenImpl(oauthIssuerImpl.accessToken(), client_id, issuedCode.getScope(), oauthIssuerImpl.refreshToken(), System.currentTimeMillis());
			token.setProperty("username", issuedCode.getProperty("username"));
			oAuthRepository.putToken(token);
			
			RefreshToken refreshToken = new RefreshToken(token.getRefreshToken(), token.getAccessToken(), client_id, issuedCode.getScope());
			refreshToken.setProperty("username", issuedCode.getProperty("username"));
			
			oAuthRepository.putRefreshToken(refreshToken);
			
			resp = OAuthASResponse
	                .tokenResponse(HttpServletResponse.SC_OK)
	                .setAccessToken(token.getAccessToken())
	                .setTokenType("Bearer")
	                .setExpiresIn(String.valueOf(token.getExpiresIn()))
	                .setRefreshToken(token.getRefreshToken())
	                .buildJSONMessage();
		} catch (OAuthProblemException ex) {
			resp = OAuthASResponse.errorResponse(HttpServletResponse.SC_FOUND).error(ex)
					.location(redirect_uri)
					.buildJSONMessage();

	
		} catch (Exception e) {
			logger.warn(e.toString(), e);
			throw new WebApplicationException(e);
		}
		
		return Response.status(resp.getResponseStatus()).entity(resp.getBody()).build();
	}

	@POST
	@Path("/refresh_token")
	public Response getRefresh(@FormParam("grant_type") String grant_type,
			@FormParam("refresh_token") String refresh_token,
			@FormParam("client_id") String client_id,
			@FormParam("client_secret") String client_secret,
			@Context HttpServletRequest request, @Context HttpServletResponse response) throws OAuthSystemException, URISyntaxException {
		OAuthResponse resp = null;
		
		try {
			
			if(StringUtils.isBlank(refresh_token))
				return Response.status(Response.Status.BAD_REQUEST).build();
			
			if(!refresh_token.matches(Constants.UUID_PATTERN))
				return Response.status(Response.Status.BAD_REQUEST).build();
			
			RefreshToken issuedRefreshToken = oAuthRepository.getRefreshToken(refresh_token);
			
			if(issuedRefreshToken == null || !issuedRefreshToken.getClientId().equals(client_id)) {
				throw OAuthProblemException.error("Refresh token not found");
			} else if(!clientCredentialsRepository.clientAllowed(client_id, client_secret)) {
				throw OAuthProblemException.error("Invalid credentials");
			} else if(!"refresh_token".equals(grant_type)) {
				throw OAuthProblemException.error("Invalid grant type");
			}
			
			OAuthTokenImpl token = new OAuthTokenImpl(oauthIssuerImpl.accessToken(), client_id, issuedRefreshToken.getScope(), oauthIssuerImpl.refreshToken(), System.currentTimeMillis());
			token.setProperty("username", issuedRefreshToken.getProperty("username"));
			oAuthRepository.putToken(token);
			
			resp = OAuthASResponse
	                .tokenResponse(HttpServletResponse.SC_OK)
	                .setAccessToken(oauthIssuerImpl.accessToken())
	                .setTokenType("Bearer")
	                .setExpiresIn("3600")
	                .buildJSONMessage();
		} catch (OAuthProblemException ex) {
			resp = OAuthASResponse.errorResponse(HttpServletResponse.SC_FOUND).error(ex)
					.buildJSONMessage();
		} catch (Exception e) {
			logger.warn(e.toString(), e);
			throw new WebApplicationException(e);
		}
		
		return Response.status(resp.getResponseStatus()).entity(resp.getBody()).build();
	}
	
	static ObjectMapper mapper = new ObjectMapper();
		
	@POST
	@Path("/introspect")
	public Response getIntrospect(@FormParam("token") String tokenId,
			@Context HttpServletRequest request, @Context HttpServletResponse response) throws Exception {
		
		if(tokenId == null)
			return Response.status(Status.NOT_FOUND).build();
		
		if(StringUtils.isBlank(tokenId))
			return Response.status(Status.BAD_REQUEST).build();
		
		if(!tokenId.matches(Constants.UUID_PATTERN))
			return Response.status(Response.Status.BAD_REQUEST).build();
		
		OAuthTokenImpl accessToken = oAuthRepository.getValidToken(tokenId);
		
		if(accessToken == null)
			return Response.status(Status.NOT_FOUND).build();
		
		String user_id = accessToken.getProperty("username");
		String userData = null;
		if(StringUtils.isNotBlank(user_id)) {
			User user = cassandraDataStore.getUser(user_id);
			if(user != null && user.getJson_object_uuid() != null) {
				ProxyObject jsonUser = cassandraDataStore.getProxyObject(user.getJson_object_uuid(), "mobile_app", "user");
				userData = jsonUser.getData();
			}
		}
		
		JSONObject jsonResponse = new JSONObject();
		//onResponse.put("active", true);
		jsonResponse.put("scope", accessToken.getScope());
		jsonResponse.put("active", true);
		jsonResponse.put("client_id", accessToken.getClientId());
		jsonResponse.put("user_id", user_id);
		jsonResponse.put("token_type", "Bearer");
		jsonResponse.put("exp", accessToken.getExpiresTimeStamp());
		jsonResponse.put("iss", request.getScheme() + "://" + request.getRemoteHost());
		jsonResponse.put("sub", user_id);
		jsonResponse.put("user", userData == null ? JSONObject.NULL : new JSONObject(userData));
		
		return Response.ok().entity(jsonResponse.toString()).build();
	}


}
