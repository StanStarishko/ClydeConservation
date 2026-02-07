package com.conservation.persistence;

import com.conservation.config.SettingsManager;
import com.conservation.exception.ValidationException;
import com.conservation.model.*;
import com.conservation.registry.Animals;
import com.conservation.registry.Cages;
import com.conservation.registry.Keepers;

import java.time.LocalDate;

/**
 * Initialises the conservation system with test data on first run.
 * 
 * Creates:
 * - 15 cages (5 large, 3 medium, 7 small)
 * - Sample animals (mammals, birds, reptiles)
 * - Sample keepers (head keepers and assistants)
 * 
 * Called automatically when settings.firstRun = true.
 * After loading, sets firstRun = false to prevent reloading on subsequent runs.
 */
public class DataInitialiser {
    
    /**
     * Private constructor to prevent instantiation.
     * This is a utility class with only static methods.
     */
    public DataInitialiser() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
    
    /**
     * Loads all test data into the system.
     * 
     * Order of operations:
     * 1. Load cages (so they exist when allocating animals)
     * 2. Load keepers (so they exist when allocating to cages)
     * 3. Load animals (can then be allocated to cages)
     * 4. Perform initial allocations (optional)
     * 5. Set firstRun = false
     */
    public static void loadTestData() {
        System.out.println("\n=== LOADING TEST DATA ===");
        
        try {
            // Step 1: Create cages
            loadCages();
            
            // Step 2: Create keepers
            loadKeepers();
            
            // Step 3: Create animals
            loadAnimals();
            
            // Step 4: Perform initial allocations (optional)
            // performInitialAllocations();
            
            // Step 5: Mark as initialized
            SettingsManager.setFirstRun(false);
            
            System.out.println("=== TEST DATA LOADED SUCCESSFULLY ===\n");
            
            // Print summary
            printDataSummary();
            
        } catch (Exception exception) {
            System.err.println("Error loading test data: " + exception.getMessage());
            exception.printStackTrace();
        }
    }
    
    /**
     * Creates 15 test cages (5 large, 3 medium, 7 small).
     * 
     * Cage capacities:
     * - Large: 10 animals
     * - Medium: 5 animals
     * - Small: 1 animal
     */
    private static void loadCages() {
        System.out.println("Creating cages...");
        
        int cageCount = 0;
        
        // Create 5 large cages (capacity: 10)
        for (int idx = 1; idx <= 5; idx++) {
            Cage cage = new Cage(
                "Large-0" + idx,
                "Large cage for multiple animals",
                10
            );
            Cages.add(cage);
            cageCount++;
        }
        
        // Create 3 medium cages (capacity: 5)
        for (int idx = 1; idx <= 3; idx++) {
            Cage cage = new Cage(
                "Medium-0" + idx,
                "Medium cage for small groups",
                5
            );
            Cages.add(cage);
            cageCount++;
        }
        
        // Create 7 small cages (capacity: 1)
        for (int idx = 1; idx <= 7; idx++) {
            Cage cage = new Cage(
                "Small-0" + idx,
                "Small cage for single animal",
                1
            );
            Cages.add(cage);
            cageCount++;
        }
        
        System.out.println("Created " + cageCount + " cages (5 large, 3 medium, 7 small)");
    }
    
    /**
     * Creates sample keepers (head keepers and assistants).
     * 
     * Creates:
     * - 2 head keepers
     * - 3 assistant keepers
     */
    private static void loadKeepers() {
        System.out.println("Creating keepers...");
        
        int keeperCount = 0;
        
        // Create head keepers
        HeadKeeper headKeeper1 = new HeadKeeper(
            "John",
            "Smith",
            "123 Main Street, Glasgow",
            "07123456789"
        );
        Keepers.add(headKeeper1);
        keeperCount++;
        
        HeadKeeper headKeeper2 = new HeadKeeper(
            "Sarah",
            "Johnson",
            "45 High Street, Glasgow",
            "07234567890"
        );
        Keepers.add(headKeeper2);
        keeperCount++;
        
        // Create assistant keepers
        AssistantKeeper assistant1 = new AssistantKeeper(
            "Michael",
            "Brown",
            "78 Queen Street, Glasgow",
            "07345678901"
        );
        Keepers.add(assistant1);
        keeperCount++;
        
        AssistantKeeper assistant2 = new AssistantKeeper(
            "Emma",
            "Davis",
            "12 King Street, Glasgow",
            "07456789012"
        );
        Keepers.add(assistant2);
        keeperCount++;
        
        AssistantKeeper assistant3 = new AssistantKeeper(
            "James",
            "Wilson",
            "90 Bridge Street, Glasgow",
            "07567890123"
        );
        Keepers.add(assistant3);
        keeperCount++;
        
        System.out.println("Created " + keeperCount + " keepers (2 head, 3 assistant)");
    }
    
