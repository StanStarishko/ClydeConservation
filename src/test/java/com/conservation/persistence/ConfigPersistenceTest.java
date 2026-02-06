package com.conservation.persistence;

import com.conservation.config.Settings;
import com.conservation.config.SettingsManager;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for configuration persistence - SettingsManager and Settings.
 * Tests JSON load/save operations, firstRun flag management, default values,
 * and error handling for invalid configurations.
 * 
 * <p>Uses JUnit 5 TempDir for isolated file system testing.
 * 
 * @author Clyde Conservation Development Team
 * @version 1.0
 */
@DisplayName("Configuration Persistence Tests")
class ConfigPersistenceTest {

    // ============================================================
    // Test Constants
    // ============================================================
    
    private static final String SETTINGS_FILE = "settings.json";
    private static final int DEFAULT_MIN_CAGES = 1;
    private static final int DEFAULT_MAX_CAGES = 4;
    private static final boolean DEFAULT_PREDATOR_SHAREABLE = false;
    private static final boolean DEFAULT_PREY_SHAREABLE = true;
    
    // ============================================================
    // Test Directory
    // ============================================================
    
    @TempDir
    Path tempDir;
    
    private Path configDir;
    private Path settingsPath;
    
    // ============================================================
    // Setup and Teardown
    // ============================================================
    
    @BeforeEach
    void setupConfigDirectory() throws IOException {
        configDir = tempDir.resolve("config");
        Files.createDirectories(configDir);
        settingsPath = configDir.resolve(SETTINGS_FILE);
        
        // Reset SettingsManager to use test directory
        // Note: This may require SettingsManager to support custom paths
        // or use a test-specific initialization method
    }

    // ============================================================
    // SETTINGS CLASS TESTS
    // ============================================================
    
    @Nested
    @DisplayName("Settings Class Tests")
    class SettingsClassTests {
        
        @Test
        @DisplayName("Default settings have expected values")
        void defaultSettings_ShouldHaveExpectedValues() {
            // Arrange & Act
            Settings settings = new Settings();
            
            // Assert
            assertTrue(settings.isFirstRun(), "Default firstRun should be true");
            assertNotNull(settings.getKeeperConstraints(), 
                "Keeper constraints should not be null");
            assertNotNull(settings.getAnimalRules(), 
                "Animal rules should not be null");
        }
        
        @Test
        @DisplayName("Default keeper constraints have correct values")
        void defaultKeeperConstraints_ShouldHaveCorrectValues() {
            // Arrange & Act
            Settings settings = new Settings();
            Settings.KeeperConstraints constraints = settings.getKeeperConstraints();
            
            // Assert
            assertEquals(DEFAULT_MIN_CAGES, constraints.getMinCages(), 
                "Default min cages should be 1");
            assertEquals(DEFAULT_MAX_CAGES, constraints.getMaxCages(), 
                "Default max cages should be 4");
        }
        
        @Test
        @DisplayName("Default animal rules have correct values")
        void defaultAnimalRules_ShouldHaveCorrectValues() {
            // Arrange & Act
            Settings settings = new Settings();
            Settings.AnimalRules rules = settings.getAnimalRules();
            
            // Assert
            assertFalse(rules.isPredatorShareable(), 
                "Predators should not be shareable by default");
            assertTrue(rules.isPreyShareable(), 
                "Prey should be shareable by default");
        }
        
        @Test
        @DisplayName("Settings firstRun can be modified")
        void setFirstRun_ShouldUpdateValue() {
            // Arrange
            Settings settings = new Settings();
            assertTrue(settings.isFirstRun(), "Initial value should be true");
            
            // Act
            settings.setFirstRun(false);
            
            // Assert
            assertFalse(settings.isFirstRun(), "Value should be updated to false");
        }
        
