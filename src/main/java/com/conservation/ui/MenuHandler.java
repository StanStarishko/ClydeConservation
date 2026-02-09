package com.conservation.ui;

import com.conservation.exception.ExceptionHandler;
import com.conservation.exception.ValidationException;
import com.conservation.model.Animal;
import com.conservation.model.Animal.Category;
import com.conservation.model.Animal.Sex;
import com.conservation.model.Keeper;
import com.conservation.model.HeadKeeper;
import com.conservation.model.AssistantKeeper;
import com.conservation.model.Cage;
import com.conservation.registry.Animals;
import com.conservation.registry.Keepers;
import com.conservation.registry.Cages;
import com.conservation.service.ConservationService;

import java.time.LocalDate;
import java.util.Collection;

/**
 * Handles all menu operations and user interactions for the console interface.
 * Coordinates between user input, display formatting, and business services.
 * 
 * <p>Menu structure:
 * <ul>
 *   <li>Main Menu - entry point with role selection</li>
 *   <li>Administrator Menu - add animals, keepers, cages</li>
 *   <li>Head Keeper Menu - allocate animals/keepers to cages</li>
 *   <li>Reports Menu - view data and statistics</li>
 * </ul>
 * 
 * @author Clyde Conservation Development Team
 * @version 1.0
 */
public class MenuHandler {

    // ============================================================
    // Menu Option Arrays
    // ============================================================
    
    private static final String[] MAIN_MENU_OPTIONS = {
        "Administrator Menu",
        "Head Keeper Menu",
        "View Reports",
        "Exit"
    };
    
    private static final String[] ADMIN_MENU_OPTIONS = {
        "Add Animal",
        "Add Keeper",
        "Add Cage",
        "View All Animals",
        "View All Keepers",
        "View All Cages",
        "Back to Main Menu"
    };
    
    private static final String[] HEAD_KEEPER_MENU_OPTIONS = {
        "Allocate Animal to Cage",
        "Allocate Keeper to Cage",
        "Remove Animal from Cage",
        "View Cage Details",
        "View Keeper Assignments",
        "Back to Main Menu"
    };
    
    private static final String[] REPORTS_MENU_OPTIONS = {
        "View All Animals",
        "View All Keepers",
        "View All Cages",
        "View Animals by Category",
        "View Cage Occupancy",
        "Back to Main Menu"
    };
    
    private static final String[] ANIMAL_TYPES = {
        "Tiger", "Zebra", "Rabbit", "Guinea Pig", "Ape", "Marmoset Monkey",
        "Eagle", "Vulture", "Owl", "Emu", "Penguin",
        "Bearded Dragon", "Lizard", "Chameleon", "Other"
    };
    
    private static final String[] CATEGORY_OPTIONS = {"Predator", "Prey"};
    
    private static final String[] SEX_OPTIONS = {"Male", "Female"};
    
    private static final String[] POSITION_OPTIONS = {"Head Keeper", "Assistant Keeper"};
    
    private static final String[] CAGE_SIZE_OPTIONS = {
        "Small (Capacity: 1 animal)",
        "Medium (Capacity: 5 animals)",
        "Large (Capacity: 10 animals)"
    };

    // ============================================================
    // Instance Variables
    // ============================================================
    
    private final InputValidator inputValidator;
    private final ConservationService conservationService;
    private boolean running;

    // ============================================================
    // Constructor
    // ============================================================
    
    /**
     * Creates a new MenuHandler with the specified input validator and service.
     * 
     * @param inputValidator     the input validator for user input
     * @param conservationService the business service for operations
     */
    public MenuHandler(InputValidator inputValidator, ConservationService conservationService) {
        this.inputValidator = inputValidator;
        this.conservationService = conservationService;
        this.running = true;
    }

    // ============================================================
    // Main Menu
    // ============================================================
    
    /**
     * Handles the main menu loop.
     * Returns when user selects Exit.
     */
    public void handleMainMenu() {
        while (running) {
            DisplayFormatter.displayMenu("CLYDE CONSERVATION MANAGEMENT SYSTEM", MAIN_MENU_OPTIONS);
            
            int choice = inputValidator.validateMenuChoice(MAIN_MENU_OPTIONS.length);
            
            switch (choice) {
                case 1 -> handleAdministratorMenu();
                case 2 -> handleHeadKeeperMenu();
                case 3 -> handleReportsMenu();
                case 4 -> handleExit();
                default -> DisplayFormatter.printError("Invalid selection. Please try again.");
            }
        }
    }
    