    /**
     * Creates sample animals (mammals, birds, reptiles).
     * 
     * Creates diverse collection including:
     * - Predators (tigers, eagles, vultures)
     * - Prey (zebras, rabbits, guinea pigs, emus, penguins)
     */
    private static void loadAnimals() {
        System.out.println("Creating animals...");
        
        int animalCount = 0;
        
        // MAMMALS - Predators
        animalCount += createAnimal("Leo", "Tiger", Animal.Category.PREDATOR, 
            Animal.Sex.MALE, LocalDate.of(2019, 5, 15), LocalDate.of(2020, 1, 10));
        
        animalCount += createAnimal("Tigress", "Tiger", Animal.Category.PREDATOR,
            Animal.Sex.FEMALE, LocalDate.of(2020, 3, 22), LocalDate.of(2021, 2, 15));
        
        // MAMMALS - Prey
        animalCount += createAnimal("Marty", "Zebra", Animal.Category.PREY,
            Animal.Sex.MALE, LocalDate.of(2018, 7, 10), LocalDate.of(2019, 5, 20));
        
        animalCount += createAnimal("Stripe", "Zebra", Animal.Category.PREY,
            Animal.Sex.FEMALE, LocalDate.of(2019, 8, 5), LocalDate.of(2020, 6, 15));
        
        animalCount += createAnimal("Daisy", "Zebra", Animal.Category.PREY,
            Animal.Sex.FEMALE, LocalDate.of(2020, 4, 12), LocalDate.of(2021, 3, 8));
        
        animalCount += createAnimal("Bugs", "Rabbit", Animal.Category.PREY,
            Animal.Sex.MALE, LocalDate.of(2021, 1, 8), LocalDate.of(2021, 6, 20));
        
        animalCount += createAnimal("Flopsy", "Rabbit", Animal.Category.PREY,
            Animal.Sex.FEMALE, LocalDate.of(2021, 2, 14), LocalDate.of(2021, 7, 1));
        
        animalCount += createAnimal("Peter", "Rabbit", Animal.Category.PREY,
            Animal.Sex.MALE, LocalDate.of(2021, 3, 20), LocalDate.of(2021, 8, 15));
        
        animalCount += createAnimal("Ginger", "Guinea Pig", Animal.Category.PREY,
            Animal.Sex.FEMALE, LocalDate.of(2022, 1, 5), LocalDate.of(2022, 5, 10));
        
        animalCount += createAnimal("Squeaky", "Guinea Pig", Animal.Category.PREY,
            Animal.Sex.MALE, LocalDate.of(2022, 2, 18), LocalDate.of(2022, 6, 22));
        
        // BIRDS - Predators
        animalCount += createAnimal("Eagle-Eye", "Eagle", Animal.Category.PREDATOR,
            Animal.Sex.MALE, LocalDate.of(2017, 4, 30), LocalDate.of(2018, 9, 5));
        
        animalCount += createAnimal("Talon", "Vulture", Animal.Category.PREDATOR,
            Animal.Sex.MALE, LocalDate.of(2018, 6, 15), LocalDate.of(2019, 10, 20));
        
        animalCount += createAnimal("Hoot", "Owl", Animal.Category.PREDATOR,
            Animal.Sex.FEMALE, LocalDate.of(2019, 9, 25), LocalDate.of(2020, 11, 12));
        
        // BIRDS - Prey
        animalCount += createAnimal("Eddie", "Emu", Animal.Category.PREY,
            Animal.Sex.MALE, LocalDate.of(2020, 5, 10), LocalDate.of(2021, 4, 8));
        
        animalCount += createAnimal("Pippa", "Penguin", Animal.Category.PREY,
            Animal.Sex.FEMALE, LocalDate.of(2021, 7, 22), LocalDate.of(2022, 1, 15));
        
        animalCount += createAnimal("Percy", "Penguin", Animal.Category.PREY,
            Animal.Sex.MALE, LocalDate.of(2021, 8, 30), LocalDate.of(2022, 2, 20));
        
        // REPTILES - Prey (most common reptiles in zoos are prey)
        animalCount += createAnimal("Spike", "Bearded Dragon", Animal.Category.PREY,
            Animal.Sex.MALE, LocalDate.of(2020, 3, 15), LocalDate.of(2021, 1, 10));
        
        animalCount += createAnimal("Lizzy", "Lizard", Animal.Category.PREY,
            Animal.Sex.FEMALE, LocalDate.of(2021, 5, 20), LocalDate.of(2022, 3, 5));
        
        animalCount += createAnimal("Camo", "Chameleon", Animal.Category.PREY,
            Animal.Sex.MALE, LocalDate.of(2022, 4, 8), LocalDate.of(2023, 2, 14));
        
        System.out.println("Created " + animalCount + " animals (mammals, birds, reptiles)");
    }
    
