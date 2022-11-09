package io.gerhardt.keycloaklivebackup.models;

import java.util.List;
import java.util.Map;

public class User {
    private final List<String> realmRoles;
    private final Map<String, List<String>> clientRoles;
    private final String id;
    private final String lastName;
    private final String firstName;
    private final Boolean emailVerified;
    private final List<Credential> credentials;
    private final Long createdTimestamp;
    private final String email;
    private final Boolean enabled;
    private final String username;
    private final String realm;

    public User(List<String> realmRoles, Map<String, List<String>> clientRoles, String id, String lastName, String firstName, Boolean emailVerified, List<Credential> credentials, Long createdTimestamp, String email, Boolean enabled, String username, String realm) {
        this.realmRoles = realmRoles;
        this.clientRoles = clientRoles;
        this.id = id;
        this.lastName = lastName;
        this.firstName = firstName;
        this.emailVerified = emailVerified;
        this.credentials = credentials;
        this.createdTimestamp = createdTimestamp;
        this.email = email;
        this.enabled = enabled;
        this.username = username;
        this.realm = realm;
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public String toString() {
        StringBuilder user = new StringBuilder("{\n");
        user.append("\t\"realm\": \"").append(realm).append("\",");
        user.append("\n\t\"users\": [\n\t\t{");
        user.append("\n\t\t\t\"realmRoles\": [");
        for (int index = 0; index < realmRoles.size(); index++) {
            user.append("\n\t\t\t\t\"").append(realmRoles.get(index)).append("\"");
            if (index + 1 < realmRoles.size()) {
                user.append(",");
            }
        }
        if (realmRoles.size() > 0) {
            user.append("\n\t\t\t");
        }
        user.append("],");
        user.append("\n\t\t\t\"clientRoles\": {");

        boolean multipleRows = false;
        boolean clientRoleDetected = false;
        for (Map.Entry<String, List<String>> entry : clientRoles.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                if (clientRoleDetected) {
                    user.append(",");
                }
                user.append("\n\t\t\t\t\"").append(entry.getKey()).append("\": [");
                for (int index = 0; index < entry.getValue().size(); index++) {
                    user.append("\"").append(entry.getValue().get(index)).append("\"");
                    if (index + 1 < entry.getValue().size()) {
                        user.append(",");
                    }
                }
                user.append("]");
                multipleRows = true;
                clientRoleDetected = true;
            }
        }
        if (multipleRows) {
            user.append("\n\t\t\t");
        }
        user.append("},");
        user.append("\n\t\t\t\"lastName\": \"").append(lastName).append("\",");
        user.append("\n\t\t\t\"firstName\": \"").append(firstName).append("\",");
        user.append("\n\t\t\t\"emailVerified\": ").append(emailVerified).append(",");
        user.append("\n\t\t\t\"credentials\": [");
        for (int index = 0; index < credentials.size(); index++) {
            user.append("\n\t\t\t\t{");
            user.append("\n\t\t\t\t\t\"credentialData\": \"{\\\"hashIterations\\\":").append(credentials.get(index).getHashIterations()).append(",\\\"additionalParameters\\\":{},\\\"algorithm\\\":\\\"").append(credentials.get(index).getAlgorithm()).append("\\\"}\",");
            user.append("\n\t\t\t\t\t\"type\": \"").append(credentials.get(index).getType()).append("\",");
            user.append("\n\t\t\t\t\t\"secretData\": \"{\\\"salt\\\":\\\"").append(credentials.get(index).getSalt()).append("\\\",\\\"additionalParameters\\\":{},\\\"value\\\":\\\"").append(credentials.get(index).getValue()).append("\\\"}\"");
            user.append("\n\t\t\t\t}");
            if (index + 1 < realmRoles.size()) {
                user.append(",");
            }
        }
        if (credentials.size() > 0) {
            user.append("\n\t\t\t");
        }

        user.append("],");
        user.append("\n\t\t\t\"createdTimestamp\": ").append(createdTimestamp).append(",");
        user.append("\n\t\t\t\"email\": \"").append(email).append("\",");
        user.append("\n\t\t\t\"enabled\": ").append(enabled).append(",");
        user.append("\n\t\t\t\"username\": \"").append(username).append("\"");
        user.append("\n\t\t}");
        user.append("\n\t]");
        user.append("\n}");
        return user.toString();
    }
}