    /**
     * Handles the exit confirmation and shutdown.
     */
    private void handleExit() {
        boolean confirmed = inputValidator.validateConfirmation("Are you sure you want to exit?");
        
        if (confirmed) {
            DisplayFormatter.printBlankLine();
            DisplayFormatter.printInfo("All data has been saved.");
            DisplayFormatter.printInfo("Thank you for using Clyde Conservation System!");
            DisplayFormatter.printHeaderBar();
            running = false;
        }
    }
    
    /**
     * Checks if the menu handler is still running.
     * 
     * @return true if running, false if user has exited
     */
    public boolean isRunning() {
        return running;
    }

    // ============================================================
    // Administrator Menu
    // ============================================================
    
    /**
     * Handles the Administrator menu loop.
     */
    public void handleAdministratorMenu() {
        boolean inAdminMenu = true;
        
        while (inAdminMenu) {
            DisplayFormatter.displayMenu("ADMINISTRATOR MENU", ADMIN_MENU_OPTIONS);
            
            int choice = inputValidator.validateMenuChoice(ADMIN_MENU_OPTIONS.length);
            
            switch (choice) {
                case 1 -> handleAddAnimal();
                case 2 -> handleAddKeeper();
                case 3 -> handleAddCage();
                case 4 -> handleViewAllAnimals();
                case 5 -> handleViewAllKeepers();
                case 6 -> handleViewAllCages();
                case 7, 0 -> inAdminMenu = false;
                default -> DisplayFormatter.printError("Invalid selection. Please try again.");
            }
        }
    }
    
    /**
     * Handles adding a new animal.
     */
    private void handleAddAnimal() {
        DisplayFormatter.printTitle("ADD ANIMAL");
        DisplayFormatter.printBlankLine();
        DisplayFormatter.printInfo("Animal ID: [Auto-generated]");
        DisplayFormatter.printBlankLine();
        
        // Get animal name
        String name = inputValidator.validateNameInput("Animal Name");
        if (name == null) return;
        
        // Get animal type
        int typeChoice = inputValidator.validateChoice("Select Animal Type:", ANIMAL_TYPES);
        if (typeChoice == -1) return;
        
        String type;
        if (typeChoice == ANIMAL_TYPES.length) { // "Other" option
            type = inputValidator.validateNonEmptyInput("Enter Animal Type");
            if (type == null) return;
        } else {
            type = ANIMAL_TYPES[typeChoice - 1];
        }
        
        // Get category
        int categoryChoice = inputValidator.validateChoice("Select Category:", CATEGORY_OPTIONS);
        if (categoryChoice == -1) return;
        Category category = (categoryChoice == 1) ? Category.PREDATOR : Category.PREY;
        
        // Get date of birth
        LocalDate dateOfBirth = inputValidator.validatePastOrPresentDate("Date of Birth");
        if (dateOfBirth == null) return;
        
        // Get date of acquisition
        LocalDate dateOfAcquisition = inputValidator.validateDateAfter(
            "Date of Acquisition", dateOfBirth, "date of birth"
        );
        if (dateOfAcquisition == null) return;
        
        // Get sex
        int sexChoice = inputValidator.validateChoice("Select Sex:", SEX_OPTIONS);
        if (sexChoice == -1) return;
        Sex sex = (sexChoice == 1) ? Sex.MALE : Sex.FEMALE;
        
        // Confirm and create
        DisplayFormatter.printBlankLine();
        DisplayFormatter.printInfo("=== Confirm Animal Details ===");
        DisplayFormatter.printLabelledValue("Name", name);
        DisplayFormatter.printLabelledValue("Type", type);
        DisplayFormatter.printLabelledValue("Category", category.toString());
        DisplayFormatter.printLabelledValue("Sex", sex.toString());
        DisplayFormatter.printBlankLine();
        
        boolean confirmed = inputValidator.validateConfirmation("Confirm details?");
        
        if (confirmed) {
            try {
                Animal animal = new Animal(name, type, category, dateOfBirth, dateOfAcquisition, sex);
                Animals.add(animal);
                
                DisplayFormatter.printSuccess(
                    "Animal '" + name + "' added successfully with ID: " + animal.getAnimalId()
                );
            } catch (IllegalArgumentException | ValidationException exception) {
                ExceptionHandler.handle(exception);
            }
        } else {
            DisplayFormatter.printInfo("Operation cancelled.");
        }
        
        inputValidator.waitForEnter();
    }
    
