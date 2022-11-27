package io.gerhardt.keycloaklivebackup.metrics;

import io.gerhardt.keycloaklivebackup.utilities.PrometheusExporter;
import org.keycloak.services.resource.RealmResourceProvider;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.Status;

public class MetricsEndpoint implements RealmResourceProvider {

    public final static String ID = "backup-metrics";

    private static final boolean DISABLE_EXTERNAL_ACCESS = Boolean.parseBoolean(System.getenv("DISABLE_EXTERNAL_ACCESS"));

    @Override
    public Object getResource() {
        return this;
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response get(@Context HttpHeaders headers) {
        if (DISABLE_EXTERNAL_ACCESS) {
            if (!headers.getRequestHeader("x-forwarded-host").isEmpty()) {
                return Response.status(Status.FORBIDDEN).build();
            }
        }

        try {
            final StreamingOutput stream = output -> PrometheusExporter.instance().export(output);
            return Response.ok(stream).build();
        } catch (Exception exception) {
            return Response.serverError().build();
        }
    }

    @Override
    public void close() {
    }
}
