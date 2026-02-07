package com.conservation.registry;

import com.conservation.interfaces.IRegistry;
import com.conservation.model.Keeper;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Registry class for managing all keepers in the conservation system.
 * 
 * Provides in-memory storage using HashMap for O(1) lookups by keeper ID.
 *
 * Stores both HeadKeeper and AssistantKeeper objects (polymorphism).
 * Uses static storage - all instances share the same keeper collection.
 */
public class Keepers {
    
    private static final Map<Integer, Keeper> allKeepers = new HashMap<>();
    private static int lastKeeperId = 0;
    
    /**
     * Generates the next available keeper ID.
     * 
     * Uses auto-increment counter starting from 1.
     * Counter is restored from persistence on application startup.
     * 
     * @return next available keeper ID
     */
    private static int generateNextKeeperId() {
        return ++lastKeeperId;
    }
    
    /**
     * Adds a new keeper to the registry.
     * 
     * Automatically generates and assigns a unique ID to the keeper.
     * Accepts both HeadKeeper and AssistantKeeper objects.
     * 
     * @param keeper the keeper to add (HeadKeeper or AssistantKeeper)
     * @throws IllegalArgumentException if keeper is null
     */
    public static void add(Keeper keeper) {
        if (keeper == null) {
            throw new IllegalArgumentException("Cannot add null keeper");
        }
        
        // Generate new ID and assign to keeper
        int newId = generateNextKeeperId();
        keeper.setKeeperId(newId);
        
        allKeepers.put(newId, keeper);
        System.out.println("Keeper added: " + keeper.getFullName() + 
                          " (ID: " + newId + ", Position: " + keeper.getPosition() + ")");
    }
    
    /**
     * Finds a keeper by their unique ID.
     * 
     * @param keeperId the unique identifier of the keeper
     * @return the keeper with the given ID, or null if not found
     */
    public static Keeper findById(int keeperId) {
        return allKeepers.get(keeperId);
    }
    
    /**
     * Returns all keepers currently in the registry.
     * 
     * @return collection of all keepers (defensive copy)
     */
    public static Collection<Keeper> getAll() {
        return allKeepers.values();
    }
    
    /**
     * Removes a keeper from the registry by their ID.
     * 
     * Note: ID is never reused even after deletion.
     * Warning: Should check if keeper has allocated cages before removing.
     * 
     * @param keeperId the unique identifier of the keeper to remove
     * @return true if keeper was removed, false if keeper was not found
     */
    public static boolean remove(int keeperId) {
        Keeper removed = allKeepers.remove(keeperId);
        if (removed != null) {
            System.out.println("Keeper removed: " + removed.getFullName() + " (ID: " + keeperId + ")");
            return true;
        }
        return false;
    }
    
    /**
     * Returns the total number of keepers in the registry.
     * 
     * @return count of keepers
     */
    public static int count() {
        return allKeepers.size();
    }
    
    /**
     * Removes all keepers from the registry.
     * 
     * Warning: This does NOT reset the ID counter.
     * Used primarily for testing purposes.
     */
    public static void clear() {
        allKeepers.clear();
        System.out.println("All keepers cleared from registry");
    }
    
    /**
     * Initialises the registry from persisted data.
     * 
     * Called on application startup to load keepers from XML file.
     * Restores the ID counter to the highest ID found in the loaded data.
     * 
     * @param keepers collection of keepers loaded from persistence
     */
    public static void initializeFromPersistence(Collection<Keeper> keepers) {
        allKeepers.clear();
        
        for (Keeper keeper : keepers) {
            allKeepers.put(keeper.getKeeperId(), keeper);
            
            // Update counter to highest ID found
            if (keeper.getKeeperId() > lastKeeperId) {
                lastKeeperId = keeper.getKeeperId();
            }
        }
        
        System.out.println("Keepers registry initialised: " + allKeepers.size() + 
                          " keepers loaded, next ID will be " + (lastKeeperId + 1));
    }
    
    /**
     * Finds a keeper by their full name.
     * 
     * Searches for "FirstName Surname" match (case-insensitive).
     * 
     * @param fullName the full name to search for
     * @return the keeper with the given name, or null if not found
     */
    public static Keeper findByFullName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return null;
        }
        
        return allKeepers.values().stream()
                .filter(keeper -> keeper.getFullName().equalsIgnoreCase(fullName))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Finds all keepers with a specific position.
     * 
     * @param position the position to filter by (HEAD_KEEPER or ASSISTANT_KEEPER)
     * @return collection of keepers with the specified position
     */
    public static Collection<Keeper> findByPosition(Keeper.Position position) {
        return allKeepers.values().stream()
                .filter(keeper -> keeper.getPosition() == position)
                .toList();
    }
    
    /**
     * Finds all keepers who can accept more cage allocations.
     * 
     * Returns keepers who have less than 4 cages allocated.
     * 
     * @return collection of keepers with available capacity
     */
    public static Collection<Keeper> findAvailableKeepers() {
        return allKeepers.values().stream()
                .filter(Keeper::canAcceptMoreCages)
                .toList();
    }
    
    /**
     * Finds all keepers assigned to a specific cage.
     * 
     * @param cageId the cage ID to search for
     * @return collection of keepers assigned to this cage
     */
    public static Collection<Keeper> findByCageId(int cageId) {
        return allKeepers.values().stream()
                .filter(keeper -> keeper.getAllocatedCageIds().contains(cageId))
                .toList();
    }
    
    /**
     * Finds all keepers who are overloaded (have 4 cages).
     * 
     * @return collection of keepers at maximum capacity
     */
    public static Collection<Keeper> findOverloadedKeepers() {
        return allKeepers.values().stream()
                .filter(keeper -> keeper.getAllocatedCageCount() >= 4)
                .toList();
    }
    
    /**
     * Gets the current highest keeper ID in use.
     * 
     * Useful for persistence layer to know the ID counter state.
     * 
     * @return highest keeper ID currently in use
     */
    public static int getLastKeeperId() {
        return lastKeeperId;
    }
    
    /**
     * Checks if a keeper with the given ID exists.
     * 
     * @param keeperId the ID to check
     * @return true if keeper exists, false otherwise
     */
    public static boolean exists(int keeperId) {
        return allKeepers.containsKey(keeperId);
    }
}
