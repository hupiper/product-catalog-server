package com.redhat.demo;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.microprofile.config.ConfigProvider;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import io.fabric8.kubernetes.client.server.mock.KubernetesServer;
import io.fabric8.openshift.api.model.Route;
import io.fabric8.openshift.api.model.RouteBuilder;
import io.fabric8.openshift.api.model.RouteList;
import io.fabric8.openshift.api.model.RouteListBuilder;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.kubernetes.client.KubernetesServerTestResource;
import io.quarkus.test.kubernetes.client.KubernetesTestServer;
import io.quarkus.test.kubernetes.client.WithKubernetesTestServer;
import io.restassured.RestAssured;

import org.jboss.logging.Logger;

import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
@WithKubernetesTestServer
@QuarkusTestResource(KubernetesServerTestResource.class)
public class OpenShiftSettingsTest {

    private static final Logger LOGGER = Logger.getLogger(OpenShiftSettingsTest.class);

    @KubernetesTestServer
    KubernetesServer mockServer;

    static final String CLIENT_HOST = "client-test.apps.home.ocplab.com";
    static final Map<String, String> CLIENT_LABELS = new HashMap<String, String>()
        {{
            put("endpoint", "client");
        }};

    // @BeforeEach
    public void before() {
        final Route testRoute = new RouteBuilder().withNewMetadata().withName("server").withNamespace("test").withLabels(CLIENT_LABELS).and().withNewSpec().withHost(CLIENT_HOST).and().build();

        RouteList list = new RouteListBuilder().withItems(testRoute).build();
        LOGGER.infof("testing with %d routes",list.getItems().size());

        mockServer.expect().get().withPath("/apis/route.openshift.io/v1/namespaces/test/routes")
                .andReturn(200, list).always();

        LOGGER.infof("MockWebServer listenening on %s:%d",mockServer.getMockServer().getHostName(),mockServer.getMockServer().getPort());
    }

    // @Test
    public void testInteractionWithAPIServer() {
        RestAssured.when().get("http://localhost:" + mockServer.getMockServer().getPort() + "/apis/route.openshift.io/v1/namespaces/test/routes").then()
                .body("size()", is(1));
    }

    // @Test
    public void testOpenShiftSettings() {
        Assertions.assertEquals(ConfigProvider.getConfig().getValue("quarkus.http.cors.origins", String.class), "http://localhost:8080,http://localhost:9000");
    }

}
