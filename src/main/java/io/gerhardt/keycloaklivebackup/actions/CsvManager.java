package io.gerhardt.keycloaklivebackup.actions;

import org.jboss.logging.Logger;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class CsvManager {
    private final Logger LOG = Logger.getLogger(JsonManager.class);
    private final UserDataManager userDataManager;
    private final File csvFile ;

    public CsvManager(JsonManager jsonManager) {
        this.userDataManager = jsonManager.getUserDataManager();
        this.csvFile = new File(jsonManager.getJsonsPath() + "keycloakid-username.csv");
    }

    public Map<String, String> getCsvData() throws IOException {
        if (!csvFile.exists()) {
            createCsvFile();
        }

        BufferedReader bufferedReader = new BufferedReader(new FileReader(csvFile));
        Map<String, String> keycloakIdAndUsername = new HashMap<>();
        String line = bufferedReader.readLine();
        while (line != null && !line.equals("\n")) {
            String[] partsOfTheLine = line.trim().split(",");
            String username = partsOfTheLine[1];
            String keycloakId = partsOfTheLine[0];

            keycloakIdAndUsername.put(keycloakId, username);

            line = bufferedReader.readLine();
        }

        bufferedReader.close();

        return keycloakIdAndUsername;
    }

    public void setCsvData(Map<String, String> keycloakIdAndUsername) throws IOException {
        if (!csvFile.exists()) {
            createCsvFile();
        }

        FileWriter fileWriter = new FileWriter(csvFile);
        StringBuilder data = new StringBuilder();

        for (Map.Entry<String, String> entry : keycloakIdAndUsername.entrySet()) {
            data.append(entry.getKey()).append(",").append(entry.getValue()).append("\n");
        }

        fileWriter.write(data.toString());
        fileWriter.close();
    }

    public void appendCsvWithUser(String keycloakId, String username) throws IOException {
        Map<String, String> users = getCsvData();
        if (!users.containsKey(keycloakId)) {
            users.put(keycloakId, userDataManager.encodeUsername(username));
            setCsvData(users);
        }
    }

    public void deleteUserFromCsv(String keycloakId) throws IOException {
        Map<String, String> users = getCsvData();
        if (users.containsKey(keycloakId)) {
            users.remove(keycloakId);
            setCsvData(users);
        }
    }

    public String getEncodedUsernameByID(String userId) throws IOException {
        return getCsvData().get(userId);
    }

    public void createCsvFile() throws IOException {
        boolean created = csvFile.createNewFile();
        if(!created){
            LOG.error("Could not create CSV file: " + csvFile.getAbsolutePath());
        }
        csvFile.setReadable(true);
        csvFile.setWritable(true);
    }
}
