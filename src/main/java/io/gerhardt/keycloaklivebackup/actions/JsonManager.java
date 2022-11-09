package io.gerhardt.keycloaklivebackup.actions;

import io.gerhardt.keycloaklivebackup.metrics.PrometheusExporter;
import io.gerhardt.keycloaklivebackup.models.User;
import org.jboss.logging.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Scanner;

public class JsonManager {
    private final Logger LOG = Logger.getLogger(JsonManager.class);
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
        try {
            int numberOfFilesInitially = getNumberOfJsonFiles();

            File[] jsonsDirectoryFiles = new File(getJsonsPath()).listFiles();
            if (jsonsDirectoryFiles == null) {
                LOG.info("JSON files directory is null. Can not create the JSON file!");
                return;
            }
            for (File jsonFile : jsonsDirectoryFiles) {
                if (jsonFile.getName().equals(userDataManager.encodeUsername(user.getUsername()) + ".json")) {
                    boolean deleted = jsonFile.delete();
                    if(!deleted){
                        LOG.error("Could not delete JSON file: " + jsonFile.getAbsolutePath());
                    }
                }
            }

            FileWriter fileWriter = new FileWriter(getJsonsPath() + userDataManager.encodeUsername(user.getUsername()) + ".json");
            fileWriter.write(user.toString());
            fileWriter.close();

            csvManager.appendCsvWithUser(user.getId(), user.getUsername());

            if (numberOfFilesInitially == getNumberOfJsonFiles()) {
                PrometheusExporter.instance().recordUpdatedJsonFilesNumber(realmId);
            }

            LOG.info("Backup file successfully created for:" + user.getId());
        } catch (Exception e) {
            LOG.error("Can't create the backup file", e);
        }
    }

    //TODO delete the user from the csv
    public void deleteFile(String userId, String realmId) throws IOException {
        File jsonsDirectory = new File(getJsonsPath());
        File[] filesInDirectory = jsonsDirectory.listFiles();

        String username = csvManager.getEncodedUsernameByID(userId);
        if (username == null) {
            LOG.error("Username is null to user with keycloak ID: " + userId);
            return;
        }

        csvManager.deleteUserFromCsv(userId);

        if (filesInDirectory == null) {
            LOG.info("JSON files directory is null. Can not delete the json file!");
            return;
        }
        for (File file : filesInDirectory) {
            if (file.getName().equals(username + ".json")) {
                File jsonFile = new File(file.getAbsolutePath());
                if (jsonFile.exists()) {
                    boolean deleted = jsonFile.delete();
                    if(!deleted){
                        LOG.error("Could not delete JSON file: " + jsonFile.getAbsolutePath());
                    }
                    PrometheusExporter.instance().recordDeletedJsonFilesNumber(realmId);
                    LOG.info("File successfully removed:" + getJsonsPath() + username + ".json");
                }
                break;
            }
        }
    }

    public String getJsonsPath() {
        return new Scanner(Objects.requireNonNull(JsonManager.class.getClassLoader().getResourceAsStream("jsons.path"),
                "Could not read jsons path."), StandardCharsets.UTF_8).nextLine();
    }

    public int getNumberOfJsonFiles() {
        File jsonsDirectory = new File(getJsonsPath());
        return Objects.requireNonNull(jsonsDirectory.listFiles(),
                "Jsons directory could not be listed.").length;
    }
}