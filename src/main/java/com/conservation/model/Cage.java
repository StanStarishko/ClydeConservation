package com.conservation.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Entity class representing a cage in the conservation system.
 * 
 * Cages house animals and have a maximum capacity that cannot be exceeded.
 * Business rules:
 * - Predator animals must be housed alone (capacity = 1 for predators)
 * - Prey animals can share cages up to the cage's capacity
 * - Each cage can be assigned to one keeper (who may manage multiple cages)
 */
public class Cage {
    
    private int cageId;
    private String cageNumber;
    private String description;
    private int animalCapacity;
    private List<Integer> currentAnimalIds;
    private Integer assignedKeeperId;  // Nullable - cage may not have keeper yet
    
    /**
     * Default constructor for creating empty Cage instance.
     * Required for XML deserialisation.
     */
    public Cage() {
        this.currentAnimalIds = new ArrayList<>();
    }
    
    /**
     * Full constructor for creating a Cage with all attributes.
     * 
     * @param cageNumber human-readable cage number (e.g., "Large-01")
     * @param description description of the cage (e.g., "Large predator cage")
     * @param animalCapacity maximum number of animals this cage can hold
     * @throws IllegalArgumentException if any required field is null or invalid
     */
    public Cage(String cageNumber, String description, int animalCapacity) {
        validateFields(cageNumber, description, animalCapacity);
        
        this.cageNumber = cageNumber;
        this.description = description;
        this.animalCapacity = animalCapacity;
        this.currentAnimalIds = new ArrayList<>();
        this.assignedKeeperId = null;
    }
    
    /**
     * Validates all required fields are present and valid.
     * 
     * @throws IllegalArgumentException if validation fails
     */
    private void validateFields(String cageNumber, String description, int animalCapacity) {
        if (cageNumber == null || cageNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Cage number cannot be null or empty");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Cage description cannot be null or empty");
        }
        if (animalCapacity <= 0) {
            throw new IllegalArgumentException("Cage capacity must be positive");
        }
    }
    
    /**
     * Adds an animal to this cage.
     * 
     * @param animalId the animal ID to add
     * @throws IllegalStateException if cage is at capacity
     * @throws IllegalArgumentException if animal is already in this cage
     */
    public void addAnimal(int animalId) {
        if (isFull()) {
            throw new IllegalStateException(
                String.format("Cage %d is at maximum capacity (%d/%d animals)",
                            cageId, currentAnimalIds.size(), animalCapacity));
        }
        if (currentAnimalIds.contains(animalId)) {
            throw new IllegalArgumentException(
                String.format("Animal %d is already in cage %d", animalId, cageId));
        }
        currentAnimalIds.add(animalId);
    }
    
    /**
     * Removes an animal from this cage.
     * 
     * @param animalId the animal ID to remove
     * @return true if animal was removed, false if animal was not in this cage
     */
    public boolean removeAnimal(int animalId) {
        return currentAnimalIds.remove(Integer.valueOf(animalId));
    }
    
    /**
     * Checks if this cage is at maximum capacity.
     * 
     * @return true if cage is full, false otherwise
     */
    public boolean isFull() {
        return currentAnimalIds.size() >= animalCapacity;
    }
    
    /**
     * Checks if this cage is empty (has no animals).
     * 
     * @return true if cage has no animals, false otherwise
     */
    public boolean isEmpty() {
        return currentAnimalIds.isEmpty();
    }
    
    /**
     * Gets the number of available spaces in this cage.
     * 
     * @return number of animals that can still be added
     */
    public int getAvailableSpace() {
        return animalCapacity - currentAnimalIds.size();
    }
    
    /**
     * Gets the current occupancy count.
     * 
     * @return number of animals currently in the cage
     */
    public int getCurrentOccupancy() {
        return currentAnimalIds.size();
    }
    
    /**
     * Checks if this cage has an assigned keeper.
     * 
     * @return true if keeper is assigned, false otherwise
     */
    public boolean hasAssignedKeeper() {
        return assignedKeeperId != null;
    }
    
    /**
     * Gets occupancy information as a formatted string.
     * 
     * @return occupancy string in format "3/10" (current/capacity)
     */
    public String getOccupancyInfo() {
        return String.format("%d/%d", currentAnimalIds.size(), animalCapacity);
    }
    
    /**
     * Gets the cage status based on occupancy.
     * 
     * @return "EMPTY" if no animals, "FULL" if at capacity, "AVAILABLE" otherwise
     */
    public String getStatus() {
        if (isEmpty()) {
            return "EMPTY";
        } else if (isFull()) {
            return "FULL";
        } else {
            return "AVAILABLE";
        }
    }
    
    // Getters and Setters
    
    public int getCageId() {
        return cageId;
    }
    
    public void setCageId(int cageId) {
        this.cageId = cageId;
    }
    
    public String getCageNumber() {
        return cageNumber;
    }
    
    public void setCageNumber(String cageNumber) {
        if (cageNumber == null || cageNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Cage number cannot be null or empty");
        }
        this.cageNumber = cageNumber;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Cage description cannot be null or empty");
        }
        this.description = description;
    }
    
    public int getAnimalCapacity() {
        return animalCapacity;
    }
    
    public void setAnimalCapacity(int animalCapacity) {
        if (animalCapacity <= 0) {
            throw new IllegalArgumentException("Cage capacity must be positive");
        }
        if (animalCapacity < currentAnimalIds.size()) {
            throw new IllegalArgumentException(
                "Cannot set capacity lower than current occupancy");
        }
        this.animalCapacity = animalCapacity;
    }
    
    public List<Integer> getCurrentAnimalIds() {
        // Return defensive copy
        return new ArrayList<>(currentAnimalIds);
    }
    
    public void setCurrentAnimalIds(List<Integer> currentAnimalIds) {
        this.currentAnimalIds = new ArrayList<>(currentAnimalIds);
    }
    
    public Integer getAssignedKeeperId() {
        return assignedKeeperId;
    }
    
    public void setAssignedKeeperId(Integer assignedKeeperId) {
        this.assignedKeeperId = assignedKeeperId;
    }
    
    /**
     * Checks equality based on cage ID.
     * 
     * @param obj the object to compare
     * @return true if objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Cage cage = (Cage) obj;
        return cageId == cage.cageId;
    }
    
    /**
     * Generates hash code based on cage ID.
     * 
     * @return hash code for this cage
     */
    @Override
    public int hashCode() {
        return Objects.hash(cageId);
    }
    
    /**
     * Returns string representation of the cage.
     * 
     * @return string representation
     */
    @Override
    public String toString() {
        return String.format("Cage{id=%d, number='%s', occupancy=%d/%d, keeper=%s}",
                cageId, cageNumber, currentAnimalIds.size(), animalCapacity,
                hasAssignedKeeper() ? assignedKeeperId : "None");
    }
}
