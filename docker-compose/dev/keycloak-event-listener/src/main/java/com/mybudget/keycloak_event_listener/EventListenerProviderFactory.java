package com.mybudget.keycloak_event_listener;

import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

public class EventListenerProviderFactory implements org.keycloak.events.EventListenerProviderFactory {

    private String baseUrl;

    @Override
    public org.keycloak.events.EventListenerProvider create(KeycloakSession session) {
        return new EventListenerProvider(baseUrl);
    }

    @Override
    public void init(Config.Scope config) {
        this.baseUrl = config.get("baseUrl", "http://host.docker.internal:8080/api/user");
    }


    @Override
    public void postInit(KeycloakSessionFactory factory) {}

    @Override
    public void close() {}

    @Override
    public String getId() {
        return "my-custom-event-listener";
    }
}
