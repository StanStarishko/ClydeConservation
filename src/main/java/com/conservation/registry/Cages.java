package com.conservation.registry;

import com.conservation.interfaces.IRegistry;
import com.conservation.model.Cage;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Registry class for managing all cages in the conservation system.
 * 
 * Provides in-memory storage using HashMap for O(1) lookups by cage ID.
 * Implements IRegistry interface for standard CRUD operations.
 * 
 * Uses static storage - all instances share the same cage collection.
 */
public class Cages implements IRegistry<Cage> {
    
    private static final Map<Integer, Cage> allCages = new HashMap<>();
    private static int lastCageId = 0;
    
    /**
     * Generates the next available cage ID.
     * 
     * Uses auto-increment counter starting from 1.
     * Counter is restored from persistence on application startup.
     * 
     * @return next available cage ID
     */
    private static int generateNextCageId() {
        return ++lastCageId;
    }
    
    /**
     * Adds a new cage to the registry.
     * 
     * Automatically generates and assigns a unique ID to the cage.
     * 
     * @param cage the cage to add
     * @throws IllegalArgumentException if cage is null
     */
    @Override
    public void add(Cage cage) {
        if (cage == null) {
            throw new IllegalArgumentException("Cannot add null cage");
        }
        
        // Generate new ID and assign to cage
        int newId = generateNextCageId();
        cage.setCageId(newId);
        
        allCages.put(newId, cage);
        System.out.println("Cage added: " + cage.getCageNumber() + " (ID: " + newId + ")");
    }
    
    /**
     * Finds a cage by its unique ID.
     * 
     * @param cageId the unique identifier of the cage
     * @return the cage with the given ID, or null if not found
     */
    @Override
    public Cage findById(int cageId) {
        return allCages.get(cageId);
    }
    
    /**
     * Returns all cages currently in the registry.
     * 
     * @return collection of all cages (defensive copy)
     */
    @Override
    public Collection<Cage> getAll() {
        return allCages.values();
    }
    
    /**
     * Removes a cage from the registry by its ID.
     * 
     * Note: ID is never reused even after deletion.
     * Warning: Should check if cage contains animals before removing.
     * 
     * @param cageId the unique identifier of the cage to remove
     * @return true if cage was removed, false if cage was not found
     */
    @Override
    public boolean remove(int cageId) {
        Cage removed = allCages.remove(cageId);
        if (removed != null) {
            System.out.println("Cage removed: " + removed.getCageNumber() + " (ID: " + cageId + ")");
            return true;
        }
        return false;
    }
    
    /**
     * Returns the total number of cages in the registry.
     * 
     * @return count of cages
     */
    @Override
    public int count() {
        return allCages.size();
    }
    
    /**
     * Removes all cages from the registry.
     * 
     * Warning: This does NOT reset the ID counter.
     * Used primarily for testing purposes.
     */
    @Override
    public void clear() {
        allCages.clear();
        System.out.println("All cages cleared from registry");
    }
    
    /**
     * Initialises the registry from persisted data.
     * 
     * Called on application startup to load cages from XML file.
     * Restores the ID counter to the highest ID found in the loaded data.
     * 
     * @param cages collection of cages loaded from persistence
     */
    public static void initializeFromPersistence(Collection<Cage> cages) {
        allCages.clear();
        
        for (Cage cage : cages) {
            allCages.put(cage.getCageId(), cage);
            
            // Update counter to highest ID found
            if (cage.getCageId() > lastCageId) {
                lastCageId = cage.getCageId();
            }
        }
        
        System.out.println("Cages registry initialised: " + allCages.size() + 
                          " cages loaded, next ID will be " + (lastCageId + 1));
    }
    
    /**
     * Finds a cage by its cage number.
     * 
     * Cage number is the human-readable identifier (e.g., "Large-01").
     * 
     * @param cageNumber the cage number to search for
     * @return the cage with the given cage number, or null if not found
     */
    public static Cage findByCageNumber(String cageNumber) {
        if (cageNumber == null || cageNumber.trim().isEmpty()) {
            return null;
        }
        
        return allCages.values().stream()
                .filter(cage -> cage.getCageNumber().equalsIgnoreCase(cageNumber))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Finds all empty cages (no animals).
     * 
     * @return collection of empty cages
     */
    public static Collection<Cage> findEmptyCages() {
        return allCages.values().stream()
                .filter(Cage::isEmpty)
                .toList();
    }
    
    /**
     * Finds all full cages (at maximum capacity).
     * 
     * @return collection of full cages
     */
    public static Collection<Cage> findFullCages() {
        return allCages.values().stream()
                .filter(Cage::isFull)
                .toList();
    }
    
    /**
     * Finds all cages with available space.
     * 
     * Returns cages that are not at maximum capacity.
     * 
     * @return collection of cages with available space
     */
    public static Collection<Cage> findAvailableCages() {
        return allCages.values().stream()
                .filter(cage -> !cage.isFull())
                .toList();
    }
    
    /**
     * Finds all cages assigned to a specific keeper.
     * 
     * @param keeperId the ID of the keeper
     * @return collection of cages assigned to this keeper
     */
    public static Collection<Cage> findByKeeperId(int keeperId) {
        return allCages.values().stream()
                .filter(cage -> cage.hasAssignedKeeper() && 
                               cage.getAssignedKeeperId().equals(keeperId))
                .toList();
    }
    
    /**
     * Finds all cages that contain a specific animal.
     * 
     * @param animalId the ID of the animal to search for
     * @return collection of cages containing this animal (should be at most 1)
     */
    public static Collection<Cage> findByAnimalId(int animalId) {
        return allCages.values().stream()
                .filter(cage -> cage.getCurrentAnimalIds().contains(animalId))
                .toList();
    }
    
    /**
     * Finds all cages without an assigned keeper.
     * 
     * @return collection of cages without a keeper
     */
    public static Collection<Cage> findUnassignedCages() {
        return allCages.values().stream()
                .filter(cage -> !cage.hasAssignedKeeper())
                .toList();
    }
    
    /**
     * Finds cages by capacity.
     * 
     * Useful for finding small (1), medium (5), or large (10) cages.
     * 
     * @param capacity the capacity to filter by
     * @return collection of cages with the specified capacity
     */
    public static Collection<Cage> findByCapacity(int capacity) {
        return allCages.values().stream()
                .filter(cage -> cage.getAnimalCapacity() == capacity)
                .toList();
    }
    
    /**
     * Gets the total capacity of all cages in the system.
     * 
     * @return sum of all cage capacities
     */
    public static int getTotalCapacity() {
        return allCages.values().stream()
                .mapToInt(Cage::getAnimalCapacity)
                .sum();
    }
    
    /**
     * Gets the total number of animals currently in all cages.
     * 
     * @return sum of all current occupancies
     */
    public static int getTotalOccupancy() {
        return allCages.values().stream()
                .mapToInt(Cage::getCurrentOccupancy)
                .sum();
    }
    
    /**
     * Gets the current highest cage ID in use.
     * 
     * Useful for persistence layer to know the ID counter state.
     * 
     * @return highest cage ID currently in use
     */
    public static int getLastCageId() {
        return lastCageId;
    }
    
    /**
     * Checks if a cage with the given ID exists.
     * 
     * @param cageId the ID to check
     * @return true if cage exists, false otherwise
     */
    public static boolean exists(int cageId) {
        return allCages.containsKey(cageId);
    }
}
