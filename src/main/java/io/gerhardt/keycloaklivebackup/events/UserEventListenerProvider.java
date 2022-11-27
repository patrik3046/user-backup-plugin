package io.gerhardt.keycloaklivebackup.events;

import io.gerhardt.keycloaklivebackup.models.JsonFileStatus;
import io.gerhardt.keycloaklivebackup.utilities.UserDataManager;
import io.gerhardt.keycloaklivebackup.utilities.PrometheusExporter;
import org.jboss.logging.Logger;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.events.admin.OperationType;
import org.keycloak.models.KeycloakSession;

import java.util.Map;


public class UserEventListenerProvider implements EventListenerProvider {
    private final Logger logger = Logger.getLogger(UserEventListenerProvider.class);

    private final UserDataManager userDataManager = new UserDataManager();

    private final KeycloakSession keycloakSession;

    public UserEventListenerProvider(KeycloakSession keycloakSession) {
        this.keycloakSession = keycloakSession;
    }

    @Override
    public void onEvent(Event event) {
        //Record metric
        PrometheusExporter.instance().recordEvents(event.getRealmId());

        //Process event by type
        JsonFileStatus jsonFileStatus = JsonFileStatus.DELETE;
        switch (event.getType()) {
            case LOGIN:
            case REGISTER:
            case UPDATE_PROFILE: {
                jsonFileStatus = JsonFileStatus.CREATE;
            }
            case DELETE_ACCOUNT: {
                try {
                    userDataManager.exportUserData(keycloakSession, jsonFileStatus, event.getUserId(), event.getRealmId());
                } catch (Exception e) {
                    logger.error("Event:" + event.getType() + ", UserID:" + event.getUserId() + ", Realm:" + event.getRealmId(), e);
                }
                break;
            }
            default:
                break;
        }

        PrometheusExporter.instance().recordJsonFilesNumber(event.getRealmId());

        logger.info("Event Occurred:" + toString(event));
    }


    @Override
    public void onEvent(AdminEvent adminEvent, boolean b) {
        PrometheusExporter.instance().recordEvents(adminEvent.getRealmId());

        JsonFileStatus jsonFileStatus = JsonFileStatus.DELETE;
        switch (adminEvent.getOperationType()) {
            case ACTION:
            case CREATE:
            case UPDATE: {
                jsonFileStatus = JsonFileStatus.CREATE;
            }
            case DELETE: {
                try {
                    String[] pathResource = adminEvent.getResourcePath().split("/");
                    if (!pathResource[0].equals("users")) {
                        break;
                    } else if (adminEvent.getOperationType() == OperationType.DELETE && pathResource.length > 2) {
                        userDataManager.exportUserData(keycloakSession, JsonFileStatus.CREATE, pathResource[1], adminEvent.getRealmId());
                    } else {
                        userDataManager.exportUserData(keycloakSession, jsonFileStatus, pathResource[1], adminEvent.getRealmId());
                    }
                } catch (Exception e) {
                    logger.error("Event:" + adminEvent.getOperationType() + ", Resource path:" + adminEvent.getResourcePath() + ", Realm:" + adminEvent.getRealmId(), e);
                }
                break;
            }
            default:
                break;
        }

        PrometheusExporter.instance().recordJsonFilesNumber(adminEvent.getRealmId());

        logger.info("Admin Event Occurred:" + toString(adminEvent));
    }

    @Override
    public void close() {

    }

    private String toString(Event event) {

        StringBuilder sb = new StringBuilder();


        sb.append("type=");

        sb.append(event.getType());

        sb.append(", realmId=");

        sb.append(event.getRealmId());

        sb.append(", clientId=");

        sb.append(event.getClientId());

        sb.append(", userId=");

        sb.append(event.getUserId());

        sb.append(", ipAddress=");

        sb.append(event.getIpAddress());


        if (event.getError() != null) {

            sb.append(", error=");

            sb.append(event.getError());

        }


        if (event.getDetails() != null) {

            for (Map.Entry<String, String> e : event.getDetails().entrySet()) {

                sb.append(", ");

                sb.append(e.getKey());

                if (e.getValue() == null || e.getValue().indexOf(' ') == -1) {

                    sb.append("=");

                    sb.append(e.getValue());

                } else {

                    sb.append("='");

                    sb.append(e.getValue());

                    sb.append("'");

                }

            }

        }


        return sb.toString();

    }


    private String toString(AdminEvent adminEvent) {

        StringBuilder sb = new StringBuilder();


        sb.append("operationType=");

        sb.append(adminEvent.getOperationType());

        sb.append(", realmId=");

        sb.append(adminEvent.getAuthDetails().getRealmId());

        sb.append(", clientId=");

        sb.append(adminEvent.getAuthDetails().getClientId());

        sb.append(", userId=");

        sb.append(adminEvent.getAuthDetails().getUserId());

        sb.append(", ipAddress=");

        sb.append(adminEvent.getAuthDetails().getIpAddress());

        sb.append(", resourcePath=");

        sb.append(adminEvent.getResourcePath());


        if (adminEvent.getError() != null) {

            sb.append(", error=");

            sb.append(adminEvent.getError());

        }


        return sb.toString();

    }
}