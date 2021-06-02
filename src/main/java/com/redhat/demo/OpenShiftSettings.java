package com.redhat.demo;

import java.util.Map;
import static java.util.Map.entry;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import io.fabric8.kubernetes.api.model.LabelSelector;
import io.fabric8.kubernetes.api.model.LabelSelectorBuilder;
import io.fabric8.openshift.api.model.Route;
import io.fabric8.openshift.client.OpenShiftClient;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;

import org.eclipse.microprofile.config.ConfigProvider;
import org.jboss.logging.Logger;

@ApplicationScoped
public class OpenShiftSettings {

    private static final Logger LOGGER = Logger.getLogger("ListenerBean");

    @Inject
    OpenShiftClient openshiftClient;

    void onStart(@Observes StartupEvent ev) {
        // Test if we are running in a pod
        String k8sSvcHost = System.getenv("KUBERNETES_SERVICE_HOST");
        if (k8sSvcHost == null || "".equals(k8sSvcHost)) {
            LOGGER.infof("Not running in kubernetes, using CORS_ORIGIN environment '%s' variable",
                    ConfigProvider.getConfig().getValue("quarkus.http.cors.origins", String.class));
            return;
        }

        if (System.getenv("CORS_ORIGIN") != null) {
            LOGGER.infof("CORS_ORIGIN explicitly defined bypassing route lookup");
            return;
        }

        // Look for route with label endpoint:client
        if (openshiftClient.getMasterUrl() == null) {
            LOGGER.info("Kubernetes context is not available");
        } else {
            LOGGER.infof("Application is running in OpenShift %s, checking for labelled route",
                    openshiftClient.getMasterUrl());

            LabelSelector selector = new LabelSelectorBuilder()
                    .withMatchLabels(Map.ofEntries(entry("endpoint", "client"))).build();
            List<Route> routes = null;
            try {
                routes = openshiftClient.routes().withLabelSelector(selector).list().getItems();
            } catch (Exception e) {
                LOGGER.info("Unexpected error occurred retrieving routes, using environment variable CORS_ORIGIN", e);
                return;
            }
            if (routes == null || routes.size() == 0) {
                LOGGER.info("No routes found with label 'endpoint:client', using environment variable CORS_ORIGIN");
                return;
            } else if (routes.size() > 1) {
                LOGGER.warn("More then one route found with 'endpoint:client', using first one");
            }

            Route route = routes.get(0);
            String host = route.getSpec().getHost();
            boolean tls = false;
            if (route.getSpec().getTls() != null && "".equals(route.getSpec().getTls().getTermination())) {
                tls = true;
            }
            String corsOrigin = (tls ? "https" : "http") + "://" + host;
            System.setProperty("quarkus.http.cors.origins", corsOrigin);
        }
        LOGGER.infof("Using host %s for cors origin",
                ConfigProvider.getConfig().getValue("quarkus.http.cors.origins", String.class));
    }

    void onStop(@Observes ShutdownEvent ev) {
        LOGGER.info("The application is stopping...");
    }
}
