package com.mybudget.keycloak_event_listener;

import org.jboss.logging.Logger;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventType;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class MyCustomEventListenerProvider implements EventListenerProvider {

    private static final Logger logger = Logger.getLogger(MyCustomEventListenerProvider.class);

    private final String baseUrl;

    public MyCustomEventListenerProvider(String baseUrl) {
        this.baseUrl = "http://host.docker.internal:8080/api/user";
    }

    @Override
    public void onEvent(Event event) {
        if (EventType.REGISTER.equals(event.getType())) {
            String userId = event.getUserId();
            String email = event.getDetails().get("email");
            String givenName = event.getDetails().get("given_name");
            String familyName = event.getDetails().get("family_name");
            String preferredUsername = event.getDetails().get("username");

            logger.infof("New user registered in Keycloak: %s, Email: %s", userId, email);

            UserInitRequest userInitRequest = new UserInitRequest(
                    userId,
                    email,
                    givenName,
                    familyName,
                    preferredUsername
            );

            sendUserInitRequest(userInitRequest);
        }
    }

    private void sendUserInitRequest(UserInitRequest userInitRequest) {
        try {
            URL url = new URL(baseUrl + "/initialize");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            String jsonPayload = "{" +
                    "\"keycloakSub\":\"" + userInitRequest.getKeycloakSub() + "\"," +
                    "\"email\":\"" + userInitRequest.getEmail() + "\"," +
                    "\"givenName\":\"" + userInitRequest.getGivenName() + "\"," +
                    "\"familyName\":\"" + userInitRequest.getFamilyName() + "\"," +
                    "\"preferredUsername\":\"" + userInitRequest.getPreferredUsername() + "\"" +
                    "}";

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            if (responseCode >= 200 && responseCode < 300) {
                logger.infof("User successfully initialized in the user-service, response code: %d", responseCode, jsonPayload);
            } else {
                logger.errorf("Error while notifying user-service. Response code: %d", responseCode);
            }

            conn.disconnect();
        } catch (Exception e) {
            logger.error("Error while notifying user-service", e);
        }
    }

    @Override
    public void onEvent(org.keycloak.events.admin.AdminEvent event, boolean includeRepresentation) {}

    @Override
    public void close() {}

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

        public String getKeycloakSub() {
            return keycloakSub;
        }
        public String getEmail() {
            return email;
        }
        public String getGivenName() {
            return givenName;
        }
        public String getFamilyName() {
            return familyName;
        }
        public String getPreferredUsername() {
            return preferredUsername;
        }
    }
}
