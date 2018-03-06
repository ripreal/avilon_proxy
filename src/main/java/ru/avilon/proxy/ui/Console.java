package ru.avilon.proxy.ui;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.mvc.Viewable;

import ru.avilon.proxy.rest.configuration.annotations.CheckBasicAuthentication;

@Path("/console")
@RolesAllowed("admin")
@CheckBasicAuthentication
public class Console {
	
	@Context private HttpServletRequest httpRequest;

	@GET
	public Response viewIndex() {
		httpRequest.setAttribute("console_navbar_json_active", "active");
		return Response.ok().entity(new Viewable("/console/objects")).build();
	}
	
	@GET
	@Path("/scripts")
	public Response viewScripts() {
		httpRequest.setAttribute("console_navbar_scripts_active", "active");
		return Response.ok().entity(new Viewable("/console/scripts")).build();
	}
	
	@GET
	@Path("/filterscripts")
	public Response viewFilterScripts() {
		httpRequest.setAttribute("console_navbar_filterscripts_active", "active");
		return Response.ok().entity(new Viewable("/console/filterscripts")).build();
	}
	
	@GET
	@Path("/users")
	public Response viewUsers() {
		httpRequest.setAttribute("console_navbar_users_active", "active");
		return Response.ok().entity(new Viewable("/console/users")).build();
	}
	
	@GET
	@Path("/logs")
	public Response viewLogs() {
		httpRequest.setAttribute("console_navbar_logs_active", "active");
		return Response.ok().entity(new Viewable("/console/logs")).build();
	}

}
