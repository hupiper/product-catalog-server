package com.redhat.demo;

import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.h2.H2DatabaseTestResource;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@QuarkusTestResource(H2DatabaseTestResource.class)
public class AuthEndpointTest {

    @Test
    public void testCreateUser() {
        given()
            .header("Content-Type", "application/x-www-form-urlencoded")
            .formParam("email", "bob.smith@noreply.com")
            .formParam("password", "password")
            .formParam("password_confirmation", "password")
            .request()
            .post("/api/auth/register")
                .then()
                    .statusCode(200);
    }

}
