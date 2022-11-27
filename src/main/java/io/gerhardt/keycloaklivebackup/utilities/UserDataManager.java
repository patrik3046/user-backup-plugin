package io.gerhardt.keycloaklivebackup.utilities;

import io.gerhardt.keycloaklivebackup.models.Credential;
import io.gerhardt.keycloaklivebackup.models.JsonFileStatus;
import io.gerhardt.keycloaklivebackup.models.User;
import org.jboss.logging.Logger;
import org.keycloak.credential.CredentialModel;
import org.keycloak.models.*;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.models.utils.Base32;

import java.io.IOException;
import java.util.*;

public class UserDataManager {
    private final JsonManager jsonManager = new JsonManager(this);

    private final Logger logger = Logger.getLogger(UserDataManager.class);

    public void exportUserData(KeycloakSession keycloakSession, JsonFileStatus jsonFileStatus, String userId, String realmId) throws IOException {
        switch (jsonFileStatus) {
            case CREATE: {
                RealmModel realmModel = keycloakSession.realms().getRealm(realmId);
                UserModel userModel = keycloakSession.users().getUserById(realmModel, userId);
                User user = convertUserModelToUser(keycloakSession, realmModel, userModel);
                jsonManager.createFile(user, realmId);
            }
            break;
            case DELETE: {
                jsonManager.deleteFile(userId, realmId);
            }
            break;
            default:
                break;
        }
    }

    public User convertUserModelToUser(KeycloakSession keycloakSession, RealmModel realmModel, UserModel userModel) {
        List<String> realmRoles = getRealmsRolesToUser(userModel);

        List<String> clientIds = getClientIds(keycloakSession, realmModel);

        Map<String, List<String>> clientRoles = getClientRolesToUser(clientIds, userModel, realmModel, keycloakSession);

        String id = userModel.getId();
        String lastName = userModel.getLastName();
        String firstName = userModel.getFirstName();
        Boolean emailVerified = userModel.isEmailVerified();
        Long createdTimestamp = userModel.getCreatedTimestamp();
        String email = userModel.getEmail();
        Boolean enabled = userModel.isEnabled();
        String username = userModel.getUsername();
        String realm = realmModel.getId();

        if (email == null) {
            email = "";
        }

        if (lastName == null) {
            lastName = "";
        }

        if (firstName == null) {
            firstName = "";
        }

        List<Credential> credentials = getCredentialsToUser(userModel, realmModel, keycloakSession);

        return new User(realmRoles, clientRoles, id, lastName, firstName, emailVerified, credentials, createdTimestamp, email, enabled, username, realm);

    }

    private List<Credential> getCredentialsToUser(UserModel userModel, RealmModel realmModel, KeycloakSession keycloakSession) {
        try {
            List<CredentialModel> credentialModelList = new ArrayList<>();
            keycloakSession.userCredentialManager().getStoredCredentialsStream(realmModel, userModel).forEach(credentialModelList::add);
            List<Credential> credentials = new ArrayList<>();
            for (CredentialModel credentialModel : credentialModelList) {
                PasswordCredentialModel passwordCredentialModel = PasswordCredentialModel.createFromCredentialModel(credentialModel);
                int hashIterations = passwordCredentialModel.getPasswordCredentialData().getHashIterations();
                String algorithm = passwordCredentialModel.getPasswordCredentialData().getAlgorithm();
                String type = passwordCredentialModel.getType();
                String salt = Base64.getEncoder().encodeToString(passwordCredentialModel.getPasswordSecretData().getSalt());
                String value = passwordCredentialModel.getPasswordSecretData().getValue();
                credentials.add(new Credential(hashIterations, algorithm, type, salt, value));
            }
            return credentials;
        } catch (Exception exception) {
            logger.error("Can not get credentials to user. Reason: " + exception);
            return new ArrayList<>();
        }
    }

    private Map<String, List<String>> getClientRolesToUser(List<String> clientIds, UserModel userModel, RealmModel realmModel, KeycloakSession keycloakSession) {
        try {
            Map<String, List<String>> clientRoles = new HashMap<>();
            for (String clientId : clientIds) {
                List<String> clientRolesName = new ArrayList<>();
                userModel.getClientRoleMappingsStream(keycloakSession.clientStorageManager().getClientByClientId(realmModel, clientId))
                        .forEach(roleModel -> clientRolesName.add(roleModel.getName()));

                clientRoles.put(clientId, clientRolesName);
            }
            return clientRoles;
        } catch (Exception exception) {
            logger.error("Can not get client roles to user. Reason: " + exception);
            return new HashMap<>();
        }
    }

    private List<String> getClientIds(KeycloakSession keycloakSession, RealmModel realmModel) {
        try {
            List<ClientModel> clients = new ArrayList<>();
            keycloakSession.clientLocalStorage().getClientsStream(realmModel).forEach(clients::add);
            List<String> clientIds = new ArrayList<>();
            for (ClientModel client : clients) {
                clientIds.add(client.getClientId());
            }
            return clientIds;
        } catch (Exception exception) {
            logger.error("Can not get client ids to user. Reason: " + exception);
            return new ArrayList<>();
        }
    }

    private List<String> getRealmsRolesToUser(UserModel userModel) {
        try {
            List<RoleModel> roleModelList = new ArrayList<>();
            userModel.getRealmRoleMappingsStream().forEach(roleModelList::add);
            List<String> realmRoles = new ArrayList<>();
            for (RoleModel model : roleModelList) {
                realmRoles.add(model.getName());
            }
            return realmRoles;
        } catch (Exception exception) {
            logger.error("Can not get realm roles to user. Reason: " + exception);
            return new ArrayList<>();
        }
    }

    public String encodeUsername(String username) {
        // this will not have the padding in the string
        return Base32.encode(username.toLowerCase().getBytes());
    }
}
