package com.redhat.demo;

import java.io.IOException;
import java.security.Principal;
import java.time.LocalDateTime;

import javax.enterprise.context.ApplicationScoped;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;

import com.redhat.demo.model.User;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;

import io.quarkus.security.Authenticated;

@Path("/api/auth")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Authentication", description = "An API to manage user authentication and authorization")
public class AuthResource {

    private static final Logger log = Logger.getLogger("AuthResource");

    @GET
    @Path("/user")
    @Operation(summary = "Get user", description = "Get the current user")
    public User getUser(@Context SecurityContext sc) {
        if (sc.getUserPrincipal() !=null) {
            return User.find("email", sc.getUserPrincipal().toString()).firstResult();
        } else {
            return new User();
        }
    }

    @POST
    @Authenticated
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Login", description = "Authenticate a user")
    public LoginResult authenticate(@Context SecurityContext sc) {
        log.info("Handling login");
        Principal userPrincipal = sc.getUserPrincipal();
        if (userPrincipal !=null) {
            User user = User.find("email", sc.getUserPrincipal().toString()).firstResult();
            if (user != null) {
                log.info("Login succeeded for user " + user.email);
                return new LoginResult("Login Success", user);
            } else {
                return new LoginResult("Invalid user/password", null);
            }
        } else {
            log.info("No user principal set, login failed");
            return new LoginResult("Authentication failed", null);
        }
    }


    @POST
    @Path("/register")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_PLAIN)
    @Transactional
    @Operation(summary = "Register", description = "Register a new user")
    public Response register(@FormParam("email") String email,
                         @FormParam("password") String password,
                         @FormParam("password_confirmation") String passwordConfirmation) {

        //return Response.status(Response.Status.OK).entity("User registration is not supported at this time").build();

        // Can't compile wildfly code needed to generate passwords with quarkus native compiler
        if (password == null || password.isEmpty()) {
            throw new WebApplicationException("Password must be provided");
        }
        if (!password.equals(passwordConfirmation)) {
            throw new WebApplicationException("Password and password confirmation must match");
        }

        try {
            User user = new User();
            user.email = email;
            user.setPasswordHash(password);

            user.createdAt = LocalDateTime.now();
            user.persist();
            return Response.status(Response.Status.OK).entity("true").build();
        } catch (Exception e) {
            log.error(String.format("Failed to register user %s", email), e);
            throw new WebApplicationException("User could not be saved", 500);
        }
    }

    public class LoginResult {
        public String message;
        public SecureUser user;

        public LoginResult() {}

        public LoginResult(String message, User user) {
            this.message = message;
            if (user != null)
                this.user = new SecureUser(user);
        }

    }

    public class SecureUser {
        public String email;
        public LocalDateTime createdAt;

        public SecureUser (User user) {
            this.email = user.email;
            this.createdAt = user.createdAt;
        }

    }
}