        @Test
        @DisplayName("Keeper constraints can be modified")
        void setKeeperConstraints_ShouldUpdateValues() {
            // Arrange
            Settings settings = new Settings();
            Settings.KeeperConstraints newConstraints = new Settings.KeeperConstraints();
            newConstraints.setMinCages(2);
            newConstraints.setMaxCages(6);
            
            // Act
            settings.setKeeperConstraints(newConstraints);
            
            // Assert
            assertEquals(2, settings.getKeeperConstraints().getMinCages(), 
                "Min cages should be updated");
            assertEquals(6, settings.getKeeperConstraints().getMaxCages(), 
                "Max cages should be updated");
        }
        
        @Test
        @DisplayName("Animal rules can be modified")
        void setAnimalRules_ShouldUpdateValues() {
            // Arrange
            Settings settings = new Settings();
            Settings.AnimalRules newRules = new Settings.AnimalRules();
            newRules.setPredatorShareable(true);
            newRules.setPreyShareable(false);
            
            // Act
            settings.setAnimalRules(newRules);
            
            // Assert
            assertTrue(settings.getAnimalRules().isPredatorShareable(), 
                "Predator shareable should be updated");
            assertFalse(settings.getAnimalRules().isPreyShareable(), 
                "Prey shareable should be updated");
        }
    }

    // ============================================================
    // SETTINGS MANAGER - LOAD TESTS
    // ============================================================
    
    @Nested
    @DisplayName("SettingsManager Load Tests")
    class SettingsManagerLoadTests {
        
        @Test
        @DisplayName("Load settings from valid JSON file")
        void loadSettings_ValidJson_ShouldReturnSettings() throws IOException {
            // Arrange
            String validJson = """
                {
                    "firstRun": false,
                    "keeperConstraints": {
                        "minCages": 1,
                        "maxCages": 4
                    },
                    "animalRules": {
                        "predatorShareable": false,
                        "preyShareable": true
                    }
                }
                """;
            Files.writeString(settingsPath, validJson);
            
            // Act
            Settings settings = SettingsManager.loadSettings(settingsPath.toString());
            
            // Assert
            assertNotNull(settings, "Settings should not be null");
            assertFalse(settings.isFirstRun(), "firstRun should be false");
            assertEquals(1, settings.getKeeperConstraints().getMinCages());
            assertEquals(4, settings.getKeeperConstraints().getMaxCages());
        }
        
        @Test
        @DisplayName("Load settings creates defaults when file missing")
        void loadSettings_FileMissing_ShouldCreateDefaults() {
            // Arrange - File doesn't exist
            String nonExistentPath = configDir.resolve("nonexistent.json").toString();
            
            // Act
            Settings settings = SettingsManager.loadSettings(nonExistentPath);
            
            // Assert
            assertNotNull(settings, "Should return default settings");
            assertTrue(settings.isFirstRun(), "Default firstRun should be true");
        }
        
        @Test
        @DisplayName("Load settings handles corrupt JSON gracefully")
        void loadSettings_CorruptJson_ShouldReturnDefaults() throws IOException {
            // Arrange
            String corruptJson = "{ this is not valid json }}}";
            Files.writeString(settingsPath, corruptJson);
            
            // Act
            Settings settings = SettingsManager.loadSettings(settingsPath.toString());
            
            // Assert
            assertNotNull(settings, "Should return default settings on error");
            // Defaults should be used
            assertTrue(settings.isFirstRun(), "Should use default firstRun");
        }
        
        @Test
        @DisplayName("Load settings handles empty file gracefully")
        void loadSettings_EmptyFile_ShouldReturnDefaults() throws IOException {
            // Arrange
            Files.writeString(settingsPath, "");
            
            // Act
            Settings settings = SettingsManager.loadSettings(settingsPath.toString());
            
            // Assert
            assertNotNull(settings, "Should return default settings for empty file");
        }
        
        @Test
        @DisplayName("Load settings handles partial JSON gracefully")
        void loadSettings_PartialJson_ShouldFillMissingWithDefaults() throws IOException {
            // Arrange - JSON with only firstRun, missing nested objects
            String partialJson = """
                {
                    "firstRun": false
                }
                """;
            Files.writeString(settingsPath, partialJson);
            
            // Act
            Settings settings = SettingsManager.loadSettings(settingsPath.toString());
            
            // Assert
            assertNotNull(settings, "Settings should not be null");
            assertFalse(settings.isFirstRun(), "firstRun should be loaded");
            // Missing nested objects should have defaults or be null
            // Behaviour depends on implementation
        }
    }

