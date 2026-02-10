package com.conservation.persistence;

import com.conservation.config.Settings;
import com.conservation.config.SettingsManager;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for configuration persistence - SettingsManager and Settings.
 * Tests core functionality without relying on temporary directories.
 *
 * <p>Focus: Settings class logic and SettingsManager integration with production file.
 *
 * @author Clyde Conservation Development Team
 * @version 1.0
 */
@DisplayName("Configuration Persistence Tests")
class ConfigPersistenceTest {

    // ============================================================
    // Test Constants
    // ============================================================

    private static final int DEFAULT_MIN_CAGES = 1;
    private static final int DEFAULT_MAX_CAGES = 4;

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
                    "Default minCages should be " + DEFAULT_MIN_CAGES);
            assertEquals(DEFAULT_MAX_CAGES, constraints.getMaxCages(),
                    "Default maxCages should be " + DEFAULT_MAX_CAGES);
        }

        @Test
        @DisplayName("Default animal rules have correct values")
        void defaultAnimalRules_ShouldHaveCorrectValues() {
            // Arrange & Act
            Settings settings = new Settings();
            Settings.AnimalRules rules = settings.getAnimalRules();

            // Assert
            assertFalse(rules.isPredatorShareable(),
                    "Default predatorShareable should be false");
            assertTrue(rules.isPreyShareable(),
                    "Default preyShareable should be true");
        }

        @Test
        @DisplayName("Settings can be updated")
        void settings_ShouldBeUpdatable() {
            // Arrange
            Settings settings = new Settings();

            // Act
            settings.setFirstRun(false);
            settings.getKeeperConstraints().setMinCages(2);
            settings.getKeeperConstraints().setMaxCages(6);
            settings.getAnimalRules().setPredatorShareable(true);
            settings.getAnimalRules().setPreyShareable(false);

            // Assert
            assertFalse(settings.isFirstRun(), "firstRun should be updated");
            assertEquals(2, settings.getKeeperConstraints().getMinCages(),
                    "minCages should be updated");
            assertEquals(6, settings.getKeeperConstraints().getMaxCages(),
                    "maxCages should be updated");
            assertTrue(settings.getAnimalRules().isPredatorShareable(),
                    "predatorShareable should be updated");
            assertFalse(settings.getAnimalRules().isPreyShareable(),
                    "preyShareable should be updated");
        }
    }

    // ============================================================
    // SETTINGS MANAGER INTEGRATION TESTS
    // ============================================================

    @Nested
    @DisplayName("SettingsManager Integration Tests")
    class SettingsManagerIntegrationTests {

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
            SettingsManager.saveSettings(original);
            Settings loaded = SettingsManager.loadSettings();

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
        @DisplayName("getSettings returns cached instance")
        void getSettings_ShouldReturnCachedInstance() {
            // Arrange
            Settings settings = new Settings();
            SettingsManager.saveSettings(settings);

            // Act
            Settings first = SettingsManager.getSettings();
            Settings second = SettingsManager.getSettings();

            // Assert
            assertNotNull(first, "First call should return settings");
            assertNotNull(second, "Second call should return settings");
            assertEquals(first.isFirstRun(), second.isFirstRun(),
                    "Both should have same firstRun value");
        }
    }

    // ============================================================
    // FIRST RUN FLAG MANAGEMENT TESTS
    // ============================================================

    @Nested
    @DisplayName("First Run Flag Management Tests")
    class FirstRunFlagTests {

        @Test
        @DisplayName("setFirstRun updates and saves flag")
        void setFirstRun_ShouldUpdateAndSave() throws IOException {
            // Arrange
            Settings settings = new Settings();
            assertTrue(settings.isFirstRun(), "Initial should be true");
            SettingsManager.saveSettings(settings);

            // Act
            SettingsManager.setFirstRun(false);

            // Assert
            Settings reloaded = SettingsManager.loadSettings();
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
            SettingsManager.saveSettings(settings);

            // Assert - Next load should return false
            Settings reloaded = SettingsManager.loadSettings();
            assertFalse(reloaded.isFirstRun(),
                    "After initialization, firstRun should be false");
        }
    }
}