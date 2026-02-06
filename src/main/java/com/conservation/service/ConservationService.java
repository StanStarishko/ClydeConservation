package com.conservation.service;

import com.conservation.exception.ValidationException;
import com.conservation.model.Animal;
import com.conservation.model.Cage;
import com.conservation.model.Keeper;
import com.conservation.registry.Animals;
import com.conservation.registry.Cages;
import com.conservation.registry.Keepers;

/**
 * Main service class coordinating all conservation system operations.
 * 
 * Provides high-level business operations:
 * - Animal allocation to cages
 * - Keeper allocation to cages
 * - Removal operations
 * - Data retrieval and reporting
 * 
 * Coordinates between:
 * - Registry classes (Animals, Keepers, Cages)
 * - Validator (AllocationValidator)
 * - Persistence layer (future integration)
 */
public class ConservationService {
    
    private final AllocationValidator validator;
    
    /**
     * Default constructor.
     * Creates service with new validator instance.
     */
    public ConservationService() {
        this.validator = new AllocationValidator();
    }
    
    /**
     * Constructor with custom validator.
     * Useful for testing with mock validators.
     * 
     * @param validator the validator to use
     */
    public ConservationService(AllocationValidator validator) {
        this.validator = validator;
    }
    
    // ============================================
    // ANIMAL OPERATIONS
    // ============================================
    
    /**
     * Adds a new animal to the system.
     * 
     * Validates animal data and adds to registry with auto-generated ID.
     * 
     * @param animal the animal to add
     * @return the animal ID assigned
     * @throws ValidationException if animal data is invalid
     */
    public int addAnimal(Animal animal) throws ValidationException {
        // Validate animal data
        validator.validateAnimalData(animal);
        
        // Add to registry (ID auto-generated)
        Animals.add(animal);
        
        System.out.println(String.format("Animal added: %s (ID: %d)",
            animal.getName(), animal.getAnimalId()));
        
        return animal.getAnimalId();
    }
    
    /**
     * Allocates an animal to a cage.
     * 
     * Validates business rules before allocation:
     * - Cage capacity not exceeded
     * - Predator/prey compatibility
     * 
     * @param animalId the ID of the animal to allocate
     * @param cageId the ID of the cage to allocate to
     * @throws ValidationException if allocation violates business rules
     * @throws IllegalArgumentException if animal or cage not found
     */
    public void allocateAnimalToCage(int animalId, int cageId) throws ValidationException {
        // Find entities
        Animal animal = Animals.findById(animalId);
        if (animal == null) {
            throw new IllegalArgumentException("Animal not found: ID " + animalId);
        }
        
        Cage cage = Cages.findById(cageId);
        if (cage == null) {
            throw new IllegalArgumentException("Cage not found: ID " + cageId);
        }
        
        // Validate allocation
        validator.validateAnimalToCage(animal, cage);
        
        // Perform allocation
        cage.addAnimal(animalId);
        
        System.out.println(String.format("Animal '%s' (ID: %d) allocated to Cage %d (%s). Occupancy: %s",
            animal.getName(),
            animalId,
            cageId,
            cage.getCageNumber(),
            cage.getOccupancyInfo()));
    }
    
    /**
     * Removes an animal from its cage.
     * 
     * @param animalId the ID of the animal to remove
     * @param cageId the ID of the cage to remove from
     * @throws IllegalArgumentException if animal or cage not found
     */
    public void removeAnimalFromCage(int animalId, int cageId) {
        // Find entities
        Animal animal = Animals.findById(animalId);
        if (animal == null) {
            throw new IllegalArgumentException("Animal not found: ID " + animalId);
        }
        
        Cage cage = Cages.findById(cageId);
        if (cage == null) {
            throw new IllegalArgumentException("Cage not found: ID " + cageId);
        }
        
        // Remove animal from cage
        boolean removed = cage.removeAnimal(animalId);
        
        if (removed) {
            System.out.println(String.format("Animal '%s' (ID: %d) removed from Cage %d. Occupancy: %s",
                animal.getName(),
                animalId,
                cageId,
                cage.getOccupancyInfo()));
        } else {
            System.out.println(String.format("Animal %d was not in Cage %d", animalId, cageId));
        }
    }
    