    /**
     * Handles adding a new keeper.
     */
    private void handleAddKeeper() {
        DisplayFormatter.printTitle("ADD KEEPER");
        DisplayFormatter.printBlankLine();
        DisplayFormatter.printInfo("Keeper ID: [Auto-generated]");
        DisplayFormatter.printBlankLine();
        
        // Get first name
        String firstName = inputValidator.validateNameInput("First Name");
        if (firstName == null) return;
        
        // Get surname
        String surname = inputValidator.validateNameInput("Surname");
        if (surname == null) return;
        
        // Get address
        String address = inputValidator.validateAddressInput("Address");
        if (address == null) return;
        
        // Get contact number
        String contactNumber = inputValidator.validatePhoneInput("Contact Number");
        if (contactNumber == null) return;
        
        // Get position
        int positionChoice = inputValidator.validateChoice("Select Position:", POSITION_OPTIONS);
        if (positionChoice == -1) return;
        
        // Confirm and create
        DisplayFormatter.printBlankLine();
        DisplayFormatter.printInfo("=== Confirm Keeper Details ===");
        DisplayFormatter.printLabelledValue("Name", firstName + " " + surname);
        DisplayFormatter.printLabelledValue("Position", POSITION_OPTIONS[positionChoice - 1]);
        DisplayFormatter.printLabelledValue("Address", address);
        DisplayFormatter.printLabelledValue("Contact", contactNumber);
        DisplayFormatter.printBlankLine();
        
        boolean confirmed = inputValidator.validateConfirmation("Confirm details?");
        
        if (confirmed) {
            try {
                Keeper keeper;
                if (positionChoice == 1) {
                    keeper = new HeadKeeper(firstName, surname, address, contactNumber);
                } else {
                    keeper = new AssistantKeeper(firstName, surname, address, contactNumber);
                }
                Keepers.add(keeper);
                
                DisplayFormatter.printSuccess(
                    "Keeper '" + firstName + " " + surname + "' added successfully with ID: " + 
                    keeper.getKeeperId()
                );
            } catch (IllegalArgumentException | ValidationException exception) {
                ExceptionHandler.handle(exception);
            }
        } else {
            DisplayFormatter.printInfo("Operation cancelled.");
        }
        
        inputValidator.waitForEnter();
    }
    
    /**
     * Handles adding a new cage.
     */
    private void handleAddCage() {
        DisplayFormatter.printTitle("ADD CAGE");
        DisplayFormatter.printBlankLine();
        DisplayFormatter.printInfo("Cage ID: [Auto-generated]");
        DisplayFormatter.printBlankLine();
        
        // Get cage number
        String cageNumber = inputValidator.validateCageNumberInput("Cage Number");
        if (cageNumber == null) return;
        
        // Get description
        String description = inputValidator.validateNonEmptyInput("Description");
        if (description == null) return;
        
        // Get size/capacity
        int sizeChoice = inputValidator.validateChoice("Select Cage Size:", CAGE_SIZE_OPTIONS);
        if (sizeChoice == -1) return;
        
        int capacity = switch (sizeChoice) {
            case 1 -> 1;   // Small
            case 2 -> 5;   // Medium
            case 3 -> 10;  // Large
            default -> 1;
        };
        
        // Confirm and create
        DisplayFormatter.printBlankLine();
        DisplayFormatter.printInfo("=== Confirm Cage Details ===");
        DisplayFormatter.printLabelledValue("Cage Number", cageNumber);
        DisplayFormatter.printLabelledValue("Description", description);
        DisplayFormatter.printLabelledValue("Capacity", capacity + " animal(s)");
        DisplayFormatter.printBlankLine();
        
        boolean confirmed = inputValidator.validateConfirmation("Confirm details?");
        
        if (confirmed) {
            try {
                Cage cage = new Cage(cageNumber, description, capacity);
                Cages.add(cage);
                
                DisplayFormatter.printSuccess(
                    "Cage '" + cageNumber + "' added successfully with ID: " + cage.getCageId()
                );
            } catch (IllegalArgumentException exception) {
                ExceptionHandler.handle(exception);
            } catch (ValidationException e) {
                throw new RuntimeException(e);
            }
        } else {
            DisplayFormatter.printInfo("Operation cancelled.");
        }
        
        inputValidator.waitForEnter();
    }
    
