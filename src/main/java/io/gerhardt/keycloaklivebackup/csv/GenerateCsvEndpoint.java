package io.gerhardt.keycloaklivebackup.csv;

import io.gerhardt.keycloaklivebackup.utilities.CsvManager;
import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resource.RealmResourceProvider;

import javax.ws.rs.GET;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.Status;
import java.io.IOException;

public class GenerateCsvEndpoint implements RealmResourceProvider {

    private final KeycloakSession keycloakSession;

    // The ID of the provider is also used as the name of the endpoint
    public final static String ID = "generate-csv";

    private static final boolean DISABLE_EXTERNAL_ACCESS = true;

    public GenerateCsvEndpoint(KeycloakSession keycloakSession) {
        this.keycloakSession = keycloakSession;
    }

    @Override
    public Object getResource() {
        return this;
    }

    @GET
    public Response get(@Context HttpHeaders headers) throws IOException {
        if (DISABLE_EXTERNAL_ACCESS) {
            if (!headers.getRequestHeader("x-forwarded-host").isEmpty()) {
                // Request is being forwarded by HA Proxy on Openshift
                return Response.status(Status.FORBIDDEN).build(); //(stream).build();
            }
        }
        try {
            CsvManager csvMapGenerator = new CsvManager(null);
            csvMapGenerator.updateCsv(keycloakSession);
            return Response.ok().entity("Success!").build();
        } catch (Exception exception) {
            return Response.serverError().build();
        }
    }

    @Override
    public void close() {
    }
}