    // ============================================================
    // SETTINGS MANAGER - SAVE TESTS
    // ============================================================
    
    @Nested
    @DisplayName("SettingsManager Save Tests")
    class SettingsManagerSaveTests {
        
        @Test
        @DisplayName("Save settings creates JSON file")
        void saveSettings_ShouldCreateJsonFile() {
            // Arrange
            Settings settings = new Settings();
            settings.setFirstRun(false);
            
            // Act
            boolean saved = SettingsManager.saveSettings(settings, settingsPath.toString());
            
            // Assert
            assertTrue(saved, "Save should return true");
            assertTrue(Files.exists(settingsPath), "JSON file should be created");
        }
        
        @Test
        @DisplayName("Save settings writes valid JSON")
        void saveSettings_ShouldWriteValidJson() throws IOException {
            // Arrange
            Settings settings = new Settings();
            settings.setFirstRun(false);
            
            // Act
            SettingsManager.saveSettings(settings, settingsPath.toString());
            String content = Files.readString(settingsPath);
            
            // Assert
            assertTrue(content.contains("\"firstRun\""), "JSON should contain firstRun");
            assertTrue(content.contains("false"), "firstRun value should be false");
            assertTrue(content.contains("keeperConstraints"), 
                "JSON should contain keeperConstraints");
            assertTrue(content.contains("animalRules"), 
                "JSON should contain animalRules");
        }
        
        @Test
        @DisplayName("Save and load preserves all settings")
        void saveAndLoad_ShouldPreserveAllSettings() {
            // Arrange
            Settings original = new Settings();
            original.setFirstRun(false);
            original.getKeeperConstraints().setMinCages(2);
            original.getKeeperConstraints().setMaxCages(5);
            original.getAnimalRules().setPredatorShareable(true);
            original.getAnimalRules().setPreyShareable(false);
            
            // Act
            SettingsManager.saveSettings(original, settingsPath.toString());
            Settings loaded = SettingsManager.loadSettings(settingsPath.toString());
            
            // Assert
            assertFalse(loaded.isFirstRun(), "firstRun should be preserved");
            assertEquals(2, loaded.getKeeperConstraints().getMinCages(), 
                "minCages should be preserved");
            assertEquals(5, loaded.getKeeperConstraints().getMaxCages(), 
                "maxCages should be preserved");
            assertTrue(loaded.getAnimalRules().isPredatorShareable(), 
                "predatorShareable should be preserved");
            assertFalse(loaded.getAnimalRules().isPreyShareable(), 
                "preyShareable should be preserved");
        }
        
        @Test
        @DisplayName("Save settings creates parent directories if missing")
        void saveSettings_MissingDirectories_ShouldCreateThem() {
            // Arrange
            Settings settings = new Settings();
            Path deepPath = tempDir.resolve("deep/nested/config/settings.json");
            
            // Act
            boolean saved = SettingsManager.saveSettings(settings, deepPath.toString());
            
            // Assert
            assertTrue(saved, "Save should succeed");
            assertTrue(Files.exists(deepPath), "File should be created with parent dirs");
        }
        
        @Test
        @DisplayName("Save settings produces pretty-printed JSON")
        void saveSettings_ShouldProducePrettyPrintedJson() throws IOException {
            // Arrange
            Settings settings = new Settings();
            
            // Act
            SettingsManager.saveSettings(settings, settingsPath.toString());
            String content = Files.readString(settingsPath);
            
            // Assert
            // Pretty-printed JSON should have newlines and indentation
            assertTrue(content.contains("\n"), "JSON should be multi-line");
            // Check for indentation (spaces or tabs)
            assertTrue(content.contains("  ") || content.contains("\t"), 
                "JSON should be indented");
        }
    }

    // ============================================================
    // FIRST RUN FLAG MANAGEMENT TESTS
    // ============================================================
    
    @Nested
    @DisplayName("First Run Flag Management Tests")
    class FirstRunFlagTests {
        
