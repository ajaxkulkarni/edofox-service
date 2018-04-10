package com.rns.web.edo.service;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;

public class EdoCORSFilter implements ContainerResponseFilter {
	 
    public ContainerResponse filter(ContainerRequest req, ContainerResponse edoContainerResponse) {
 
        ResponseBuilder edoResponseBuilder = Response.fromResponse(edoContainerResponse.getResponse());
        
        // *(allow from all servers) OR https://crunchify.com/ OR http://example.com/
        edoResponseBuilder.header("Access-Control-Allow-Origin", "*")
        
        // As a part of the response to a request, which HTTP methods can be used during the actual request.
        .header("Access-Control-Allow-Methods", "API, CRUNCHIFYGET, GET, POST, PUT, UPDATE, OPTIONS")
        
        // How long the results of a request can be cached in a result cache.
        .header("Access-Control-Max-Age", "151200")
        
        // As part of the response to a request, which HTTP headers can be used during the actual request.
        .header("Access-Control-Allow-Headers", "x-requested-with,Content-Type");
 
        String crunchifyRequestHeader = req.getHeaderValue("Access-Control-Request-Headers");
 
        if (null != crunchifyRequestHeader && !crunchifyRequestHeader.equals(null)) {
            edoResponseBuilder.header("Access-Control-Allow-Headers", crunchifyRequestHeader);
        }
 
        edoContainerResponse.setResponse(edoResponseBuilder.build());
        return edoContainerResponse;
    }
}
