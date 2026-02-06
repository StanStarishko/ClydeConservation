package com.conservation.service;

import com.conservation.config.Settings;
import com.conservation.config.SettingsManager;
import com.conservation.exception.ValidationException;
import com.conservation.interfaces.IValidator;
import com.conservation.model.Animal;
import com.conservation.model.Cage;
import com.conservation.model.Keeper;
import com.conservation.registry.Animals;
import com.conservation.registry.Cages;

/**
 * Validator for animal and keeper allocation business rules.
 * 
 * Validates:
 * - Cage capacity constraints
 * - Predator/prey compatibility rules
 * - Keeper workload limits (1-4 cages)
 * 
 * Implements IValidator interface for consistent validation approach.
 */
public class AllocationValidator implements IValidator<Object> {
    
    private String lastValidationError;
    
    /**
     * Default constructor.
     */
    public AllocationValidator() {
        this.lastValidationError = null;
    }
    
    /**
     * Generic validation method (required by IValidator interface).
     * 
     * Routes to specific validation methods based on object type.
     * Not typically used directly - use specific methods instead.
     * 
     * @param entity the object to validate
     * @return true if validation passes, false otherwise
     */
    @Override
    public boolean validate(Object entity) {
        lastValidationError = "Cannot validate unknown object type";
        return false;
    }
    
    /**
     * Gets the last validation error message.
     * 
     * @return error message from last validation, or null if validation passed
     */
    @Override
    public String getValidationError() {
        return lastValidationError;
    }
    
    /**
     * Validates that an animal can be allocated to a specific cage.
     * 
     * Checks:
     * 1. Cage is not at capacity
     * 2. Predator/prey compatibility with existing animals
     * 3. Cage has assigned keeper (optional check)
     * 
     * @param animal the animal to allocate
     * @param cage the cage to allocate to
     * @throws ValidationException if validation fails
     */
    public void validateAnimalToCage(Animal animal, Cage cage) throws ValidationException {
        if (animal == null) {
            throw new ValidationException(
                ValidationException.ErrorType.INVALID_ANIMAL_DATA,
                "Animal cannot be null"
            );
        }
        
        if (cage == null) {
            throw new ValidationException(
                ValidationException.ErrorType.INVALID_CAGE_DATA,
                "Cage cannot be null"
            );
        }
        
        // Check 1: Cage capacity
        if (cage.isFull()) {
            throw new ValidationException(
                ValidationException.ErrorType.CAGE_CAPACITY_EXCEEDED,
                String.format("Cage %d (%s) is at maximum capacity (%d/%d animals)",
                    cage.getCageId(),
                    cage.getCageNumber(),
                    cage.getCurrentOccupancy(),
                    cage.getAnimalCapacity())
            );
        }
        
        // Check 2: Predator/prey compatibility
        validatePredatorPreyCompatibility(animal, cage);
        
        // Validation passed
        lastValidationError = null;
    }
    
    /**
     * Validates predator/prey compatibility rules.
     * 
     * Business rules:
     * - Predator animals MUST be housed alone (cannot share with any other animal)
     * - Prey animals CAN share cages with other prey animals
     * - Prey animals CANNOT share cages with predators
     * 
     * @param animal the animal to allocate
     * @param cage the cage to check compatibility with
     * @throws ValidationException if compatibility rules are violated
     */
    private void validatePredatorPreyCompatibility(Animal animal, Cage cage) 
            throws ValidationException {
        
        // Get animal rules from settings
        Settings.AnimalRules rules = SettingsManager.getAnimalRules();
        
        // If cage is empty, any animal can go in
        if (cage.isEmpty()) {
            return;
        }
        
        // Get existing animals in cage
        boolean cageHasPredators = false;
        boolean cageHasPrey = false;
        
        for (Integer existingAnimalId : cage.getCurrentAnimalIds()) {
            Animal existingAnimal = Animals.findById(existingAnimalId);
            if (existingAnimal != null) {
                if (existingAnimal.getCategory() == Animal.Category.PREDATOR) {
                    cageHasPredators = true;
                } else {
                    cageHasPrey = true;
                }
            }
        }
        
        // Rule 1: Cannot add any animal to cage with predator
        if (cageHasPredators) {
            throw new ValidationException(
                ValidationException.ErrorType.INVALID_PREDATOR_PREY_MIX,
                String.format("Cannot add %s '%s' to Cage %d. Cage contains predator animal(s). Predators must be housed alone.",
                    animal.getCategory(),
                    animal.getName(),
                    cage.getCageId())
            );
        }
        
        // Rule 2: Cannot add predator to cage with any other animals
        if (animal.getCategory() == Animal.Category.PREDATOR && !cage.isEmpty()) {
            throw new ValidationException(
                ValidationException.ErrorType.INVALID_PREDATOR_PREY_MIX,
                String.format("Cannot add PREDATOR '%s' to Cage %d. Cage already contains %d animal(s). Predators must be housed alone.",
                    animal.getName(),
                    cage.getCageId(),
                    cage.getCurrentOccupancy())
            );
        }
        
        // Rule 3: Prey can share with other prey (this is allowed)
        if (animal.getCategory() == Animal.Category.PREY && cageHasPrey) {
            // This is valid - prey can share with prey
            return;
        }
    }
    