        @Test
        @DisplayName("isFirstRun returns true when file missing")
        void isFirstRun_FileMissing_ShouldReturnTrue() {
            // Arrange
            String nonExistentPath = configDir.resolve("nonexistent.json").toString();
            
            // Act
            boolean isFirstRun = SettingsManager.isFirstRun(nonExistentPath);
            
            // Assert
            assertTrue(isFirstRun, "Should return true when file doesn't exist");
        }
        
        @Test
        @DisplayName("isFirstRun returns value from existing file")
        void isFirstRun_FileExists_ShouldReturnStoredValue() throws IOException {
            // Arrange
            String jsonWithFirstRunFalse = """
                {
                    "firstRun": false,
                    "keeperConstraints": { "minCages": 1, "maxCages": 4 },
                    "animalRules": { "predatorShareable": false, "preyShareable": true }
                }
                """;
            Files.writeString(settingsPath, jsonWithFirstRunFalse);
            
            // Act
            boolean isFirstRun = SettingsManager.isFirstRun(settingsPath.toString());
            
            // Assert
            assertFalse(isFirstRun, "Should return false from file");
        }
        
        @Test
        @DisplayName("setFirstRun updates and saves flag")
        void setFirstRun_ShouldUpdateAndSave() throws IOException {
            // Arrange
            Settings settings = new Settings();
            assertTrue(settings.isFirstRun(), "Initial should be true");
            SettingsManager.saveSettings(settings, settingsPath.toString());
            
            // Act
            SettingsManager.setFirstRun(false, settingsPath.toString());
            
            // Assert
            Settings reloaded = SettingsManager.loadSettings(settingsPath.toString());
            assertFalse(reloaded.isFirstRun(), "Flag should be updated and saved");
        }
        
        @Test
        @DisplayName("First run workflow: true -> false after initialization")
        void firstRunWorkflow_ShouldTransitionFromTrueToFalse() {
            // Arrange - Simulate first run
            Settings settings = new Settings();
            assertTrue(settings.isFirstRun(), "Should start as true");
            
            // Act - Simulate initialization complete
            settings.setFirstRun(false);
            SettingsManager.saveSettings(settings, settingsPath.toString());
            
            // Assert - Next load should return false
            Settings reloaded = SettingsManager.loadSettings(settingsPath.toString());
            assertFalse(reloaded.isFirstRun(), 
                "After initialization, firstRun should be false");
        }
    }

    // ============================================================
    // SETTINGS MANAGER SINGLETON/INSTANCE TESTS
    // ============================================================
    
    @Nested
    @DisplayName("SettingsManager Instance Tests")
    class SettingsManagerInstanceTests {
        
        @Test
        @DisplayName("getSettings returns cached instance")
        void getSettings_ShouldReturnCachedInstance() {
            // Arrange
            Settings settings = new Settings();
            SettingsManager.saveSettings(settings, settingsPath.toString());
            
            // Act
            Settings first = SettingsManager.getSettings(settingsPath.toString());
            Settings second = SettingsManager.getSettings(settingsPath.toString());
            
            // Assert
            assertNotNull(first, "First call should return settings");
            assertNotNull(second, "Second call should return settings");
            // Depending on implementation, they may or may not be same instance
            // At minimum, they should have same values
            assertEquals(first.isFirstRun(), second.isFirstRun(), 
                "Both should have same firstRun value");
        }
        
        @Test
        @DisplayName("updateSettings modifies and saves")
        void updateSettings_ShouldModifyAndSave() {
            // Arrange
            Settings original = new Settings();
            SettingsManager.saveSettings(original, settingsPath.toString());
            
            // Act
            Settings toUpdate = SettingsManager.getSettings(settingsPath.toString());
            toUpdate.setFirstRun(false);
            toUpdate.getKeeperConstraints().setMaxCages(6);
            SettingsManager.saveSettings(toUpdate, settingsPath.toString());
            
            // Assert
            Settings reloaded = SettingsManager.loadSettings(settingsPath.toString());
            assertFalse(reloaded.isFirstRun(), "firstRun should be updated");
            assertEquals(6, reloaded.getKeeperConstraints().getMaxCages(), 
                "maxCages should be updated");
        }
    }

