package io.gerhardt.keycloaklivebackup.utilities;

import io.gerhardt.keycloaklivebackup.models.User;
import org.jboss.logging.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Objects;

public class JsonManager {
    private final Logger logger = Logger.getLogger(JsonManager.class);
    private final UserDataManager userDataManager;
    private final CsvManager csvManager;

    public UserDataManager getUserDataManager() {
        return userDataManager;
    }

    public JsonManager(UserDataManager userDataManager) {
        this.userDataManager = userDataManager;
        this.csvManager = new CsvManager(this);
    }

    public void createFile(User user, String realmId) {
        deleteJsonFileBeforeCreate(user);

        String filePath = getJsonsPath() + userDataManager.encodeUsername(user.getUsername()) + ".json";
        try (FileWriter fileWriter = new FileWriter(filePath)){
            int numberOfFilesInitially = getNumberOfJsonFiles();

            fileWriter.write(user.toString());

            csvManager.appendCsvWithUser(user.getId(), user.getUsername());

            if (numberOfFilesInitially == getNumberOfJsonFiles()) {
                PrometheusExporter.instance().recordUpdatedJsonFilesNumber(realmId);
            }

            logger.info("Backup file successfully created for:" + user.getId());
        } catch (Exception e) {
            logger.error("Can't create the backup file", e);
        }
    }

    private void deleteJsonFileBeforeCreate(User user){
        File[] jsonsDirectoryFiles = new File(getJsonsPath()).listFiles();
        if (jsonsDirectoryFiles == null) {
            logger.info("JSON files directory is null. Can not create the JSON file!");
            return;
        }
        for (File jsonFile : jsonsDirectoryFiles) {
            if (jsonFile.getName().equals(userDataManager.encodeUsername(user.getUsername()) + ".json")) {
                boolean deleted = jsonFile.delete();
                if (!deleted) {
                    logger.error("Could not delete JSON file: " + jsonFile.getAbsolutePath());
                }
            }
        }
    }

    public void deleteFile(String userId, String realmId) throws IOException {
        File jsonsDirectory = new File(getJsonsPath());
        File[] filesInDirectory = jsonsDirectory.listFiles();
        if (filesInDirectory == null) {
            logger.info("JSON files directory is null. Can not delete the json file!");
            return;
        }

        String username = csvManager.getEncodedUsernameByID(userId);
        if (username == null) {
            logger.error("Username is null to user with keycloak ID: " + userId);
            return;
        }

        csvManager.deleteUserFromCsv(userId);


        File jsonFile = new File(getJsonsPath() + username + ".json");
        if (jsonFile.exists()) {
            boolean deleted = jsonFile.delete();
            if (!deleted) {
                logger.error("Could not delete JSON file: " + jsonFile.getAbsolutePath());
            }
            PrometheusExporter.instance().recordDeletedJsonFilesNumber(realmId);
            logger.info("File successfully removed:" + getJsonsPath() + username + ".json");
        }
    }

    public String getJsonsPath() {
        return System.getenv("JSONS_PATH");
    }

    public int getNumberOfJsonFiles() {
        File jsonsDirectory = new File(getJsonsPath());
        return Objects.requireNonNull(jsonsDirectory.listFiles(),
                "Jsons directory could not be listed.").length - 1;
    }
}