    /**
     * Handles viewing all animals.
     */
    private void handleViewAllAnimals() {
        DisplayFormatter.printTitle("VIEW ALL ANIMALS");
        DisplayFormatter.displayAnimalTable(Animals.getAll());
        inputValidator.waitForEnter();
    }
    
    /**
     * Handles viewing all keepers.
     */
    private void handleViewAllKeepers() {
        DisplayFormatter.printTitle("VIEW ALL KEEPERS");
        DisplayFormatter.displayKeeperTable(Keepers.getAll());
        inputValidator.waitForEnter();
    }
    
    /**
     * Handles viewing all cages.
     */
    private void handleViewAllCages() {
        DisplayFormatter.printTitle("VIEW ALL CAGES");
        DisplayFormatter.displayCageTable(Cages.getAll());
        inputValidator.waitForEnter();
    }

    // ============================================================
    // Head Keeper Menu
    // ============================================================
    
    /**
     * Handles the Head Keeper menu loop.
     */
    public void handleHeadKeeperMenu() {
        boolean inHeadKeeperMenu = true;
        
        while (inHeadKeeperMenu) {
            DisplayFormatter.displayMenu("HEAD KEEPER MENU", HEAD_KEEPER_MENU_OPTIONS);
            
            int choice = inputValidator.validateMenuChoice(HEAD_KEEPER_MENU_OPTIONS.length);
            
            switch (choice) {
                case 1 -> handleAllocateAnimalToCage();
                case 2 -> handleAllocateKeeperToCage();
                case 3 -> handleRemoveAnimalFromCage();
                case 4 -> handleViewCageDetails();
                case 5 -> handleViewKeeperAssignments();
                case 6, 0 -> inHeadKeeperMenu = false;
                default -> DisplayFormatter.printError("Invalid selection. Please try again.");
            }
        }
    }
    
    /**
     * Handles allocating an animal to a cage.
     */
    private void handleAllocateAnimalToCage() {
        DisplayFormatter.printTitle("ALLOCATE ANIMAL TO CAGE");
        
        // Show available animals
        Collection<Animal> availableAnimals = conservationService.getAvailableAnimals();
        if (availableAnimals.isEmpty()) {
            DisplayFormatter.printInfo("No animals available for allocation.");
            inputValidator.waitForEnter();
            return;
        }
        
        DisplayFormatter.printInfo("Available Animals (not yet allocated):");
        DisplayFormatter.displayAnimalTable(availableAnimals);
        
        // Get animal ID
        int animalId = inputValidator.validateIdInput("Enter Animal ID to allocate");
        if (animalId == -1) return;
        
        Animal animal = Animals.findById(animalId);
        if (animal == null) {
            DisplayFormatter.printError("Animal not found with ID: " + animalId);
            inputValidator.waitForEnter();
            return;
        }
        
        // Show available cages
        DisplayFormatter.printBlankLine();
        DisplayFormatter.printInfo("Selected Animal: " + animal.getName() + 
            " (" + animal.getType() + ", " + animal.getCategory() + ")");
        DisplayFormatter.printBlankLine();
        
        Collection<Cage> availableCages = conservationService.getAvailableCages();
        if (availableCages.isEmpty()) {
            DisplayFormatter.printInfo("No cages available.");
            inputValidator.waitForEnter();
            return;
        }
        
        DisplayFormatter.printInfo("Available Cages:");
        DisplayFormatter.displayCageTable(availableCages);
        
        // Get cage ID
        int cageId = inputValidator.validateIdInput("Enter Cage ID");
        if (cageId == -1) return;
        
        // Attempt allocation
        try {
            conservationService.allocateAnimalToCage(animalId, cageId);
            DisplayFormatter.printSuccess(
                "Animal '" + animal.getName() + "' successfully allocated to cage " + cageId + "."
            );
        } catch (ValidationException exception) {
            DisplayFormatter.printError(exception.getMessage());
        } catch (Exception exception) {
            ExceptionHandler.handle(exception);
        }
        
        inputValidator.waitForEnter();
    }
    
