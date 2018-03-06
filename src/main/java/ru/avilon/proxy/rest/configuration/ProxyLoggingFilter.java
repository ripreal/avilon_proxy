package ru.avilon.proxy.rest.configuration;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.annotation.Priority;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;

import org.glassfish.jersey.message.internal.ReaderWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.avilon.proxy.rest.configuration.annotations.UseLogger;

@UseLogger
@Priority(Integer.MIN_VALUE)
public class ProxyLoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {

	final static Logger logger = LoggerFactory.getLogger(ProxyLoggingFilter.class);
	

	@Override
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
			throws IOException {
		logger.info("RESPONSE: Status=({} {}), Principal=({}) entity=({})", 
				responseContext.getStatus(), responseContext.getStatusInfo(),
				requestContext.getSecurityContext().getUserPrincipal(),
				responseContext.getEntity());
	}



	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
		logger.info("REQUEST: Method={}, URI={}, Headers=({}) entity=({})", 
				requestContext.getMethod(), 
				requestContext.getUriInfo().getRequestUri(),
				requestContext.getHeaders(),
				getEntityBody(requestContext));
		
	}
	
	
	private String getEntityBody(ContainerRequestContext requestContext) 
	    {
	        ByteArrayOutputStream out = new ByteArrayOutputStream();
	        InputStream in = requestContext.getEntityStream();
	         
	        final StringBuilder b = new StringBuilder();
	        try
	        {
	            ReaderWriter.writeTo(in, out);
	 
	            byte[] requestEntity = out.toByteArray();
	            if (requestEntity.length == 0)
	            {
	                b.append("");
	            }
	            else
	            {
	                b.append(new String(requestEntity)).append("\n");
	            }
	            requestContext.setEntityStream( new ByteArrayInputStream(requestEntity) );
	 
	        } catch (IOException ex) {
	            logger.error(ex.toString(), ex);
	        }
	        return b.toString();
	    }
	

}
