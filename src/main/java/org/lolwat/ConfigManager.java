package org.lolwat;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import org.dreambot.api.utilities.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;

public class ConfigManager {
    private static ConfigManager instance;
    private final HashMap<Object, Object> config;
    private final HashMap<String, Integer> itemThresholds;

    public ConfigManager() {
        config = new HashMap<>();
        itemThresholds = new HashMap<>();
    }

    private JsonObject getDefaultProfile() {
        JsonObject defaultProfile = new JsonObject();
        defaultProfile.addProperty("min_profit", 50);
        return defaultProfile;
    }

    public void loadFromProfile(String p) {
        String filePath = System.getProperty("user.dir") + "/PyfaAlcher/" + p + ".json";

        try {
            Gson gson = new Gson();

            File file = new File(filePath);
            if (!file.exists()) {
                file.getParentFile().mkdirs();

                JsonObject defaultProfile = getDefaultProfile();
                FileWriter fileWriter = new FileWriter(file);
                gson.toJson(defaultProfile, fileWriter);
                fileWriter.close();
            }

            JsonObject jsonObject = gson.fromJson(new FileReader(filePath), JsonObject.class);
            JsonObject defaultProfile = getDefaultProfile();

            for (String key : defaultProfile.keySet()) {
                if (!jsonObject.has(key)) {
                    jsonObject.add(key, defaultProfile.get(key));
                }
            }

            FileWriter fileWriter = new FileWriter(file);
            gson.toJson(jsonObject, fileWriter);
            fileWriter.close();
        } catch (IOException | JsonSyntaxException ignored) {
            Logger.error("Encountered an error during setup");
        }
    }

    public String getConfigString(String key) {
        String value = config.get(key).toString();
        return value.replace("\"", "");
    }

    public boolean getConfigBoolean(String key) {
        String value = config.get(key).toString().replace("\"", "");
        return Objects.equals(value, "true") || Objects.equals(value, "1");
    }

    public int getConfigInt(String key) {
        return Integer.parseInt(config.get(key).toString());
    }

    public double getConfigDouble(String key) {
        return Double.parseDouble(config.get(key).toString());
    }

    public static ConfigManager getInstance() {
        return instance;
    }

    public static void setInstance(ConfigManager inst) {
        instance = inst;
    }
}
