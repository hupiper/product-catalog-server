package com.redhat.demo;

import java.io.IOException;

import javax.json.Json;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.jboss.logging.Logger;

import io.vertx.core.http.HttpServerRequest;

@Provider
public class GlobalFilter implements ContainerRequestFilter, ExceptionMapper<Exception> {

    private static final Logger LOGGER = Logger.getLogger(GlobalFilter.class);

    @Context
    UriInfo info;

    @Context
    HttpServerRequest request;

    @Override
    public Response toResponse(Exception exception) {

        LOGGER.error("Unexpected exception", exception);
        int code = 500;
        if (exception instanceof WebApplicationException) {
            code = ((WebApplicationException) exception).getResponse().getStatus();
        }
        return Response.status(code)
                .entity(Json.createObjectBuilder().add("error", exception.getMessage()).add("code", code).build())
                .build();
    }

    @Override
    public void filter(ContainerRequestContext context) throws IOException {
        final String method = context.getMethod();
        final String path = info.getPath();
        final String address = request.remoteAddress().toString();

        LOGGER.infof("Request %s %s from IP %s", method, path, address);
    }
}
