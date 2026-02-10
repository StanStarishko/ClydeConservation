package com.conservation.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Manager class for loading and saving application settings.
 * 
 * Handles JSON serialisation/deserialisation of Settings object
 * using Gson library. Creates default settings if file doesn't exist.
 * 
 * Settings file location: config/settings.json
 */
public class SettingsManager {
    
    private static final String SETTINGS_FILE_PATH = "config/settings.json";
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    
    private static Settings currentSettings;
    
    /**
     * Loads settings from JSON file.
     * 
     * If file doesn't exist, creates new default settings and saves them.
     * If file is corrupted, returns default settings and logs error.
     * 
     * @return Settings object loaded from file or default settings
     */
    public static Settings loadSettings() {
        File settingsFile = new File(SETTINGS_FILE_PATH);
        
        // If settings file doesn't exist, create default
        if (!settingsFile.exists()) {
            System.out.println("Settings file not found. Creating default settings...");
            Settings defaultSettings = new Settings();
            saveSettings(defaultSettings);
            currentSettings = defaultSettings;
            return defaultSettings;
        }
        
        // Try to load existing settings
        try (FileReader reader = new FileReader(settingsFile)) {
            currentSettings = gson.fromJson(reader, Settings.class);
            System.out.println("Settings loaded successfully from " + SETTINGS_FILE_PATH);
            return currentSettings;
        } catch (IOException ioException) {
            System.err.println("Error loading settings: " + ioException.getMessage());
            System.err.println("Using default settings instead.");
            currentSettings = new Settings();
            return currentSettings;
        }
    }
    
    /**
     * Saves settings to JSON file.
     * 
     * Creates parent directory if it doesn't exist.
     * Overwrites existing file with new settings.
     * 
     * @param settings the Settings object to save
     * @return true if save successful, false otherwise
     */
    public static boolean saveSettings(Settings settings) {
            return saveSettings(settings, SETTINGS_FILE_PATH);
    }

    /**
     * Saves settings to a specific JSON file path.
     *
     * @param settings the Settings object to save
     * @param filePath the target file path
     * @return true if save successful, false otherwise
     */
    public static boolean saveSettings(Settings settings, String filePath) {
        // Validate parameters
        if (settings == null) {
            System.err.println("Cannot save null settings");
            return false;
        }

        if (filePath == null || filePath.trim().isEmpty()) {
            System.err.println("Config path cannot be null or empty");
            return false;
        }

        try {
            // Create parent directory if needed
            File configFile = new File(filePath);
            File parentDir = configFile.getParentFile();

            if (parentDir != null && !parentDir.exists()) {
                if (!parentDir.mkdirs()) {
                    System.err.println("Failed to create config directory: " +
                            parentDir.getAbsolutePath());
                    return false;
                }
            }

            // Write settings to file with pretty printing
            try (FileWriter writer = new FileWriter(configFile)) {
                gson.toJson(settings, writer);
            }

            System.out.println("Settings saved successfully to " + filePath);

            // Only update currentSettings if saving to default path
            if (filePath.equals(SETTINGS_FILE_PATH)) {
                currentSettings = settings;
            }

            return true;

        } catch (IOException ioException) {
            System.err.println("Failed to save settings: " + ioException.getMessage());
            return false;
        }
    }

    /**
     * Gets the current settings instance.
     * 
     * If settings haven't been loaded yet, loads them from file.
     * 
     * @return current Settings object
     */
    public static Settings getSettings() {
        if (currentSettings == null) {
            return loadSettings();
        }
        return currentSettings;
    }
    
    /**
     * Checks if this is the first run of the application.
     * 
     * @return true if firstRun flag is set, false otherwise
     */
    public static boolean isFirstRun() {
        return getSettings().isFirstRun();
    }
    
    /**
     * Updates the firstRun flag and saves settings.
     * 
     * Called after initial test data has been loaded.
     * 
     * @param firstRun new value for firstRun flag
     */
    public static void setFirstRun(boolean firstRun) {
        Settings settings = getSettings();
        settings.setFirstRun(firstRun);
        saveSettings(settings);
    }
    
    /**
     * Gets keeper constraints from settings.
     * 
     * @return KeeperConstraints object
     */
    public static Settings.KeeperConstraints getKeeperConstraints() {
        return getSettings().getKeeperConstraints();
    }
    
    /**
     * Gets animal rules from settings.
     * 
     * @return AnimalRules object
     */
    public static Settings.AnimalRules getAnimalRules() {
        return getSettings().getAnimalRules();
    }
}
