package com.mybudget.keycloak_event_listener;

import org.jboss.logging.Logger;
import org.keycloak.events.Event;
import org.keycloak.events.EventType;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class EventListenerProvider implements org.keycloak.events.EventListenerProvider {

    private static final Logger logger = Logger.getLogger(EventListenerProvider.class);

    private final String baseUrl;

    public EventListenerProvider(String baseUrl) {
        this.baseUrl = (baseUrl != null ? baseUrl : "http://host.docker.internal:8080/user");
    }

    @Override
    public void onEvent(Event event) {
        logger.infof("Received user event: %s for user: %s", event.getType(), event.getUserId());

        if (event.getDetails() != null) {
            logger.infof("Event details: %s", event.getDetails().toString());
        }

        try {
            if (EventType.REGISTER.equals(event.getType())) {
                String userId = event.getUserId();
                String email = event.getDetails().get("email");
                String givenName = event.getDetails().get("given_name");
                String familyName = event.getDetails().get("family_name");
                String preferredUsername= event.getDetails().get("username");

                logger.infof("New user registered in Keycloak: %s, Email: %s", userId, email);

                UserInitRequest userInitRequest = new UserInitRequest(
                        userId, email, givenName, familyName, preferredUsername
                );
                sendUserInitRequest(userInitRequest);
            }

            if (EventType.DELETE_ACCOUNT.equals(event.getType())) {
                String userId   = event.getUserId();
                String username = event.getDetails().get("username");

                if (username != null) {
                    logger.infof("User deleted in Keycloak: %s, username: %s", userId, username);
                    sendDeleteRequest(username);
                } else {
                    logger.error("Username is null for DELETE_ACCOUNT event.");
                }
            }
        } catch (Exception e) {
            logger.errorf("Exception in event handling: %s", e.getMessage(), e);
        }
    }

    private void sendDeleteRequest(String username) {
        String jsonPayload = "{\"username\":\"" + username + "\"}";
        logger.infof("Preparing to send DELETE request for user: %s", jsonPayload);

        try {
            sendHttpRequest("DELETE", "/delete", jsonPayload);
        } catch (Exception e) {
            logger.errorf("Exception while sending DELETE request for user '%s'", username, e);
        }
    }

    private void sendUserInitRequest(UserInitRequest userInitRequest) {
        String jsonPayload = String.format(
                "{\"keycloakSub\":\"%s\",\"email\":\"%s\",\"givenName\":\"%s\"," +
                        "\"familyName\":\"%s\",\"preferredUsername\":\"%s\"}",
                userInitRequest.getKeycloakSub(),
                userInitRequest.getEmail(),
                userInitRequest.getGivenName(),
                userInitRequest.getFamilyName(),
                userInitRequest.getPreferredUsername()
        );
        logger.infof("Preparing to send POST /initialize request: %s", jsonPayload);

        try {
            sendHttpRequest("POST", "/initialize", jsonPayload);
        } catch (Exception e) {
            logger.error("Error while notifying user-service", e);
        }
    }

    private void sendHttpRequest(String method, String endpoint, String jsonPayload) throws IOException {
        String fullUrl = baseUrl + endpoint;
        URL url = new URL(fullUrl);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        conn.setRequestProperty("Content-Type", "application/json");

        if (jsonPayload != null && !jsonPayload.isEmpty()) {
            conn.setDoOutput(true);
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
        }

        int responseCode = conn.getResponseCode();
        logger.infof("%s %s -> response code: %d", method, fullUrl, responseCode);

        if (responseCode >= 200 && responseCode < 300) {
            try (Scanner scanner = new Scanner(conn.getInputStream(), StandardCharsets.UTF_8)) {
                if (scanner.hasNext()) {
                    String responseBody = scanner.useDelimiter("\\A").next();
                    logger.infof("Success response: %s", responseBody);
                }
            } catch (IOException e) {
                logger.info("No response body or error reading success body");
            }
        } else {
            try (Scanner scanner = new Scanner(conn.getErrorStream(), StandardCharsets.UTF_8)) {
                String errorBody = scanner.hasNext() ? scanner.useDelimiter("\\A").next() : "";
                logger.errorf("Error response from %s: code=%d, body=%s", fullUrl, responseCode, errorBody);
            }
        }

        conn.disconnect();
    }

    @Override
    public void onEvent(org.keycloak.events.admin.AdminEvent event, boolean includeRepresentation) {
    }

    @Override
    public void close() {
    }

    private static class UserInitRequest {
        private final String keycloakSub;
        private final String email;
        private final String givenName;
        private final String familyName;
        private final String preferredUsername;

        public UserInitRequest(String keycloakSub, String email, String givenName,
                               String familyName, String preferredUsername) {
            this.keycloakSub = keycloakSub;
            this.email = email;
            this.givenName = givenName;
            this.familyName = familyName;
            this.preferredUsername = preferredUsername;
        }

        public String getKeycloakSub() { return keycloakSub; }
        public String getEmail() { return email; }
        public String getGivenName() { return givenName; }
        public String getFamilyName() { return familyName; }
        public String getPreferredUsername() { return preferredUsername; }
    }
}
