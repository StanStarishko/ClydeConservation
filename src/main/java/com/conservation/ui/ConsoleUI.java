package com.conservation.ui;

import com.conservation.config.SettingsManager;
import com.conservation.exception.ExceptionHandler;
import com.conservation.exception.PersistenceException;
import com.conservation.persistence.DataInitialiser;
import com.conservation.persistence.XMLPersistence;
import com.conservation.registry.Animals;
import com.conservation.registry.Keepers;
import com.conservation.registry.Cages;
import com.conservation.service.ConservationService;
import com.conservation.service.AllocationValidator;

import java.util.Scanner;

/**
 * Main entry point for the Clyde Conservation Management System console application.
 * Handles application initialisation, the main run loop, and graceful shutdown.
 * 
 * <p>Application lifecycle:
 * <ol>
 *   <li>Initialise settings and check firstRun flag</li>
 *   <li>Load test data (if firstRun) or load from XML persistence</li>
 *   <li>Run main menu loop until user exits</li>
 *   <li>Save data and perform graceful shutdown</li>
 * </ol>
 * 
 * <p>Usage: Run the {@link #main(String[])} method to start the application.
 * 
 * @author Clyde Conservation Development Team
 * @version 1.0
 */
public class ConsoleUI {

    // ============================================================
    // Constants
    // ============================================================
    
    /** Path to the configuration file */
    private static final String CONFIG_PATH = "config/settings.json";
    
    /** Path to the data directory */
    private static final String DATA_PATH = "data/";
    
    /** Application version */
    private static final String VERSION = "1.0.0";

    // ============================================================
    // Instance Variables
    // ============================================================
    
    private final Scanner scanner;
    private final InputValidator inputValidator;
    private final MenuHandler menuHandler;
    private final ConservationService conservationService;
    private final AllocationValidator allocationValidator;

    // ============================================================
    // Constructor
    // ============================================================
    
    /**
     * Creates a new ConsoleUI instance and initialises all dependencies.
     */
    public ConsoleUI() {
        this.scanner = new Scanner(System.in);
        this.inputValidator = new InputValidator(scanner);
        this.allocationValidator = new AllocationValidator();
        this.conservationService = new ConservationService(allocationValidator);
        this.menuHandler = new MenuHandler(inputValidator, conservationService);
    }

    // ============================================================
    // Main Entry Point
    // ============================================================
    
    /**
     * Main entry point for the application.
     * 
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        ConsoleUI application = new ConsoleUI();
        application.run();
    }

    // ============================================================
    // Application Lifecycle
    // ============================================================
    
    /**
     * Runs the main application lifecycle.
     * Initialises data, runs the menu loop, and performs cleanup.
     */
    public void run() {
        try {
            displayWelcome();
            initialiseApplication();
            runMainLoop();
            shutdown();
        } catch (Exception exception) {
            handleFatalError(exception);
        } finally {
            cleanup();
        }
    }
    
    /**
     * Displays the welcome message and application banner.
     */
    private void displayWelcome() {
        DisplayFormatter.printHeaderBar();
        DisplayFormatter.printCentred("CLYDE CONSERVATION");
        DisplayFormatter.printCentred("MANAGEMENT SYSTEM");
        DisplayFormatter.printCentred("Version " + VERSION);
        DisplayFormatter.printHeaderBar();
        DisplayFormatter.printBlankLine();
    }
    
    /**
     * Initialises the application by loading settings and data.
     */
    private void initialiseApplication() {
        DisplayFormatter.printInfo("Initialising system...");
        
        try {
            // Check if this is first run
            boolean isFirstRun = SettingsManager.isFirstRun();
            
            if (isFirstRun) {
                DisplayFormatter.printInfo("First run detected. Loading initial test data...");
                loadTestData();
                SettingsManager.setFirstRun(false);
                DisplayFormatter.printSuccess("Test data loaded successfully.");
            } else {
                DisplayFormatter.printInfo("Loading data from persistence...");
                loadDataFromPersistence();
                DisplayFormatter.printSuccess("Data loaded successfully.");
            }
            
            displaySystemStatus();
            
        } catch (Exception exception) {
            DisplayFormatter.printWarning(
                "Some data could not be loaded. Starting with available data."
            );
            ExceptionHandler.handle(exception);
        }
        
        DisplayFormatter.printBlankLine();
        inputValidator.waitForEnter();
    }
    
