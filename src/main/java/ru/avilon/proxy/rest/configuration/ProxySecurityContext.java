package ru.avilon.proxy.rest.configuration;

import java.security.Principal;

import javax.ws.rs.core.SecurityContext;

import ru.avilon.proxy.entities.User;

public class ProxySecurityContext implements SecurityContext {

	User user;
	boolean isSecure;
	String authScheme;
	
	public ProxySecurityContext(User user, boolean isSecure, String authScheme) {
		this.user = user;
		this.isSecure = isSecure;
		this.authScheme = authScheme;
	}
	
	@Override
	public Principal getUserPrincipal() {
		return user;
	}

	@Override
	public boolean isUserInRole(String role) {
		return user.getRoles().contains(role);
	}

	@Override
	public boolean isSecure() {
		return isSecure;
	}

	@Override
	public String getAuthenticationScheme() {
		return authScheme;
	}

}
