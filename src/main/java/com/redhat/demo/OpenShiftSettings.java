package com.redhat.demo;

import java.util.Map;
import java.util.Optional;

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
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

@ApplicationScoped
public class OpenShiftSettings {

    private static final Logger log = Logger.getLogger(OpenShiftSettings.class);

    @Inject
    OpenShiftClient openshiftClient;

    @ConfigProperty(name = "kubernetes.service.host")
    Optional<String> kubernetesServiceHost;

    @ConfigProperty(name = "quarkus.http.cors.origins")
    Optional<String> corsOrigin;

    void onStart(@Observes StartupEvent ev) {
        // Test if we are running in a pod
        if (kubernetesServiceHost.isEmpty()) {
            if (corsOrigin.isPresent()) {
                log.infof("Not running in kubernetes, using CORS_ORIGIN environment '%s' variable", corsOrigin.get());
            } else {
                log.warnf("Not running in kubernetes, no CORS_ORIGIN environment is set");
            }
            return;
        }

        if (corsOrigin.isPresent()) {
            log.infof("CORS_ORIGIN explicitly defined bypassing route lookup");
            return;
        }

        overrideCorsSetting();

        log.infof("Using host %s for cors origin",
                ConfigProvider.getConfig().getValue("quarkus.http.cors.origins", String.class));
    }

    void onStop(@Observes ShutdownEvent ev) {
        log.info("The application is stopping...");
    }

    private void overrideCorsSetting() {
        // Look for route with label endpoint:client
        if (openshiftClient.getMasterUrl() == null) {
            log.info("Kubernetes context is not available");
        } else {
            log.infof("Application is running in OpenShift %s, checking for labelled route",
                    openshiftClient.getMasterUrl());

            LabelSelector selector = new LabelSelectorBuilder()
                    .withMatchLabels(Map.ofEntries(entry("endpoint", "client"))).build();
            List<Route> routes = null;
            try {
                routes = openshiftClient.routes().withLabelSelector(selector).list().getItems();
                //Debug mockserver
                if (routes == null || routes.isEmpty()) {
                    routes = openshiftClient.routes().list().getItems();
                }
            } catch (Exception e) {
                log.info("Unexpected error occurred retrieving routes, using environment variable CORS_ORIGIN", e);
                return;
            }
            if (routes == null || routes.isEmpty()) {
                log.info("No routes found with label 'endpoint:client', using environment variable CORS_ORIGIN");
                //Temp debugging
                System.setProperty("quarkus.http.cors.origins", "*");
                return;
            } else if (routes.size() > 1) {
                log.warn("More then one route found with 'endpoint:client', using first one");
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
    }
}
