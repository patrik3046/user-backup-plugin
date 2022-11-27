package io.gerhardt.keycloaklivebackup.utilities;

import org.jboss.logging.Logger;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class CsvManager {
    private final Logger logger = Logger.getLogger(JsonManager.class);
    private final UserDataManager userDataManager;
    private final File csvFile;

    public CsvManager(JsonManager jsonManager) {
        this.userDataManager = jsonManager.getUserDataManager();
        this.csvFile = new File(jsonManager.getJsonsPath() + "keycloakid-username.csv");
    }

    public Map<String, String> getCsvData() throws IOException {
        if (!csvFile.exists()) {
            createCsvFile();
        }

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(csvFile))) {
            Map<String, String> keycloakIdAndUsername = new HashMap<>();
            String line = bufferedReader.readLine();
            while (line != null && !line.equals("\n")) {
                String[] partsOfTheLine = line.trim().split(",");
                String username = partsOfTheLine[1];
                String keycloakId = partsOfTheLine[0];

                keycloakIdAndUsername.put(keycloakId, username);

                line = bufferedReader.readLine();
            }

            return keycloakIdAndUsername;
        } catch (Exception exception) {
            logger.error("An error occurred while trying to read the CSV file.");
            return new HashMap<>();
        }
    }

    public void setCsvData(Map<String, String> keycloakIdAndUsername) throws IOException {
        if (!csvFile.exists()) {
            createCsvFile();
        }

        try (FileWriter fileWriter = new FileWriter(csvFile)) {
            StringBuilder data = new StringBuilder();

            for (Map.Entry<String, String> entry : keycloakIdAndUsername.entrySet()) {
                data.append(entry.getKey()).append(",").append(entry.getValue()).append("\n");
            }

            fileWriter.write(data.toString());
        } catch (Exception exception) {
            logger.error("An error occurred while trying to write the CSV file.");
        }
    }

    public void appendCsvWithUser(String keycloakId, String username) throws IOException {
        Map<String, String> users = getCsvData();
        if (!users.containsKey(keycloakId)) {
            users.put(keycloakId, userDataManager.encodeUsername(username));
            setCsvData(users);
        }
    }

    public void deleteUserFromCsv(String userId) throws IOException {
        Map<String, String> users = getCsvData();
        if (users.containsKey(userId)) {
            users.remove(userId);
            setCsvData(users);
        }
    }

    public String getEncodedUsernameByID(String userId) throws IOException {
        return getCsvData().get(userId);
    }

    public void createCsvFile() throws IOException {
        boolean created = csvFile.createNewFile();
        if (!created) {
            logger.error("Could not create CSV file: " + csvFile.getAbsolutePath());
        }
    }

    public void updateCsv(KeycloakSession keycloakSession) throws IOException {
        long time = System.currentTimeMillis();
        String realmName = System.getenv("REALM_NAME");
        Stream<UserModel> usersOnTheServer = keycloakSession.users().getUsersStream(keycloakSession.realms().getRealmByName(realmName));

        Map<String, String> keycloakIdAndUsername = new HashMap<>();
        usersOnTheServer.forEach(user -> keycloakIdAndUsername.put(user.getId(), userDataManager.encodeUsername(user.getUsername())));

        setCsvData(keycloakIdAndUsername);

        logger.info("CSV keycloakId-username map is written in " + (System.currentTimeMillis() - time) + " milliseconds.");
    }
}
