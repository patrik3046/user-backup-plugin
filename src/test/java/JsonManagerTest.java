import io.gerhardt.keycloaklivebackup.utilities.JsonManager;
import io.gerhardt.keycloaklivebackup.utilities.UserDataManager;
import io.gerhardt.keycloaklivebackup.models.User;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class JsonManagerTest {

    @Test
    public void createFileTest() {
        UserDataManager userDataManager = new UserDataManager();
        JsonManager jsonManager = new JsonManager(userDataManager);
        User user = new User(new ArrayList<>(), new HashMap<>(), "userid", "", "", true, new ArrayList<>(), 0L, "", true, "", "");
        jsonManager.createFile(user, "test");
        Assert.assertTrue(new File(jsonManager.getJsonsPath() + userDataManager.encodeUsername(user.getUsername()) + ".json").exists());

        new File(jsonManager.getJsonsPath() + userDataManager.encodeUsername(user.getUsername()) + ".json").delete();
    }

    @Test
    public void deleteFileTest() throws IOException {
        UserDataManager userDataManager = new UserDataManager();
        JsonManager jsonManager = new JsonManager(userDataManager);
        jsonManager.deleteFile("userid", "test");
        Assert.assertFalse(new File(jsonManager.getJsonsPath() + "userid.json").exists());

        User user = new User(new ArrayList<>(), new HashMap<>(), "userid", "", "", true, new ArrayList<>(), 0L, "", true, "", "");
        jsonManager.createFile(user, "test");
        jsonManager.deleteFile("userid", "test");
        Assert.assertFalse(new File(jsonManager.getJsonsPath() + "userid.json").exists());
    }
}
