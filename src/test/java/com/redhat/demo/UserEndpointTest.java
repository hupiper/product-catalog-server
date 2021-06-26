package com.redhat.demo;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.h2.H2DatabaseTestResource;
import io.quarkus.test.junit.QuarkusTest;

import static io.restassured.RestAssured.given;

import static org.hamcrest.CoreMatchers.equalTo;

@QuarkusTest
@QuarkusTestResource(H2DatabaseTestResource.class)
@TestMethodOrder(OrderAnnotation.class)
public class UserEndpointTest {

    @Test
    @Order(1)
    public void testExistingUserEndpoint() {
        given()
          .auth().basic("test@demo.com", "Welcome1")
          .pathParam("id", 1)
          .when().get("/api/user/{id}")
          .then()
            .statusCode(200)
            .body("id",equalTo(1));
    }

    @Test
    @Order(2)
    public void testMissingUserEndpoint() {
        given()
          .auth().basic("test@demo.com", "Welcome1")
          .pathParam("id", 99)
          .when().get("/api/user/{id}")
          .then()
            .statusCode(404);
    }
}