    /**
     * Loads initial test data using DataInitialiser.
     */
    private void loadTestData() {
        try {
            DataInitialiser.loadTestData();
            
            // Save the test data to XML files
            saveAllData();
            
        } catch (Exception exception) {
            DisplayFormatter.printError("Failed to load test data: " + exception.getMessage());
            throw new RuntimeException("Failed to initialise test data", exception);
        }
    }
    
    /**
     * Loads data from XML persistence files.
     */
    private void loadDataFromPersistence() {
        try {
            // Load animals
            var loadedAnimals = XMLPersistence.loadFromXML(DATA_PATH + "animals.xml");
            Animals.initializeFromPersistence(loadedAnimals);
            
            // Load keepers
            var loadedKeepers = XMLPersistence.loadFromXML(DATA_PATH + "keepers.xml");
            Keepers.initializeFromPersistence(loadedKeepers);
            
            // Load cages
            var loadedCages = XMLPersistence.loadFromXML(DATA_PATH + "cages.xml");
            Cages.initializeFromPersistence(loadedCages);
            
        } catch (PersistenceException exception) {
            DisplayFormatter.printWarning("Could not load some data files.");
            // Continue with empty registries - application can still function
        }
    }
    
    /**
     * Displays current system status (counts of entities).
     */
    private void displaySystemStatus() {
        DisplayFormatter.printBlankLine();
        DisplayFormatter.printInfo("=== System Status ===");
        DisplayFormatter.printLabelledValue("Animals", Animals.count());
        DisplayFormatter.printLabelledValue("Keepers", Keepers.count());
        DisplayFormatter.printLabelledValue("Cages", Cages.count());
    }
    
    /**
     * Runs the main menu loop until user exits.
     */
    private void runMainLoop() {
        menuHandler.handleMainMenu();
    }
    
    /**
     * Performs graceful shutdown, saving all data.
     */
    private void shutdown() {
        DisplayFormatter.printBlankLine();
        DisplayFormatter.printInfo("Saving data...");
        
        try {
            saveAllData();
            DisplayFormatter.printSuccess("All data saved successfully.");
        } catch (Exception exception) {
            DisplayFormatter.printError("Some data could not be saved: " + exception.getMessage());
            ExceptionHandler.handle(exception);
        }
    }
    
    /**
     * Saves all data to XML persistence files.
     */
    private void saveAllData() {
        try {
            // Ensure data directory exists
            java.nio.file.Files.createDirectories(java.nio.file.Path.of(DATA_PATH));
            
            // Save animals
            XMLPersistence.saveToXML(
                Animals.getAll(),
                DATA_PATH + "animals.xml",
                "animals"
            );
            
            // Save keepers
            XMLPersistence.saveToXML(
                Keepers.getAll(),
                DATA_PATH + "keepers.xml",
                "keepers"
            );
            
            // Save cages
            XMLPersistence.saveToXML(
                Cages.getAll(),
                DATA_PATH + "cages.xml",
                "cages"
            );
            
        } catch (PersistenceException exception) {
            throw new RuntimeException("Failed to save data", exception);
        } catch (java.io.IOException exception) {
            throw new RuntimeException("Failed to create data directory", exception);
        }
    }
    
    /**
     * Performs cleanup operations (closing resources).
     */
    private void cleanup() {
        if (scanner != null) {
            scanner.close();
        }
    }
    
    /**
     * Handles fatal errors that prevent the application from continuing.
     * 
     * @param exception the fatal exception
     */
    private void handleFatalError(Exception exception) {
        DisplayFormatter.printHeaderBar();
        DisplayFormatter.printError("A fatal error has occurred.");
        DisplayFormatter.printInfo("Error: " + exception.getMessage());
        DisplayFormatter.printBlankLine();
        DisplayFormatter.printInfo("The application will now exit.");
        DisplayFormatter.printInfo("Please contact support if this problem persists.");
        DisplayFormatter.printHeaderBar();
        
        // Log the full stack trace for debugging
        exception.printStackTrace();
    }

    // ============================================================
    // Utility Methods
    // ============================================================
    
    /**
     * Gets the current version of the application.
     * 
     * @return the version string
     */
    public static String getVersion() {
        return VERSION;
    }
    
    /**
     * Gets the configuration file path.
     * 
     * @return the config path
     */
    public static String getConfigPath() {
        return CONFIG_PATH;
    }
    
    /**
     * Gets the data directory path.
     * 
     * @return the data path
     */
    public static String getDataPath() {
        return DATA_PATH;
    }
}
