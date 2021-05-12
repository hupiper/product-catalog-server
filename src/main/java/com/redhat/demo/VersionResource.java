package com.redhat.demo;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Timed;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/api/version")
@ApplicationScoped
@Tag(name = "Products", description = "An API to return the version of the API")
public class VersionResource {

    @GET
    @Operation(summary = "API Version", description = "Get API version")
    @Counted(name = "countGetVersion", description = "How many get version calls have been performed.", tags = {"type=counter","api=version", "method=getVersion"} )
    @Timed(name = "perfGetVersion", description = "A measure of how long it takes to get version.", unit = MetricUnits.MILLISECONDS, tags = {"type=perf","api=version", "method=getVersion"})
    public String get() {
        return "1.0.2";
    }

}