    /**
     * Handles allocating a keeper to a cage.
     */
    private void handleAllocateKeeperToCage() {
        DisplayFormatter.printTitle("ALLOCATE KEEPER TO CAGE");
        
        // Show available keepers
        Collection<Keeper> availableKeepers = conservationService.getAvailableKeepers();
        if (availableKeepers.isEmpty()) {
            DisplayFormatter.printInfo("No keepers available for allocation.");
            inputValidator.waitForEnter();
            return;
        }
        
        DisplayFormatter.printInfo("Available Keepers:");
        DisplayFormatter.displayKeeperTable(availableKeepers);
        
        // Get keeper ID
        int keeperId = inputValidator.validateIdInput("Enter Keeper ID");
        if (keeperId == -1) return;
        
        Keeper keeper = Keepers.findById(keeperId);
        if (keeper == null) {
            DisplayFormatter.printError("Keeper not found with ID: " + keeperId);
            inputValidator.waitForEnter();
            return;
        }
        
        // Show all cages
        DisplayFormatter.printBlankLine();
        DisplayFormatter.printInfo("Selected Keeper: " + keeper.getFirstName() + " " + keeper.getSurname());
        DisplayFormatter.printInfo("Current Assignments: " + keeper.getAllocatedCageIds().size() + "/4 cages");
        DisplayFormatter.printBlankLine();
        
        DisplayFormatter.printInfo("All Cages:");
        DisplayFormatter.displayCageTable(Cages.getAll());
        
        // Get cage ID
        int cageId = inputValidator.validateIdInput("Enter Cage ID");
        if (cageId == -1) return;
        
        // Attempt allocation
        try {
            conservationService.allocateKeeperToCage(keeperId, cageId);
            DisplayFormatter.printSuccess(
                "Keeper '" + keeper.getFirstName() + " " + keeper.getSurname() + 
                "' successfully assigned to cage " + cageId + "."
            );
        } catch (ValidationException exception) {
            DisplayFormatter.printError(exception.getMessage());
        } catch (Exception exception) {
            ExceptionHandler.handle(exception);
        }
        
        inputValidator.waitForEnter();
    }
    
    /**
     * Handles removing an animal from a cage.
     */
    private void handleRemoveAnimalFromCage() {
        DisplayFormatter.printTitle("REMOVE ANIMAL FROM CAGE");
        
        // Show all cages with animals
        DisplayFormatter.printInfo("Cages with Animals:");
        DisplayFormatter.displayCageTable(Cages.getAll());
        
        // Get cage ID
        int cageId = inputValidator.validateIdInput("Enter Cage ID");
        if (cageId == -1) return;
        
        Cage cage = Cages.findById(cageId);
        if (cage == null) {
            DisplayFormatter.printError("Cage not found with ID: " + cageId);
            inputValidator.waitForEnter();
            return;
        }
        
        if (cage.isEmpty()) {
            DisplayFormatter.printInfo("This cage has no animals.");
            inputValidator.waitForEnter();
            return;
        }
        
        // Show animals in cage
        DisplayFormatter.printBlankLine();
        DisplayFormatter.printInfo("Animals in Cage " + cage.getCageNumber() + ":");
        for (Integer animalId : cage.getCurrentAnimalIds()) {
            Animal animal = Animals.findById(animalId);
            if (animal != null) {
                System.out.printf("  ID: %d - %s (%s)%n", 
                    animal.getAnimalId(), animal.getName(), animal.getType());
            }
        }
        
        // Get animal ID to remove
        int animalId = inputValidator.validateIdInput("Enter Animal ID to remove");
        if (animalId == -1) return;
        
        // Confirm and remove
        boolean confirmed = inputValidator.validateConfirmation(
            "Remove animal " + animalId + " from cage " + cageId + "?"
        );
        
        if (confirmed) {
            try {
                conservationService.removeAnimalFromCage(animalId, cageId);
                DisplayFormatter.printSuccess("Animal successfully removed from cage.");
            } catch (Exception exception) {
                ExceptionHandler.handle(exception);
            }
        } else {
            DisplayFormatter.printInfo("Operation cancelled.");
        }
        
        inputValidator.waitForEnter();
    }
    
