package io.gerhardt.keycloaklivebackup.utilities;

import io.gerhardt.keycloaklivebackup.models.Credential;
import io.gerhardt.keycloaklivebackup.models.JsonFileStatus;
import io.gerhardt.keycloaklivebackup.models.User;
import org.keycloak.credential.CredentialModel;
import org.keycloak.models.*;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.models.utils.Base32;

import java.io.IOException;
import java.util.*;

public class UserDataManager {
    private final JsonManager jsonManager = new JsonManager(this);

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
        List<RoleModel> roleModelList = new ArrayList<>();
        userModel.getRealmRoleMappingsStream().forEach(roleModelList::add);
        List<String> realmRoles = new ArrayList<>();
        for (RoleModel model : roleModelList) {
            realmRoles.add(model.getName());
        }

        List<ClientModel> clients = new ArrayList<>();
        keycloakSession.clientLocalStorage().getClientsStream(realmModel).forEach(clients::add);
        List<String> clientIds = new ArrayList<>();
        for (ClientModel client : clients) {
            clientIds.add(client.getClientId());
        }

        Map<String, List<String>> clientRoles = new HashMap<>();
        for (String clientId : clientIds) {
            List<String> clientRolesName = new ArrayList<>();
            userModel.getClientRoleMappingsStream(keycloakSession.clientStorageManager().getClientByClientId(realmModel, clientId))
                    .forEach(roleModel -> clientRolesName.add(roleModel.getName()));

            clientRoles.put(clientId, clientRolesName);
        }

        List<CredentialModel> credentialModelList = new ArrayList<>();
        keycloakSession.userCredentialManager().getStoredCredentialsStream(realmModel, userModel).forEach(credentialModelList::add);

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

        return new User(realmRoles, clientRoles, id, lastName, firstName, emailVerified, credentials, createdTimestamp, email, enabled, username, realm);

    }

    public String encodeUsername(String username) {
        // this will not have the padding in the string
        return Base32.encode(username.toLowerCase().getBytes());
    }
}
