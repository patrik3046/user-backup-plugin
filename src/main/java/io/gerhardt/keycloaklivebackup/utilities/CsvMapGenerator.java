package io.gerhardt.keycloaklivebackup.utilities;

import org.jboss.logging.Logger;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;

import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

public final class CsvMapGenerator {
    private final Logger logger = Logger.getLogger(CsvMapGenerator.class);
    private final UserDataManager userDataManager = new UserDataManager();
    private final CsvManager csvManager = new CsvManager(new JsonManager(userDataManager));
    //Set your realm name first in src/main/resources/realm.name
    private final String REALM_NAME = System.getenv("REALM_NAME");


    public void updateCsv(KeycloakSession keycloakSession) throws IOException {
        long time = System.currentTimeMillis();
        Stream<UserModel> usersOnTheServer = keycloakSession.users().getUsersStream(keycloakSession.realms().getRealmByName(REALM_NAME));

        Map<String, String> keycloakIdAndUsername = new HashMap<>();
        usersOnTheServer.forEach(user -> keycloakIdAndUsername.put(user.getId(), userDataManager.encodeUsername(user.getUsername())));

        csvManager.setCsvData(keycloakIdAndUsername);

        logger.info("CSV keycloakId-username map is written in " + (System.currentTimeMillis() - time) + " milliseconds.");
    }
}
