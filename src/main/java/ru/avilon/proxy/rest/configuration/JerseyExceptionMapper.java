package ru.avilon.proxy.rest.configuration;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.avilon.proxy.utils.ExceptionUtils;
import ru.avilon.proxy.utils.WebUtils;

@Provider
public class JerseyExceptionMapper implements ExceptionMapper<java.lang.Exception> {
    final static Logger logger = LoggerFactory.getLogger(JerseyExceptionMapper.class);
    
    @Context HttpServletRequest request;
    
	public Response toResponse(java.lang.Exception ex) {
    	
		if(ex instanceof WebApplicationException) {
			logger.warn("error for request: {} error: {}", WebUtils.debugRequest(request), ExceptionUtils.toStringWithCause(ex));
			WebApplicationException wes = ((WebApplicationException)ex);
			return Response.status(wes.getResponse().getStatus()).entity(wes.getMessage()).type("text/plain").build();
		} else {
			logger.error("error occurred for request: {}", WebUtils.debugRequest(request), ex);
			return Response.status(500)
					.entity(ex.getMessage() != null ? ex.getMessage() : ex.toString())
					.type("text/plain")
					.build();
		}
    }
}