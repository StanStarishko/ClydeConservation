package com.conservation.registry;

import com.conservation.interfaces.IRegistry;
import com.conservation.model.Animal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Registry class for managing all animals in the conservation system.
 * 
 * Provides in-memory storage using HashMap for O(1) lookups by animal ID.
 *
 * Uses static storage - all instances share the same animal collection.
 * This is intentional for this application where we need a single global registry.
 */
public class Animals {
    
    private static final Map<Integer, Animal> allAnimals = new HashMap<>();
    private static int lastAnimalId = 0;
    
    /**
     * Generates the next available animal ID.
     * 
     * Uses auto-increment counter starting from 1.
     * Counter is restored from persistence on application startup.
     * 
     * @return next available animal ID
     */
    private static int generateNextAnimalId() {
        return ++lastAnimalId;
    }
    
    /**
     * Adds a new animal to the registry.
     * 
     * Automatically generates and assigns a unique ID to the animal.
     * 
     * @param animal the animal to add
     * @throws IllegalArgumentException if animal is null
     */
    public static void add(Animal animal) {
        if (animal == null) {
            throw new IllegalArgumentException("Cannot add null animal");
        }
        
        // Generate new ID and assign to animal
        int newId = generateNextAnimalId();
        animal.setAnimalId(newId);
        
        allAnimals.put(newId, animal);
        System.out.println("Animal added: " + animal.getName() + " (ID: " + newId + ")");
    }
    
    /**
     * Finds an animal by its unique ID.
     * 
     * @param animalId the unique identifier of the animal
     * @return the animal with the given ID, or null if not found
     */
    public static Animal findById(int animalId) {
        return allAnimals.get(animalId);
    }
    
    /**
     * Returns all animals currently in the registry.
     * 
     * @return collection of all animals (defensive copy)
     */
    public static Collection<Animal> getAll() {
        return new ArrayList<>(allAnimals.values());
    }
    
    /**
     * Removes an animal from the registry by its ID.
     * 
     * Note: ID is never reused even after deletion (follows database best practice).
     * 
     * @param animalId the unique identifier of the animal to remove
     * @return true if animal was removed, false if animal was not found
     */
    public static boolean remove(int animalId) {
        Animal removed = allAnimals.remove(animalId);
        if (removed != null) {
            System.out.println("Animal removed: " + removed.getName() + " (ID: " + animalId + ")");
            return true;
        }
        return false;
    }
    
    /**
     * Returns the total number of animals in the registry.
     * 
     * @return count of animals
     */
    public static int count() {
        return allAnimals.size();
    }
    
    /**
     * Removes all animals from the registry.
     * 
     * Warning: This does NOT reset the ID counter.
     * Used primarily for testing purposes.
     */
    public static void clear() {
        allAnimals.clear();
        System.out.println("All animals cleared from registry");
    }
    
    /**
     * Initialises the registry from persisted data.
     * 
     * Called on application startup to load animals from XML file.
     * Restores the ID counter to the highest ID found in the loaded data.
     * 
     * @param animals collection of animals loaded from persistence
     */
    public static void initializeFromPersistence(Collection<Animal> animals) {
        allAnimals.clear();
        
        for (Animal animal : animals) {
            allAnimals.put(animal.getAnimalId(), animal);
            
            // Update counter to highest ID found
            if (animal.getAnimalId() > lastAnimalId) {
                lastAnimalId = animal.getAnimalId();
            }
        }
        
        System.out.println("Animals registry initialised: " + allAnimals.size() + 
                          " animals loaded, next ID will be " + (lastAnimalId + 1));
    }
    
    /**
     * Finds an animal by its name.
     * 
     * Performs case-insensitive search.
     * If multiple animals have the same name, returns the first found.
     * 
     * @param name the name to search for
     * @return the animal with the given name, or null if not found
     */
    public static Animal findByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        
        return allAnimals.values().stream()
                .filter(animal -> animal.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Finds all animals of a specific category.
     * 
     * @param category the category to filter by (PREDATOR or PREY)
     * @return collection of animals matching the category
     */
    public static Collection<Animal> findByCategory(Animal.Category category) {
        return allAnimals.values().stream()
                .filter(animal -> animal.getCategory() == category)
                .toList();
    }
    
    /**
     * Finds all animals of a specific type.
     * 
     * @param type the animal type (e.g., "Tiger", "Zebra")
     * @return collection of animals matching the type
     */
    public static Collection<Animal> findByType(String type) {
        if (type == null || type.trim().isEmpty()) {
            return java.util.Collections.emptyList();
        }
        
        return allAnimals.values().stream()
                .filter(animal -> animal.getType().equalsIgnoreCase(type))
                .toList();
    }
    
    /**
     * Gets the current highest animal ID in use.
     * 
     * Useful for persistence layer to know the ID counter state.
     * 
     * @return highest animal ID currently in use
     */
    public static int getLastAnimalId() {
        return lastAnimalId;
    }
    
    /**
     * Checks if an animal with the given ID exists.
     * 
     * @param animalId the ID to check
     * @return true if animal exists, false otherwise
     */
    public static boolean exists(int animalId) {
        return allAnimals.containsKey(animalId);
    }
}