    /**
     * Removes an animal from the system entirely.
     * 
     * Removes from all cages and from registry.
     * 
     * @param animalId the ID of the animal to remove
     * @throws IllegalArgumentException if animal not found
     */
    public void removeAnimal(int animalId) {
        Animal animal = Animals.findById(animalId);
        if (animal == null) {
            throw new IllegalArgumentException("Animal not found: ID " + animalId);
        }
        
        // Find and remove from any cages
        for (Cage cage : Cages.findByAnimalId(animalId)) {
            cage.removeAnimal(animalId);
            System.out.println(String.format("Animal %d removed from Cage %d", animalId, cage.getCageId()));
        }
        
        // Remove from registry
        Animals.remove(animalId);
        
        System.out.println(String.format("Animal '%s' (ID: %d) completely removed from system",
            animal.getName(), animalId));
    }
    
    // ============================================
    // KEEPER OPERATIONS
    // ============================================
    
    /**
     * Adds a new keeper to the system.
     * 
     * Validates keeper data and adds to registry with auto-generated ID.
     * 
     * @param keeper the keeper to add
     * @return the keeper ID assigned
     * @throws ValidationException if keeper data is invalid
     */
    public int addKeeper(Keeper keeper) throws ValidationException {
        // Validate keeper data
        validator.validateKeeperData(keeper);
        
        // Add to registry (ID auto-generated)
        Keepers.add(keeper);
        
        System.out.println(String.format("Keeper added: %s (ID: %d, Position: %s)",
            keeper.getFullName(), keeper.getKeeperId(), keeper.getPosition()));
        
        return keeper.getKeeperId();
    }
    
    /**
     * Allocates a keeper to a cage.
     * 
     * Validates business rules before allocation:
     * - Keeper has not exceeded maximum cage limit (4)
     * 
     * If cage already has a keeper, replaces the existing keeper.
     * 
     * @param keeperId the ID of the keeper to allocate
     * @param cageId the ID of the cage to allocate to
     * @throws ValidationException if allocation violates business rules
     * @throws IllegalArgumentException if keeper or cage not found
     */
    public void allocateKeeperToCage(int keeperId, int cageId) throws ValidationException {
        // Find entities
        Keeper keeper = Keepers.findById(keeperId);
        if (keeper == null) {
            throw new IllegalArgumentException("Keeper not found: ID " + keeperId);
        }
        
        Cage cage = Cages.findById(cageId);
        if (cage == null) {
            throw new IllegalArgumentException("Cage not found: ID " + cageId);
        }
        
        // Validate allocation
        validator.validateKeeperToCage(keeper, cage);
        
        // Handle existing keeper (if any)
        if (cage.hasAssignedKeeper()) {
            Integer previousKeeperId = cage.getAssignedKeeperId();
            Keeper previousKeeper = Keepers.findById(previousKeeperId);
            if (previousKeeper != null && !previousKeeperId.equals(keeperId)) {
                previousKeeper.removeAllocatedCage(cageId);
                System.out.println(String.format("Cage %d reassigned from Keeper %d to Keeper %d",
                    cageId, previousKeeperId, keeperId));
            }
        }
        
        // Perform allocation
        cage.setAssignedKeeperId(keeperId);
        keeper.addAllocatedCage(cageId);
        
        System.out.println(String.format("Keeper %s (ID: %d) allocated to Cage %d (%s). Keeper workload: %d/%d cages",
            keeper.getFullName(),
            keeperId,
            cageId,
            cage.getCageNumber(),
            keeper.getAllocatedCageCount(),
            4));  // Max cages hardcoded for display
    }
    
    /**
     * Removes a keeper from a cage.
     * 
     * Validates that keeper maintains minimum cage allocation (1) after removal.
     * 
     * @param keeperId the ID of the keeper to remove
     * @param cageId the ID of the cage to remove from
     * @throws ValidationException if removal violates minimum cage rule
     * @throws IllegalArgumentException if keeper or cage not found
     */
    public void removeKeeperFromCage(int keeperId, int cageId) throws ValidationException {
        // Find entities
        Keeper keeper = Keepers.findById(keeperId);
        if (keeper == null) {
            throw new IllegalArgumentException("Keeper not found: ID " + keeperId);
        }
        
        Cage cage = Cages.findById(cageId);
        if (cage == null) {
            throw new IllegalArgumentException("Cage not found: ID " + cageId);
        }
        
        // Validate removal (checks minimum cage requirement)
        validator.validateKeeperRemoval(keeper);
        
        // Remove keeper from cage
        if (cage.getAssignedKeeperId() != null && cage.getAssignedKeeperId().equals(keeperId)) {
            cage.setAssignedKeeperId(null);
            keeper.removeAllocatedCage(cageId);
            
            System.out.println(String.format("Keeper %s (ID: %d) removed from Cage %d. Keeper workload: %d cages",
                keeper.getFullName(),
                keeperId,
                cageId,
                keeper.getAllocatedCageCount()));
        } else {
            System.out.println(String.format("Keeper %d was not assigned to Cage %d", keeperId, cageId));
        }
    }
    
