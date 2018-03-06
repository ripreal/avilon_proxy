package ru.avilon.proxy.rest.configuration;

import javax.ws.rs.core.SecurityContext;

import ru.avilon.proxy.entities.User;
import ru.avilon.proxy.rest.Administration;
import ru.avilon.proxy.rest.Changes;

public class TestDependencyBinder extends DependencyBinder {

	@Override
	protected void configure() {
		
		super.configure();
		bind(Changes.class).to(Changes.class);
		bind(Administration.class).to(Administration.class);
		bind(new ProxySecurityContext(new User(), false, "OAUTH")).to(SecurityContext.class);
	}
}