    /**
     * Helper method to create and add an animal.
     * 
     * @param name animal's name
     * @param type animal's type/species
     * @param category PREDATOR or PREY
     * @param sex MALE or FEMALE
     * @param dateOfBirth animal's birth date
     * @param dateOfAcquisition date acquired by conservation
     * @return 1 if created successfully, 0 if failed
     */
    private static int createAnimal(String name, String type, Animal.Category category,
                                   Animal.Sex sex, LocalDate dateOfBirth, LocalDate dateOfAcquisition) {
        try {
            Animal animal = new Animal(
                name,
                type,
                category,
                dateOfBirth,
                dateOfAcquisition,
                sex
            );
            Animals.add(animal);
            return 1;
        } catch (Exception exception) {
            System.err.println("Failed to create animal " + name + ": " + exception.getMessage());
            return 0;
        }
    }
    
    /**
     * Performs initial allocations of animals to cages and keepers to cages.
     * 
     * This is optional and can be commented out to start with empty allocations.
     */
    private static void performInitialAllocations() {
        System.out.println("Performing initial allocations...");
        
        // Example allocations could be added here
        // For now, leaving system with entities created but not allocated
        
        System.out.println("Initial allocations complete (none performed in test data)");
    }
    
    /**
     * Prints summary of loaded data.
     */
    private static void printDataSummary() {
        System.out.println("\n=== DATA SUMMARY ===");
        System.out.println("Animals: " + Animals.count());
        System.out.println("  - Predators: " + Animals.findByCategory(Animal.Category.PREDATOR).size());
        System.out.println("  - Prey: " + Animals.findByCategory(Animal.Category.PREY).size());
        System.out.println("Keepers: " + Keepers.count());
        System.out.println("  - Head Keepers: " + Keepers.findByPosition(Keeper.Position.HEAD_KEEPER).size());
        System.out.println("  - Assistants: " + Keepers.findByPosition(Keeper.Position.ASSISTANT_KEEPER).size());
        System.out.println("Cages: " + Cages.count());
        System.out.println("  - Large (10): " + Cages.findByCapacity(10).size());
        System.out.println("  - Medium (5): " + Cages.findByCapacity(5).size());
        System.out.println("  - Small (1): " + Cages.findByCapacity(1).size());
        System.out.println("===================\n");
    }
    
    /**
     * Checks if test data needs to be loaded.
     * 
     * @return true if firstRun flag is set, false otherwise
     */
    public static boolean shouldLoadTestData() {
        return SettingsManager.isFirstRun();
    }
}