    /**
     * Removes a keeper from the system entirely.
     * 
     * Warning: Only removes if keeper has no allocated cages.
     * Must unassign all cages first.
     * 
     * @param keeperId the ID of the keeper to remove
     * @throws IllegalStateException if keeper still has allocated cages
     * @throws IllegalArgumentException if keeper not found
     */
    public void removeKeeper(int keeperId) {
        Keeper keeper = Keepers.findById(keeperId);
        if (keeper == null) {
            throw new IllegalArgumentException("Keeper not found: ID " + keeperId);
        }
        
        // Check keeper has no allocated cages
        if (keeper.getAllocatedCageCount() > 0) {
            throw new IllegalStateException(
                String.format("Cannot remove Keeper %d. Keeper still has %d allocated cage(s). Unassign cages first.",
                    keeperId, keeper.getAllocatedCageCount())
            );
        }
        
        // Remove from registry
        Keepers.remove(keeperId);
        
        System.out.println(String.format("Keeper %s (ID: %d) removed from system",
            keeper.getFullName(), keeperId));
    }
    
    // ============================================
    // CAGE OPERATIONS
    // ============================================
    
    /**
     * Adds a new cage to the system.
     * 
     * Validates cage data and adds to registry with auto-generated ID.
     * 
     * @param cage the cage to add
     * @return the cage ID assigned
     * @throws ValidationException if cage data is invalid
     */
    public int addCage(Cage cage) throws ValidationException {
        // Validate cage data
        validator.validateCageData(cage);
        
        // Add to registry (ID auto-generated)
        Cages.add(cage);
        
        System.out.println(String.format("Cage added: %s (ID: %d, Capacity: %d)",
            cage.getCageNumber(), cage.getCageId(), cage.getAnimalCapacity()));
        
        return cage.getCageId();
    }
    
    /**
     * Removes a cage from the system entirely.
     * 
     * Warning: Only removes if cage is empty (no animals).
     * Must remove all animals first.
     * 
     * @param cageId the ID of the cage to remove
     * @throws IllegalStateException if cage still contains animals
     * @throws IllegalArgumentException if cage not found
     */
    public void removeCage(int cageId) {
        Cage cage = Cages.findById(cageId);
        if (cage == null) {
            throw new IllegalArgumentException("Cage not found: ID " + cageId);
        }
        
        // Check cage is empty
        if (!cage.isEmpty()) {
            throw new IllegalStateException(
                String.format("Cannot remove Cage %d. Cage contains %d animal(s). Remove animals first.",
                    cageId, cage.getCurrentOccupancy())
            );
        }
        
        // Remove keeper assignment if exists
        if (cage.hasAssignedKeeper()) {
            Integer keeperId = cage.getAssignedKeeperId();
            Keeper keeper = Keepers.findById(keeperId);
            if (keeper != null) {
                keeper.removeAllocatedCage(cageId);
            }
        }
        
        // Remove from registry
        Cages.remove(cageId);
        
        System.out.println(String.format("Cage %s (ID: %d) removed from system",
            cage.getCageNumber(), cageId));
    }
    
    // ============================================
    // REPORTING / QUERY OPERATIONS
    // ============================================
    
    /**
     * Gets system statistics summary.
     * 
     * @return formatted string with system statistics
     */
    public String getSystemStatistics() {
        int totalAnimals = Animals.count();
        int totalKeepers = Keepers.count();
        int totalCages = Cages.count();
        
        int totalCapacity = Cages.getTotalCapacity();
        int totalOccupancy = Cages.getTotalOccupancy();
        int availableSpace = totalCapacity - totalOccupancy;
        
        int emptyCages = Cages.findEmptyCages().size();
        int fullCages = Cages.findFullCages().size();
        int unassignedCages = Cages.findUnassignedCages().size();
        
        return String.format(
            "=== SYSTEM STATISTICS ===\n" +
            "Animals: %d\n" +
            "Keepers: %d\n" +
            "Cages: %d (Empty: %d, Full: %d, Unassigned: %d)\n" +
            "Capacity: %d/%d animals (%d available spaces)\n" +
            "Occupancy Rate: %.1f%%\n" +
            "========================",
            totalAnimals,
            totalKeepers,
            totalCages, emptyCages, fullCages, unassignedCages,
            totalOccupancy, totalCapacity, availableSpace,
            totalCapacity > 0 ? (totalOccupancy * 100.0 / totalCapacity) : 0.0
        );
    }
    
    /**
     * Gets the validator used by this service.
     * 
     * @return the allocation validator
     */
    public AllocationValidator getValidator() {
        return validator;
    }
}