    /**
     * Handles viewing detailed cage information.
     */
    private void handleViewCageDetails() {
        DisplayFormatter.printTitle("VIEW CAGE DETAILS");
        
        // Show all cages
        DisplayFormatter.displayCageTable(Cages.getAll());
        
        // Get cage ID
        int cageId = inputValidator.validateIdInput("Enter Cage ID for details");
        if (cageId == -1) return;
        
        Cage cage = Cages.findById(cageId);
        if (cage == null) {
            DisplayFormatter.printError("Cage not found with ID: " + cageId);
        } else {
            DisplayFormatter.printTitle("CAGE DETAILS");
            DisplayFormatter.displayCage(cage);
            
            // Show animals in this cage
            if (!cage.isEmpty()) {
                DisplayFormatter.printBlankLine();
                DisplayFormatter.printInfo("Current Animals:");
                for (Integer animalId : cage.getCurrentAnimalIds()) {
                    Animal animal = Animals.findById(animalId);
                    if (animal != null) {
                        System.out.printf("  - %d: %s (%s, %s)%n", 
                            animal.getAnimalId(), animal.getName(), 
                            animal.getType(), animal.getCategory());
                    }
                }
            }
            
            // Show assigned keeper
            Integer keeperId = cage.getAssignedKeeperId();
            if (keeperId != null) {
                Keeper keeper = Keepers.findById(keeperId);
                if (keeper != null) {
                    DisplayFormatter.printBlankLine();
                    DisplayFormatter.printInfo("Assigned Keeper:");
                    System.out.printf("  - %d: %s %s (%s)%n",
                        keeper.getKeeperId(), keeper.getFirstName(), 
                        keeper.getSurname(), keeper.getPosition());
                }
            }
        }
        
        inputValidator.waitForEnter();
    }
    
    /**
     * Handles viewing keeper assignments.
     */
    private void handleViewKeeperAssignments() {
        DisplayFormatter.printTitle("VIEW KEEPER ASSIGNMENTS");
        
        Collection<Keeper> allKeepers = Keepers.getAll();
        if (allKeepers.isEmpty()) {
            DisplayFormatter.printInfo("No keepers found.");
            inputValidator.waitForEnter();
            return;
        }
        
        for (Keeper keeper : allKeepers) {
            DisplayFormatter.printBlankLine();
            DisplayFormatter.printInfo("=== " + keeper.getFirstName() + " " + keeper.getSurname() + 
                " (" + keeper.getPosition() + ") ===");
            
            var allocatedCages = keeper.getAllocatedCageIds();
            if (allocatedCages.isEmpty()) {
                DisplayFormatter.printInfo("  No cages assigned.");
            } else {
                DisplayFormatter.printInfo("  Assigned Cages (" + allocatedCages.size() + "/4):");
                for (Integer cageId : allocatedCages) {
                    Cage cage = Cages.findById(cageId);
                    if (cage != null) {
                        System.out.printf("    - Cage %d: %s (%s)%n",
                            cage.getCageId(), cage.getCageNumber(), cage.getOccupancyInfo());
                    }
                }
            }
        }
        
        inputValidator.waitForEnter();
    }

    // ============================================================
    // Reports Menu
    // ============================================================
    
    /**
     * Handles the Reports menu loop.
     */
    public void handleReportsMenu() {
        boolean inReportsMenu = true;
        
        while (inReportsMenu) {
            DisplayFormatter.displayMenu("VIEW REPORTS", REPORTS_MENU_OPTIONS);
            
            int choice = inputValidator.validateMenuChoice(REPORTS_MENU_OPTIONS.length);
            
            switch (choice) {
                case 1 -> handleViewAllAnimals();
                case 2 -> handleViewAllKeepers();
                case 3 -> handleViewAllCages();
                case 4 -> handleViewAnimalsByCategory();
                case 5 -> handleViewCageOccupancy();
                case 6, 0 -> inReportsMenu = false;
                default -> DisplayFormatter.printError("Invalid selection. Please try again.");
            }
        }
    }
    
    /**
     * Handles viewing animals grouped by category.
     */
    private void handleViewAnimalsByCategory() {
        DisplayFormatter.printTitle("ANIMALS BY CATEGORY");
        DisplayFormatter.displayAnimalsByCategory(Animals.getAll());
        inputValidator.waitForEnter();
    }
    
    /**
     * Handles viewing cage occupancy summary.
     */
    private void handleViewCageOccupancy() {
        DisplayFormatter.printTitle("CAGE OCCUPANCY");
        DisplayFormatter.displayCageOccupancySummary(Cages.getAll());
        inputValidator.waitForEnter();
    }
}