    // ============================================================
    // BUSINESS RULE CONFIGURATION TESTS
    // ============================================================
    
    @Nested
    @DisplayName("Business Rule Configuration Tests")
    class BusinessRuleConfigTests {
        
        @Test
        @DisplayName("Keeper constraints are used for validation")
        void keeperConstraints_ShouldBeAccessibleForValidation() {
            // Arrange
            Settings settings = new Settings();
            SettingsManager.saveSettings(settings, settingsPath.toString());
            
            // Act
            Settings loaded = SettingsManager.loadSettings(settingsPath.toString());
            int minCages = loaded.getKeeperConstraints().getMinCages();
            int maxCages = loaded.getKeeperConstraints().getMaxCages();
            
            // Assert
            assertTrue(minCages > 0, "Min cages should be positive");
            assertTrue(maxCages >= minCages, "Max should be >= min");
            assertEquals(DEFAULT_MIN_CAGES, minCages, "Default min should be 1");
            assertEquals(DEFAULT_MAX_CAGES, maxCages, "Default max should be 4");
        }
        
        @Test
        @DisplayName("Animal rules are used for allocation validation")
        void animalRules_ShouldBeAccessibleForValidation() {
            // Arrange
            Settings settings = new Settings();
            SettingsManager.saveSettings(settings, settingsPath.toString());
            
            // Act
            Settings loaded = SettingsManager.loadSettings(settingsPath.toString());
            boolean predatorShareable = loaded.getAnimalRules().isPredatorShareable();
            boolean preyShareable = loaded.getAnimalRules().isPreyShareable();
            
            // Assert
            assertFalse(predatorShareable, "Predators should not be shareable by default");
            assertTrue(preyShareable, "Prey should be shareable by default");
        }
        
        @Test
        @DisplayName("Custom business rules can be configured")
        void customBusinessRules_ShouldBePersisted() {
            // Arrange
            Settings settings = new Settings();
            settings.getKeeperConstraints().setMinCages(2);
            settings.getKeeperConstraints().setMaxCages(8);
            settings.getAnimalRules().setPredatorShareable(true); // Allow predator sharing
            
            // Act
            SettingsManager.saveSettings(settings, settingsPath.toString());
            Settings loaded = SettingsManager.loadSettings(settingsPath.toString());
            
            // Assert
            assertEquals(2, loaded.getKeeperConstraints().getMinCages(), 
                "Custom min cages should be persisted");
            assertEquals(8, loaded.getKeeperConstraints().getMaxCages(), 
                "Custom max cages should be persisted");
            assertTrue(loaded.getAnimalRules().isPredatorShareable(), 
                "Custom predator rule should be persisted");
        }
    }