    /**
     * Validates that a keeper can be allocated to a specific cage.
     * 
     * Checks:
     * 1. Keeper has not reached maximum cage limit (default: 4)
     * 2. Keeper has at least minimum cages if removing (default: 1)
     * 3. Cage exists and is valid
     * 
     * @param keeper the keeper to allocate
     * @param cage the cage to allocate to
     * @throws ValidationException if validation fails
     */
    public void validateKeeperToCage(Keeper keeper, Cage cage) throws ValidationException {
        if (keeper == null) {
            throw new ValidationException(
                ValidationException.ErrorType.INVALID_KEEPER_DATA,
                "Keeper cannot be null"
            );
        }
        
        if (cage == null) {
            throw new ValidationException(
                ValidationException.ErrorType.INVALID_CAGE_DATA,
                "Cage cannot be null"
            );
        }
        
        // Get keeper constraints from settings
        Settings.KeeperConstraints constraints = SettingsManager.getKeeperConstraints();
        int maxCages = constraints.getMaxCages();
        
        // Check: Keeper has not exceeded maximum cage allocation
        if (keeper.getAllocatedCageCount() >= maxCages) {
            throw new ValidationException(
                ValidationException.ErrorType.KEEPER_OVERLOAD,
                String.format("Keeper %s (%s) already has maximum cages allocated (%d/%d). Cannot assign more cages.",
                    keeper.getKeeperId(),
                    keeper.getFullName(),
                    keeper.getAllocatedCageCount(),
                    maxCages)
            );
        }
        
        // Check: If cage already has a keeper, validate replacement
        if (cage.hasAssignedKeeper()) {
            Integer currentKeeperId = cage.getAssignedKeeperId();
            if (!currentKeeperId.equals(keeper.getKeeperId())) {
                // This is a keeper replacement - allowed but should be logged
                System.out.println(String.format("Note: Cage %d will be reassigned from Keeper %d to Keeper %d",
                    cage.getCageId(), currentKeeperId, keeper.getKeeperId()));
            }
        }
        
        // Validation passed
        lastValidationError = null;
    }
    
    /**
     * Validates that a keeper can be removed from a cage.
     * 
     * Checks that keeper maintains minimum cage allocation after removal.
     * 
     * @param keeper the keeper to remove from cage
     * @throws ValidationException if removal would violate minimum cage rule
     */
    public void validateKeeperRemoval(Keeper keeper) throws ValidationException {
        if (keeper == null) {
            throw new ValidationException(
                ValidationException.ErrorType.INVALID_KEEPER_DATA,
                "Keeper cannot be null"
            );
        }
        
        // Get keeper constraints from settings
        Settings.KeeperConstraints constraints = SettingsManager.getKeeperConstraints();
        int minCages = constraints.getMinCages();
        
        // Check: After removal, keeper must still have minimum cages
        if (keeper.getAllocatedCageCount() - 1 < minCages) {
            throw new ValidationException(
                ValidationException.ErrorType.KEEPER_UNDERLOAD,
                String.format("Cannot remove cage from Keeper %s (%s). Keeper must maintain at least %d cage(s). Current: %d",
                    keeper.getKeeperId(),
                    keeper.getFullName(),
                    minCages,
                    keeper.getAllocatedCageCount())
            );
        }
        
        lastValidationError = null;
    }
    
