package io.gerhardt.keycloaklivebackup.csv;

import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.services.resource.RealmResourceProvider;
import org.keycloak.services.resource.RealmResourceProviderFactory;

public class GenerateCsvEndpointFactory implements RealmResourceProviderFactory {
    @Override
    public RealmResourceProvider create(KeycloakSession session) {
        return new GenerateCsvEndpoint(session);
    }

    @Override
    public void init(Config.Scope config) {
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        // nothing to do
    }

    @Override
    public void close() {
        // nothing to close
    }

    @Override
    public String getId() {
        return GenerateCsvEndpoint.ID;
    }
}