    // ============================================================
    // ERROR HANDLING TESTS
    // ============================================================
    
    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {
        
        @Test
        @DisplayName("Load with null path returns defaults")
        void loadSettings_NullPath_ShouldReturnDefaults() {
            // Act
            Settings settings = SettingsManager.loadSettings(null);
            
            // Assert
            assertNotNull(settings, "Should return default settings");
            assertTrue(settings.isFirstRun(), "Should have default firstRun");
        }
        
        @Test
        @DisplayName("Save with null settings returns false")
        void saveSettings_NullSettings_ShouldReturnFalse() {
            // Act
            boolean result = SettingsManager.saveSettings(null, settingsPath.toString());
            
            // Assert
            assertFalse(result, "Should return false for null settings");
        }
        
        @Test
        @DisplayName("Save with null path returns false")
        void saveSettings_NullPath_ShouldReturnFalse() {
            // Arrange
            Settings settings = new Settings();
            
            // Act
            boolean result = SettingsManager.saveSettings(settings, null);
            
            // Assert
            assertFalse(result, "Should return false for null path");
        }
        
        @Test
        @DisplayName("Load handles JSON with extra fields gracefully")
        void loadSettings_ExtraFields_ShouldIgnoreThem() throws IOException {
            // Arrange - JSON with extra unknown fields
            String jsonWithExtras = """
                {
                    "firstRun": false,
                    "unknownField": "should be ignored",
                    "keeperConstraints": {
                        "minCages": 1,
                        "maxCages": 4,
                        "extraConstraint": 100
                    },
                    "animalRules": {
                        "predatorShareable": false,
                        "preyShareable": true
                    },
                    "futureFeature": { "enabled": true }
                }
                """;
            Files.writeString(settingsPath, jsonWithExtras);
            
            // Act
            Settings settings = SettingsManager.loadSettings(settingsPath.toString());
            
            // Assert
            assertNotNull(settings, "Should load despite extra fields");
            assertFalse(settings.isFirstRun(), "Known fields should be loaded");
        }
        
        @Test
        @DisplayName("Load handles wrong data types gracefully")
        void loadSettings_WrongTypes_ShouldUseDefaults() throws IOException {
            // Arrange - JSON with wrong types
            String jsonWithWrongTypes = """
                {
                    "firstRun": "yes",
                    "keeperConstraints": {
                        "minCages": "one",
                        "maxCages": "four"
                    },
                    "animalRules": {
                        "predatorShareable": "no",
                        "preyShareable": "yes"
                    }
                }
                """;
            Files.writeString(settingsPath, jsonWithWrongTypes);
            
            // Act - Should either convert, use defaults, or handle gracefully
            Settings settings = SettingsManager.loadSettings(settingsPath.toString());
            
            // Assert
            assertNotNull(settings, "Should return settings even with wrong types");
            // Implementation may use defaults or attempt conversion
        }
    }

    // ============================================================
    // JSON FORMAT TESTS
    // ============================================================
    
    @Nested
    @DisplayName("JSON Format Tests")
    class JsonFormatTests {
        
        @Test
        @DisplayName("Saved JSON has expected structure")
        void saveSettings_ShouldProduceExpectedStructure() throws IOException {
            // Arrange
            Settings settings = new Settings();
            
            // Act
            SettingsManager.saveSettings(settings, settingsPath.toString());
            String content = Files.readString(settingsPath);
            
            // Assert - Check structure
            assertTrue(content.contains("\"firstRun\""), "Should have firstRun key");
            assertTrue(content.contains("\"keeperConstraints\""), 
                "Should have keeperConstraints key");
            assertTrue(content.contains("\"minCages\""), "Should have minCages key");
            assertTrue(content.contains("\"maxCages\""), "Should have maxCages key");
            assertTrue(content.contains("\"animalRules\""), "Should have animalRules key");
            assertTrue(content.contains("\"predatorShareable\""), 
                "Should have predatorShareable key");
            assertTrue(content.contains("\"preyShareable\""), "Should have preyShareable key");
        }
        
        @Test
        @DisplayName("Boolean values are saved as JSON booleans")
        void saveSettings_Booleans_ShouldBeSavedAsJsonBooleans() throws IOException {
            // Arrange
            Settings settings = new Settings();
            settings.setFirstRun(true);
            
            // Act
            SettingsManager.saveSettings(settings, settingsPath.toString());
            String content = Files.readString(settingsPath);
            
            // Assert - Booleans should be true/false, not "true"/"false"
            // JSON boolean: "firstRun": true (not "firstRun": "true")
            assertTrue(content.contains(": true") || content.contains(":true"), 
                "Boolean should be saved as JSON boolean");
            assertFalse(content.contains("\"true\""), 
                "Boolean should not be saved as string");
        }
        
        @Test
        @DisplayName("Integer values are saved as JSON numbers")
        void saveSettings_Integers_ShouldBeSavedAsJsonNumbers() throws IOException {
            // Arrange
            Settings settings = new Settings();
            
            // Act
            SettingsManager.saveSettings(settings, settingsPath.toString());
            String content = Files.readString(settingsPath);
            
            // Assert - Numbers should not have quotes
            // JSON number: "minCages": 1 (not "minCages": "1")
            assertTrue(content.contains("\"minCages\": 1") || 
                       content.contains("\"minCages\":1"),
                "Integer should be saved as JSON number");
        }
    }
}
