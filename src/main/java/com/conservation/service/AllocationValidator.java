package com.conservation.service;

import com.conservation.config.Settings;
import com.conservation.config.SettingsManager;
import com.conservation.exception.ValidationException;
import com.conservation.interfaces.IValidator;
import com.conservation.model.Animal;
import com.conservation.model.Cage;
import com.conservation.model.Keeper;
import com.conservation.registry.Animals;

/**
 * Validator for animal and keeper allocation business rules.
 *
 * Validates:
 * - Cage capacity constraints
 * - Predator/prey compatibility rules
 * - Keeper workload limits (1-4 cages)
 * - Animal and keeper removal constraints
 * - Cage data integrity
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
        if (entity == null) {
            lastValidationError = "Cannot validate null entity";
            return false;
        }

        if (entity instanceof Cage cageEntity) {
            try {
                validateCage(cageEntity);
                return true;
            } catch (ValidationException validationException) {
                lastValidationError = validationException.getMessage();
                return false;
            }
        }

        if (entity instanceof Animal animalEntity) {
            try {
                validateAnimalData(animalEntity);
                return true;
            } catch (ValidationException validationException) {
                lastValidationError = validationException.getMessage();
                return false;
            }
        }

        if (entity instanceof Keeper keeperEntity) {
            try {
                validateKeeperData(keeperEntity);
                return true;
            } catch (ValidationException validationException) {
                lastValidationError = validationException.getMessage();
                return false;
            }
        }

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

    // ============================================
    // ANIMAL-TO-CAGE VALIDATION
    // ============================================

    /**
     * Validates that an animal can be allocated to a specific cage.
     *
     * Checks:
     * 1. Cage is not at capacity
     * 2. Predator/prey compatibility with existing animals
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
            lastValidationError = String.format("Cage %d (%s) is at maximum capacity (%d/%d animals)",
                    cage.getCageId(), cage.getCageNumber(),
                    cage.getCurrentOccupancy(), cage.getAnimalCapacity());
            throw new ValidationException(
                    ValidationException.ErrorType.CAGE_CAPACITY_EXCEEDED,
                    lastValidationError
            );
        }

        // Check 2: Predator/prey compatibility
        validatePredatorPreyCompatibility(animal, cage);

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

        // If cage is empty, any animal can go in
        if (cage.isEmpty()) {
            return;
        }

        // Determine existing animal categories in cage
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
            lastValidationError = String.format(
                    "Cannot add %s '%s' to Cage %d. Cage contains predator animal(s). Predators must be housed alone.",
                    animal.getCategory(), animal.getName(), cage.getCageId());
            throw new ValidationException(
                    ValidationException.ErrorType.INVALID_PREDATOR_PREY_MIX,
                    lastValidationError
            );
        }

        // Rule 2: Cannot add predator to cage with any other animals
        if (animal.getCategory() == Animal.Category.PREDATOR && !cage.isEmpty()) {
            lastValidationError = String.format(
                    "Cannot add PREDATOR '%s' to Cage %d. Cage already contains %d animal(s). Predators must be housed alone.",
                    animal.getName(), cage.getCageId(), cage.getCurrentOccupancy());
            throw new ValidationException(
                    ValidationException.ErrorType.INVALID_PREDATOR_PREY_MIX,
                    lastValidationError
            );
        }

        // Rule 3: Prey can share with other prey - this is valid
    }

    // ============================================
    // KEEPER-TO-CAGE VALIDATION
    // ============================================

    /**
     * Validates that a keeper can be allocated to a specific cage.
     *
     * Checks:
     * 1. Keeper has not reached maximum cage limit (default: 4)
     * 2. Keeper is not already assigned to this cage
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

        Settings.KeeperConstraints constraints = SettingsManager.getKeeperConstraints();
        int maxCages = constraints.getMaxCages();

        // Check: Keeper has not exceeded maximum cage allocation
        if (keeper.getAllocatedCageCount() >= maxCages) {
            lastValidationError = String.format(
                    "Keeper %s (%s) already has maximum cages allocated (%d/%d). Cannot assign more cages.",
                    keeper.getKeeperId(), keeper.getFullName(),
                    keeper.getAllocatedCageCount(), maxCages);
            throw new ValidationException(
                    ValidationException.ErrorType.KEEPER_OVERLOAD,
                    lastValidationError
            );
        }

        // Check: If cage already has a keeper, log the reassignment
        if (cage.hasAssignedKeeper()) {
            Integer currentKeeperId = cage.getAssignedKeeperId();
            if (!currentKeeperId.equals(keeper.getKeeperId())) {
                System.out.println(String.format(
                        "Note: Cage %d will be reassigned from Keeper %d to Keeper %d",
                        cage.getCageId(), currentKeeperId, keeper.getKeeperId()));
            }
        }

        lastValidationError = null;
    }

    // ============================================
    // REMOVAL VALIDATION
    // ============================================

    /**
     * Validates that a keeper can be removed from a cage.
     * Strict mode - does not allow underload below minimum cages.
     *
     * @param keeper the keeper to remove from cage
     * @throws ValidationException if removal would violate minimum cage rule
     */
    public void validateKeeperRemoval(Keeper keeper) throws ValidationException {
        validateKeeperRemoval(keeper, false);
    }

    /**
     * Validates that a keeper can be removed from a cage.
     *
     * When allowUnderload is false, keeper must maintain minimum cage allocation after removal.
     * When allowUnderload is true, the minimum cage constraint is bypassed.
     *
     * @param keeper the keeper to remove from cage
     * @param allowUnderload if true, permits removal even if it drops below minimum cages
     * @throws ValidationException if removal is not permitted
     */
    public void validateKeeperRemoval(Keeper keeper, boolean allowUnderload)
            throws ValidationException {
        if (keeper == null) {
            throw new ValidationException(
                    ValidationException.ErrorType.INVALID_KEEPER_DATA,
                    "Keeper cannot be null"
            );
        }

        if (!allowUnderload) {
            Settings.KeeperConstraints constraints = SettingsManager.getKeeperConstraints();
            int minCages = constraints.getMinCages();

            if (keeper.getAllocatedCageCount() - 1 < minCages) {
                lastValidationError = String.format(
                        "Cannot remove cage from Keeper %s (%s). Keeper must maintain at least %d cage(s). Current: %d",
                        keeper.getKeeperId(), keeper.getFullName(),
                        minCages, keeper.getAllocatedCageCount());
                throw new ValidationException(
                        ValidationException.ErrorType.KEEPER_UNDERLOAD,
                        lastValidationError
                );
            }
        }

        lastValidationError = null;
    }

    /**
     * Validates that an animal can be removed from a specific cage.
     *
     * Checks that the animal is actually present in the cage before removal.
     *
     * @param animal the animal to remove
     * @param cage the cage to remove the animal from
     * @throws ValidationException if the animal is not in the cage or inputs are invalid
     */
    public void validateAnimalRemoval(Animal animal, Cage cage) throws ValidationException {
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

        if (!cage.getCurrentAnimalIds().contains(animal.getAnimalId())) {
            lastValidationError = String.format(
                    "Animal '%s' (ID: %d) is not in Cage %d (%s). Cannot remove.",
                    animal.getName(), animal.getAnimalId(),
                    cage.getCageId(), cage.getCageNumber());
            throw new ValidationException(
                    ValidationException.ErrorType.INVALID_ANIMAL_DATA,
                    lastValidationError
            );
        }

        lastValidationError = null;
    }

    // ============================================
    // CAGE VALIDATION
    // ============================================

    /**
     * Validates that a cage's data is consistent and valid.
     *
     * Checks:
     * 1. Cage is not null
     * 2. Capacity is positive
     * 3. Current occupancy does not exceed capacity
     *
     * @param cage the cage to validate
     * @throws ValidationException if cage data is invalid
     */
    public void validateCage(Cage cage) throws ValidationException {
        if (cage == null) {
            throw new ValidationException(
                    ValidationException.ErrorType.INVALID_CAGE_DATA,
                    "Cage cannot be null"
            );
        }

        if (cage.getAnimalCapacity() <= 0) {
            lastValidationError = String.format(
                    "Cage %d (%s) has invalid capacity: %d. Capacity must be positive.",
                    cage.getCageId(), cage.getCageNumber(), cage.getAnimalCapacity());
            throw new ValidationException(
                    ValidationException.ErrorType.INVALID_CAGE_DATA,
                    lastValidationError
            );
        }

        if (cage.getCurrentOccupancy() > cage.getAnimalCapacity()) {
            lastValidationError = String.format(
                    "Cage %d (%s) is over capacity: %d/%d animals.",
                    cage.getCageId(), cage.getCageNumber(),
                    cage.getCurrentOccupancy(), cage.getAnimalCapacity());
            throw new ValidationException(
                    ValidationException.ErrorType.CAGE_CAPACITY_EXCEEDED,
                    lastValidationError
            );
        }

        lastValidationError = null;
    }

    // ============================================
    // ENTITY DATA VALIDATION
    // ============================================

    /**
     * Validates that an animal's data is complete and valid.
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

        try {
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

        if (keeper.getPosition() == null) {
            throw new ValidationException(
                    ValidationException.ErrorType.INVALID_KEEPER_DATA,
                    "Keeper position must be specified"
            );
        }

        lastValidationError = null;
    }
}