    /**
     * Validates that an animal's data is complete and valid.
     * 
     * Used when adding new animals to the system.
     * 
     * @param animal the animal to validate
     * @throws ValidationException if animal data is invalid
     */
    public void validateAnimalData(Animal animal) throws ValidationException {
        if (animal == null) {
            throw new ValidationException(
                ValidationException.ErrorType.INVALID_ANIMAL_DATA,
                "Animal cannot be null"
            );
        }
        
        // Validation is handled by Animal class constructor and setters
        // This method provides explicit validation entry point for service layer
        
        try {
            // Trigger validation by accessing fields
            if (animal.getName() == null || animal.getName().trim().isEmpty()) {
                throw new ValidationException(
                    ValidationException.ErrorType.INVALID_ANIMAL_DATA,
                    "Animal name cannot be empty"
                );
            }
            
            if (animal.getType() == null || animal.getType().trim().isEmpty()) {
                throw new ValidationException(
                    ValidationException.ErrorType.INVALID_ANIMAL_DATA,
                    "Animal type cannot be empty"
                );
            }
            
            if (animal.getCategory() == null) {
                throw new ValidationException(
                    ValidationException.ErrorType.INVALID_ANIMAL_DATA,
                    "Animal category must be specified (PREDATOR or PREY)"
                );
            }
            
            if (animal.getDateOfBirth() == null) {
                throw new ValidationException(
                    ValidationException.ErrorType.INVALID_ANIMAL_DATA,
                    "Animal date of birth must be specified"
                );
            }
            
            if (animal.getDateOfAcquisition() == null) {
                throw new ValidationException(
                    ValidationException.ErrorType.INVALID_ANIMAL_DATA,
                    "Animal date of acquisition must be specified"
                );
            }
            
            if (animal.getSex() == null) {
                throw new ValidationException(
                    ValidationException.ErrorType.INVALID_ANIMAL_DATA,
                    "Animal sex must be specified (MALE or FEMALE)"
                );
            }
            
            lastValidationError = null;
            
        } catch (IllegalArgumentException illegalArgException) {
            throw new ValidationException(
                ValidationException.ErrorType.INVALID_ANIMAL_DATA,
                illegalArgException.getMessage()
            );
        }
    }
    
    /**
     * Validates that a keeper's data is complete and valid.
     * 
     * Used when adding new keepers to the system.
     * 
     * @param keeper the keeper to validate
     * @throws ValidationException if keeper data is invalid
     */
    public void validateKeeperData(Keeper keeper) throws ValidationException {
        if (keeper == null) {
            throw new ValidationException(
                ValidationException.ErrorType.INVALID_KEEPER_DATA,
                "Keeper cannot be null"
            );
        }
        
        try {
            // Trigger validation by accessing fields
            if (keeper.getFirstName() == null || keeper.getFirstName().trim().isEmpty()) {
                throw new ValidationException(
                    ValidationException.ErrorType.INVALID_KEEPER_DATA,
                    "Keeper first name cannot be empty"
                );
            }
            
            if (keeper.getSurname() == null || keeper.getSurname().trim().isEmpty()) {
                throw new ValidationException(
                    ValidationException.ErrorType.INVALID_KEEPER_DATA,
                    "Keeper surname cannot be empty"
                );
            }
            
            if (keeper.getAddress() == null || keeper.getAddress().trim().isEmpty()) {
                throw new ValidationException(
                    ValidationException.ErrorType.INVALID_KEEPER_DATA,
                    "Keeper address cannot be empty"
                );
            }
            
            if (keeper.getContactNumber() == null || keeper.getContactNumber().trim().isEmpty()) {
                throw new ValidationException(
                    ValidationException.ErrorType.INVALID_KEEPER_DATA,
                    "Keeper contact number cannot be empty"
                );
            }
            
            if (keeper.getPosition() == null) {
                throw new ValidationException(
                    ValidationException.ErrorType.INVALID_KEEPER_DATA,
                    "Keeper position must be specified (HEAD_KEEPER or ASSISTANT_KEEPER)"
                );
            }
            
            lastValidationError = null;
            
        } catch (IllegalArgumentException illegalArgException) {
            throw new ValidationException(
                ValidationException.ErrorType.INVALID_KEEPER_DATA,
                illegalArgException.getMessage()
            );
        }
    }
    
    /**
     * Validates that a cage's data is complete and valid.
     * 
     * Used when adding new cages to the system.
     * 
     * @param cage the cage to validate
     * @throws ValidationException if cage data is invalid
     */
    public void validateCageData(Cage cage) throws ValidationException {
        if (cage == null) {
            throw new ValidationException(
                ValidationException.ErrorType.INVALID_CAGE_DATA,
                "Cage cannot be null"
            );
        }
        
        try {
            // Trigger validation by accessing fields
            if (cage.getCageNumber() == null || cage.getCageNumber().trim().isEmpty()) {
                throw new ValidationException(
                    ValidationException.ErrorType.INVALID_CAGE_DATA,
                    "Cage number cannot be empty"
                );
            }
            
            if (cage.getDescription() == null || cage.getDescription().trim().isEmpty()) {
                throw new ValidationException(
                    ValidationException.ErrorType.INVALID_CAGE_DATA,
                    "Cage description cannot be empty"
                );
            }
            
            if (cage.getAnimalCapacity() <= 0) {
                throw new ValidationException(
                    ValidationException.ErrorType.INVALID_CAGE_DATA,
                    "Cage capacity must be positive"
                );
            }
            
            lastValidationError = null;
            
        } catch (IllegalArgumentException illegalArgException) {
            throw new ValidationException(
                ValidationException.ErrorType.INVALID_CAGE_DATA,
                illegalArgException.getMessage()
            );
        }
    }
}
