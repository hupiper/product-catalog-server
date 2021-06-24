package com.redhat.demo;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.equalTo;

import static io.restassured.RestAssured.given;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.h2.H2DatabaseTestResource;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@QuarkusTestResource(H2DatabaseTestResource.class)
public class VersionEndpointTest {

    @Test
    public void testVersionEndpoint() {
        given()
          .when().get("/api/version")
          .then()
             .statusCode(200)
             .body(equalTo(VersionResource.VERSION));
    }
}