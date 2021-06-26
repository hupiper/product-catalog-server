package com.redhat.demo;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.h2.H2DatabaseTestResource;
import io.quarkus.test.junit.QuarkusTest;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import static io.restassured.RestAssured.given;

import static org.hamcrest.CoreMatchers.is;

import javax.json.bind.JsonbBuilder;

import com.redhat.demo.model.Category;

import static org.hamcrest.CoreMatchers.equalTo;

@QuarkusTest
@QuarkusTestResource(H2DatabaseTestResource.class)
@TestMethodOrder(OrderAnnotation.class)
public class CategoryEndpointTest {

    @Test
    @Order(1)
    public void testCategoryEndpoint() {
        given()
          .when().get("/api/category")
          .then()
             .statusCode(200)
             .body("size()", is(3));
    }

    @Test
    @Order(2)
    public void testExistingCategoryEndpoint() {
        given()
          .pathParam("id", 1)
          .when().get("/api/category/{id}")
          .then()
            .statusCode(200)
            .body("id",equalTo(1));
    }

    @Test
    @Order(3)
    public void testMissingCategoryEndpoint() {
        given()
          .pathParam("id", 99)
          .when().get("/api/category/{id}")
          .then()
            .statusCode(404);
    }

    @Test
    @Order(4)
    public void testCreateCategory() {
      Category category = new Category();
      category.name = "Test category";
      category.description = "Test category";

      Integer id = given()
        .auth().basic("test@demo.com", "Welcome1")
        .contentType("application/json")
        .body(JsonbBuilder.create().toJson(category))
        .request()
        .post("/api/category")
        .then()
          .statusCode(201)
        .extract().path("id");

      given()
        .auth().basic("test@demo.com", "Welcome1")
        .contentType("application/json")
        .pathParam("id", id.toString())
        .delete("/api/category/{id}")
        .then()
          .statusCode(204);

    }

    @Test
    @Order(5)
    public void testUpdateCategory() {
      final String name = "test";

      Category category = Category.findById(1);
      category.name = name;

      given()
        .auth().basic("test@demo.com", "Welcome1")
        .contentType("application/json")
        .body(JsonbBuilder.create().toJson(category))
        .pathParam("id", category.id)
        .put("/api/category/{id}")
        .then()
          .statusCode(HttpStatus.SC_OK)
          .body("name", equalTo(name));
    }

}