package com.redhat.demo;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.h2.H2DatabaseTestResource;
import io.quarkus.test.junit.QuarkusTest;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import static io.restassured.RestAssured.given;

import static org.hamcrest.CoreMatchers.is;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;

@QuarkusTest
@QuarkusTestResource(H2DatabaseTestResource.class)
@TestMethodOrder(OrderAnnotation.class)
public class ProductEndpointTest {

    static final int DEFAULT_PRODUCT_SIZE = 12;

    @Test
    @Order(1)
    public void testProductEndpoint() {
        given()
          .when().get("/api/product")
          .then()
             .statusCode(200)
             .body("size()", is(DEFAULT_PRODUCT_SIZE));
    }

    @Test
    @Order(2)
    public void testCountEndpoint() {
        given()
          .when().get("/api/product/count")
          .then()
             .statusCode(200)
             .body(equalTo(String.valueOf(DEFAULT_PRODUCT_SIZE)));
    }

    @Test
    @Order(3)
    public void testExistingProductEndpoint() {
        given()
          .pathParam("id", 1)
          .when().get("/api/product/{id}")
          .then()
            .statusCode(200)
            .body("id",equalTo(1));
    }

    @Test
    @Order(4)
    public void testMissingProductEndpoint() {
        given()
          .pathParam("id", 99)
          .when().get("/api/product/{id}")
          .then()
            .statusCode(404);
    }

    @Test
    @Order(4)
    public void testCreateProductEndpoint() {
      // Test bad request, missing category id
      given()
        .auth().basic("test@demo.com", "Welcome1")
        .header("Content-Type", "application/x-www-form-urlencoded")
        .formParam("name", "Test product")
        .formParam("description", "Test product")
        .formParam("price", 100.00)
        .request()
        .post("/api/product")
        .then()
          .statusCode(400);

      // Test bad request, n category found
      given()
        .auth().basic("test@demo.com", "Welcome1")
        .header("Content-Type", "application/x-www-form-urlencoded")
        .formParam("name", "Test product")
        .formParam("description", "Test product")
        .formParam("category_id", 99)
        .formParam("price", 100.00)
        .request()
        .post("/api/product")
        .then()
          .statusCode(400);


      // Valid create request
      Integer id = given()
        .auth().basic("test@demo.com", "Welcome1")
        .header("Content-Type", "application/x-www-form-urlencoded")
        .formParam("name", "Test product")
        .formParam("description", "Test product")
        .formParam("category_id", 1)
        .formParam("price", 100.00)
        .request()
        .post("/api/product")
        .then()
          .statusCode(200)
        .extract().path("product.id");

      given()
        .auth().basic("test@demo.com", "Welcome1")
        .contentType("application/json")
        .pathParam("id", id.toString())
        .delete("/api/product/{id}")
        .then()
          .statusCode(204);
    }

    @Test
    @Order(5)
    public void testUpdateProductEndpoint() {
      given()
        .auth().basic("test@demo.com", "Welcome1")
        .header("Content-Type", "application/x-www-form-urlencoded")
        .pathParam("id", 1)
        .formParam("id", 1)
        .formParam("name", "Test product")
        .formParam("description", "Test product")
        .formParam("description", "Test product")
        .formParam("category_id", 1)
        .formParam("price", 100.00)
        .request()
        .put("/api/product/{id}")
        .then()
          .statusCode(200);

      // Missing name
      given()
          .auth().basic("test@demo.com", "Welcome1")
          .header("Content-Type", "application/x-www-form-urlencoded")
          .pathParam("id", 1)
          .formParam("id", 1)
          .formParam("description", "Test product")
          .formParam("description", "Test product")
          .formParam("category_id", 1)
          .formParam("price", 100.00)
          .request()
          .put("/api/product/{id}")
          .then()
            .statusCode(400);

        // Missing category
        given()
            .auth().basic("test@demo.com", "Welcome1")
            .header("Content-Type", "application/x-www-form-urlencoded")
            .pathParam("id", 1)
            .formParam("id", 1)
            .formParam("description", "Test product")
            .formParam("description", "Test product")
            .formParam("price", 100.00)
            .request()
            .put("/api/product/{id}")
            .then()
              .statusCode(400);
    }

    @Test
    @Order(5)
    public void testMassDeleteEndpoint() {
      final List<Integer> idList = Arrays.asList(new Integer[]{1, 2});

      given()
        .auth().basic("test@demo.com", "Welcome1")
        .header("Content-Type", "application/x-www-form-urlencoded")
        .formParam("del_ids", idList)
        .request()
        .post("/api/product/delete")
        .then()
          .statusCode(200);
    }

}