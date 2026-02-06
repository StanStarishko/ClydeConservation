package com.conservation.service;

import com.conservation.config.Settings;
import com.conservation.config.SettingsManager;
import com.conservation.exception.ValidationException;
import com.conservation.model.Animal;
import com.conservation.model.AssistantKeeper;
import com.conservation.model.Cage;
import com.conservation.model.HeadKeeper;
import com.conservation.model.Keeper;
import com.conservation.registry.Animals;
import com.conservation.registry.Cages;
import com.conservation.registry.Keepers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AllocationValidator class.
 * Tests all business rule validations for animal and keeper allocations.
 * 
 * Business Rules Tested:
 * - Cage capacity limits
 * - Predator/prey separation (predators must be alone)
 * - Keeper workload constraints (1-4 cages)
 * - Animal/keeper removal validations
 */
@DisplayName("AllocationValidator Tests")
public class AllocationValidatorTest {

    private AllocationValidator validator;
    
    // Test fixtures - dates
    private LocalDate validBirthDate;
    private LocalDate validAcquisitionDate;
    
    // Test fixtures - entities
    private Animal predatorAnimal;
    private Animal preyAnimal;
    private Animal anotherPreyAnimal;
    private Cage emptyCage;
    private Cage cageWithPrey;
    private Cage cageWithPredator;
    private Cage fullCage;
    private HeadKeeper headKeeper;
    private AssistantKeeper assistantKeeper;

    @BeforeEach
    void setUp() {
        validator = new AllocationValidator();
        
        // Clear registries before each test
        Animals.clear();
        Keepers.clear();
        Cages.clear();
        
        // Set up test dates
        validBirthDate = LocalDate.of(2020, 5, 15);
        validAcquisitionDate = LocalDate.of(2023, 11, 20);
        
        // Create test animals
        predatorAnimal = new Animal("Leo", "Tiger", Animal.Category.PREDATOR,
                                    validBirthDate, validAcquisitionDate, Animal.Sex.MALE);
        predatorAnimal.setAnimalId(1);
        
        preyAnimal = new Animal("Marty", "Zebra", Animal.Category.PREY,
                                validBirthDate, validAcquisitionDate, Animal.Sex.MALE);
        preyAnimal.setAnimalId(2);
        
        anotherPreyAnimal = new Animal("Bugs", "Rabbit", Animal.Category.PREY,
                                       validBirthDate, validAcquisitionDate, Animal.Sex.FEMALE);
        anotherPreyAnimal.setAnimalId(3);
        
        // Create test cages
        emptyCage = new Cage("Large-01", "Large predator cage", 10);
        emptyCage.setCageId(1);
        
        cageWithPrey = new Cage("Medium-01", "Medium prey cage", 5);
        cageWithPrey.setCageId(2);
        cageWithPrey.addAnimal(preyAnimal.getAnimalId());
        
        cageWithPredator = new Cage("Small-01", "Small predator cage", 1);
        cageWithPredator.setCageId(3);
        cageWithPredator.addAnimal(predatorAnimal.getAnimalId());
        
        fullCage = new Cage("Small-02", "Full small cage", 1);
        fullCage.setCageId(4);
        fullCage.addAnimal(99); // Add dummy animal to make it full
        
        // Create test keepers
        headKeeper = new HeadKeeper("John", "Smith", "123 Main St", "07123456789");
        headKeeper.setKeeperId(1);
        
        assistantKeeper = new AssistantKeeper("Emma", "Wilson", "456 Oak Ave", "07987654321");
        assistantKeeper.setKeeperId(2);
    }

    // ========================================================================
    // Animal-to-Cage Validation Tests
    // ========================================================================
    
    @Nested
    @DisplayName("Animal-to-Cage Validation Tests")
    class AnimalToCageValidationTests {
        
        @Test
        @DisplayName("Should allow prey animal to empty cage")
        void testPreyToEmptyCage() {
            assertDoesNotThrow(() -> {
                validator.validateAnimalToCage(preyAnimal, emptyCage);
            });
        }
        
        @Test
        @DisplayName("Should allow predator animal to empty cage")
        void testPredatorToEmptyCage() {
            assertDoesNotThrow(() -> {
                validator.validateAnimalToCage(predatorAnimal, emptyCage);
            });
        }
        
        @Test
        @DisplayName("Should allow prey animal to cage with other prey")
        void testPreyToCageWithPrey() {
            assertDoesNotThrow(() -> {
                validator.validateAnimalToCage(anotherPreyAnimal, cageWithPrey);
            });
        }
        
        @Test
        @DisplayName("Should reject predator animal to cage with prey")
        void testPredatorToCageWithPrey() {
            ValidationException exception = assertThrows(ValidationException.class, () -> {
                validator.validateAnimalToCage(predatorAnimal, cageWithPrey);
            });
            
            assertEquals(ValidationException.ErrorType.INVALID_PREDATOR_PREY_MIX, 
                        exception.getErrorType());
        }
        
        @Test
        @DisplayName("Should reject prey animal to cage with predator")
        void testPreyToCageWithPredator() {
            // First, we need to register the predator so validator can check its category
            Animals.add(predatorAnimal);
            
            ValidationException exception = assertThrows(ValidationException.class, () -> {
                validator.validateAnimalToCage(preyAnimal, cageWithPredator);
            });
            
            assertEquals(ValidationException.ErrorType.INVALID_PREDATOR_PREY_MIX, 
                        exception.getErrorType());
        }
        
        @Test
        @DisplayName("Should reject predator to cage with existing predator")
        void testPredatorToCageWithPredator() {
            // Predators must be alone - another predator shouldn't be added
            Animal anotherPredator = new Animal("Shere Khan", "Tiger", Animal.Category.PREDATOR,
                                                validBirthDate, validAcquisitionDate, Animal.Sex.MALE);
            anotherPredator.setAnimalId(10);
            
            Animals.add(predatorAnimal);
            
            ValidationException exception = assertThrows(ValidationException.class, () -> {
                validator.validateAnimalToCage(anotherPredator, cageWithPredator);
            });
            
            assertEquals(ValidationException.ErrorType.INVALID_PREDATOR_PREY_MIX, 
                        exception.getErrorType());
        }
        
        @Test
        @DisplayName("Should reject animal to full cage")
        void testAnimalToFullCage() {
            ValidationException exception = assertThrows(ValidationException.class, () -> {
                validator.validateAnimalToCage(preyAnimal, fullCage);
            });
            
            assertEquals(ValidationException.ErrorType.CAGE_CAPACITY_EXCEEDED, 
                        exception.getErrorType());
        }
        
        @Test
        @DisplayName("Should reject animal already in cage")
        void testAnimalAlreadyInCage() {
            // preyAnimal is already in cageWithPrey
            ValidationException exception = assertThrows(ValidationException.class, () -> {
                validator.validateAnimalToCage(preyAnimal, cageWithPrey);
            });
            
            // Should fail with some validation error (could be INVALID_INPUT or custom)
            assertNotNull(exception.getErrorType());
        }
        
        @Test
        @DisplayName("Should reject null animal")
        void testNullAnimal() {
            assertThrows(ValidationException.class, () -> {
                validator.validateAnimalToCage(null, emptyCage);
            });
        }
        
        @Test
        @DisplayName("Should reject null cage")
        void testNullCage() {
            assertThrows(ValidationException.class, () -> {
                validator.validateAnimalToCage(preyAnimal, null);
            });
        }
    }

    // ========================================================================
    // Keeper-to-Cage Validation Tests
    // ========================================================================
    
    @Nested
    @DisplayName("Keeper-to-Cage Validation Tests")
    class KeeperToCageValidationTests {
        
        @Test
        @DisplayName("Should allow keeper with no cages to be assigned")
        void testKeeperWithNoCages() {
            assertDoesNotThrow(() -> {
                validator.validateKeeperToCage(headKeeper, emptyCage);
            });
        }
        
        @Test
        @DisplayName("Should allow keeper with 3 cages to accept 4th")
        void testKeeperWith3Cages() {
            // Assign 3 cages to keeper
            headKeeper.addAllocatedCage(1);
            headKeeper.addAllocatedCage(2);
            headKeeper.addAllocatedCage(3);
            
            Cage fourthCage = new Cage("Large-04", "Fourth cage", 10);
            fourthCage.setCageId(10);
            
            assertDoesNotThrow(() -> {
                validator.validateKeeperToCage(headKeeper, fourthCage);
            });
        }
        
        @Test
        @DisplayName("Should reject keeper with 4 cages from accepting 5th")
        void testKeeperWith4Cages() {
            // Assign 4 cages (maximum) to keeper
            headKeeper.addAllocatedCage(1);
            headKeeper.addAllocatedCage(2);
            headKeeper.addAllocatedCage(3);
            headKeeper.addAllocatedCage(4);
            
            Cage fifthCage = new Cage("Large-05", "Fifth cage", 10);
            fifthCage.setCageId(10);
            
            ValidationException exception = assertThrows(ValidationException.class, () -> {
                validator.validateKeeperToCage(headKeeper, fifthCage);
            });
            
            assertEquals(ValidationException.ErrorType.KEEPER_OVERLOAD, 
                        exception.getErrorType());
        }
        
        @Test
        @DisplayName("Should reject keeper already assigned to this cage")
        void testKeeperAlreadyAssigned() {
            headKeeper.addAllocatedCage(emptyCage.getCageId());
            
            ValidationException exception = assertThrows(ValidationException.class, () -> {
                validator.validateKeeperToCage(headKeeper, emptyCage);
            });
            
            assertNotNull(exception.getErrorType());
        }
        
        @Test
        @DisplayName("Should reject null keeper")
        void testNullKeeper() {
            assertThrows(ValidationException.class, () -> {
                validator.validateKeeperToCage(null, emptyCage);
            });
        }
        
        @Test
        @DisplayName("Should work for both HeadKeeper and AssistantKeeper")
        void testBothKeeperTypes() {
            assertDoesNotThrow(() -> {
                validator.validateKeeperToCage(headKeeper, emptyCage);
            });
            
            Cage anotherCage = new Cage("Medium-02", "Another cage", 5);
            anotherCage.setCageId(20);
            
            assertDoesNotThrow(() -> {
                validator.validateKeeperToCage(assistantKeeper, anotherCage);
            });
        }
    }

    // ========================================================================
    // Keeper Removal Validation Tests
    // ========================================================================
    
    @Nested
    @DisplayName("Keeper Removal Validation Tests")
    class KeeperRemovalValidationTests {
        
        @Test
        @DisplayName("Should allow removal when keeper has multiple cages")
        void testRemovalWithMultipleCages() {
            headKeeper.addAllocatedCage(1);
            headKeeper.addAllocatedCage(2);
            
            assertDoesNotThrow(() -> {
                validator.validateKeeperRemoval(headKeeper, false);
            });
        }
        
        @Test
        @DisplayName("Should reject removal when keeper has only 1 cage (underload)")
        void testRemovalCausingUnderload() {
            headKeeper.addAllocatedCage(1);
            
            ValidationException exception = assertThrows(ValidationException.class, () -> {
                validator.validateKeeperRemoval(headKeeper, false);
            });
            
            assertEquals(ValidationException.ErrorType.KEEPER_UNDERLOAD, 
                        exception.getErrorType());
        }
        
        @Test
        @DisplayName("Should allow removal with underload when explicitly allowed")
        void testRemovalWithAllowUnderload() {
            headKeeper.addAllocatedCage(1);
            
            assertDoesNotThrow(() -> {
                validator.validateKeeperRemoval(headKeeper, true);
            });
        }
        
        @Test
        @DisplayName("Should allow removal when keeper has no cages with underload allowed")
        void testRemovalNoCagesWithAllowUnderload() {
            assertDoesNotThrow(() -> {
                validator.validateKeeperRemoval(headKeeper, true);
            });
        }
    }

    // ========================================================================
    // Animal Removal Validation Tests
    // ========================================================================
    
    @Nested
    @DisplayName("Animal Removal Validation Tests")
    class AnimalRemovalValidationTests {
        
        @Test
        @DisplayName("Should allow removal when animal is in cage")
        void testRemovalAnimalInCage() {
            assertDoesNotThrow(() -> {
                validator.validateAnimalRemoval(preyAnimal, cageWithPrey);
            });
        }
        
        @Test
        @DisplayName("Should reject removal when animal is not in cage")
        void testRemovalAnimalNotInCage() {
            ValidationException exception = assertThrows(ValidationException.class, () -> {
                validator.validateAnimalRemoval(anotherPreyAnimal, cageWithPrey);
            });
            
            assertNotNull(exception);
        }
        
        @Test
        @DisplayName("Should reject removal from empty cage")
        void testRemovalFromEmptyCage() {
            ValidationException exception = assertThrows(ValidationException.class, () -> {
                validator.validateAnimalRemoval(preyAnimal, emptyCage);
            });
            
            assertNotNull(exception);
        }
    }

    // ========================================================================
    // Cage Validation Tests
    // ========================================================================
    
    @Nested
    @DisplayName("Cage Validation Tests")
    class CageValidationTests {
        
        @Test
        @DisplayName("Should validate cage with positive capacity")
        void testValidCage() {
            assertDoesNotThrow(() -> {
                validator.validateCage(emptyCage);
            });
        }
        
        @Test
        @DisplayName("Should validate cage at capacity")
        void testCageAtCapacity() {
            // fullCage has capacity 1 and 1 animal
            assertDoesNotThrow(() -> {
                validator.validateCage(fullCage);
            });
        }
        
        @Test
        @DisplayName("Should reject null cage")
        void testNullCageValidation() {
            assertThrows(ValidationException.class, () -> {
                validator.validateCage(null);
            });
        }
    }

    // ========================================================================
    // Error Message Quality Tests
    // ========================================================================
    
    @Nested
    @DisplayName("Error Message Quality Tests")
    class ErrorMessageQualityTests {
        
        @Test
        @DisplayName("Should provide detailed error for capacity exceeded")
        void testCapacityErrorMessage() {
            try {
                validator.validateAnimalToCage(preyAnimal, fullCage);
                fail("Should have thrown ValidationException");
            } catch (ValidationException exception) {
                String message = exception.getMessage();
                assertNotNull(message);
                assertFalse(message.isEmpty());
                // Message should contain useful context
            }
        }
        
        @Test
        @DisplayName("Should provide detailed error for predator/prey conflict")
        void testPredatorPreyErrorMessage() {
            try {
                validator.validateAnimalToCage(predatorAnimal, cageWithPrey);
                fail("Should have thrown ValidationException");
            } catch (ValidationException exception) {
                String message = exception.getMessage();
                assertNotNull(message);
                assertFalse(message.isEmpty());
            }
        }
        
        @Test
        @DisplayName("Should provide detailed error for keeper overload")
        void testKeeperOverloadErrorMessage() {
            headKeeper.addAllocatedCage(1);
            headKeeper.addAllocatedCage(2);
            headKeeper.addAllocatedCage(3);
            headKeeper.addAllocatedCage(4);
            
            Cage newCage = new Cage("Test", "Test cage", 5);
            newCage.setCageId(100);
            
            try {
                validator.validateKeeperToCage(headKeeper, newCage);
                fail("Should have thrown ValidationException");
            } catch (ValidationException exception) {
                String message = exception.getMessage();
                assertNotNull(message);
                assertFalse(message.isEmpty());
            }
        }
        
        @Test
        @DisplayName("Should store last validation error for retrieval")
        void testLastValidationError() {
            try {
                validator.validateAnimalToCage(preyAnimal, fullCage);
            } catch (ValidationException ignored) {
                // Expected
            }
            
            String lastError = validator.getValidationError();
            assertNotNull(lastError);
            assertFalse(lastError.isEmpty());
        }
    }

    // ========================================================================
    // Settings Integration Tests
    // ========================================================================
    
    @Nested
    @DisplayName("Settings Integration Tests")
    class SettingsIntegrationTests {
        
        @Test
        @DisplayName("Should use keeper constraints from settings")
        void testKeeperConstraintsFromSettings() {
            Settings settings = SettingsManager.getSettings();
            
            assertNotNull(settings);
            assertNotNull(settings.getKeeperConstraints());
            
            int maxCages = settings.getKeeperConstraints().getMaxCages();
            assertTrue(maxCages > 0, "Max cages should be positive");
            assertEquals(4, maxCages, "Default max cages should be 4");
        }
        
        @Test
        @DisplayName("Should use animal rules from settings")
        void testAnimalRulesFromSettings() {
            Settings settings = SettingsManager.getSettings();
            
            assertNotNull(settings);
            assertNotNull(settings.getAnimalRules());
            
            assertFalse(settings.getAnimalRules().isPredatorShareable(),
                       "Predators should not be shareable by default");
            assertTrue(settings.getAnimalRules().isPreyShareable(),
                      "Prey should be shareable by default");
        }
        
        @Test
        @DisplayName("Should respect min cages constraint from settings")
        void testMinCagesConstraint() {
            Settings settings = SettingsManager.getSettings();
            
            int minCages = settings.getKeeperConstraints().getMinCages();
            assertEquals(1, minCages, "Default min cages should be 1");
        }
    }

    // ========================================================================
    // IValidator Interface Compliance Tests
    // ========================================================================
    
    @Nested
    @DisplayName("IValidator Interface Compliance Tests")
    class IValidatorComplianceTests {
        
        @Test
        @DisplayName("Should implement validate method correctly")
        void testValidateMethod() {
            // Test that validate returns boolean for valid entity
            boolean result = validator.validate(emptyCage);
            assertTrue(result, "Empty valid cage should pass validation");
        }
        
        @Test
        @DisplayName("Should return false for invalid entity")
        void testValidateReturnsFalse() {
            // Test with null - should return false, not throw
            boolean result = validator.validate(null);
            assertFalse(result, "Null entity should fail validation");
        }
        
        @Test
        @DisplayName("Should populate validation error on failure")
        void testGetValidationError() {
            validator.validate(null);
            
            String error = validator.getValidationError();
            assertNotNull(error);
            assertFalse(error.isEmpty());
        }
    }

    // ========================================================================
    // Edge Cases Tests
    // ========================================================================
    
    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {
        
        @Test
        @DisplayName("Should handle cage with zero capacity")
        void testZeroCapacityCage() {
            Cage zeroCage = new Cage("Zero", "Zero capacity cage", 0);
            zeroCage.setCageId(999);
            
            // Should reject any animal
            assertThrows(ValidationException.class, () -> {
                validator.validateAnimalToCage(preyAnimal, zeroCage);
            });
        }
        
        @Test
        @DisplayName("Should handle keeper at exact boundary (4 cages)")
        void testKeeperAtExactBoundary() {
            // Add exactly 3 cages
            headKeeper.addAllocatedCage(1);
            headKeeper.addAllocatedCage(2);
            headKeeper.addAllocatedCage(3);
            
            assertEquals(3, headKeeper.getAllocatedCageIds().size());
            assertTrue(headKeeper.canAcceptMoreCages(), 
                      "Keeper with 3 cages should accept more");
            
            // Add 4th cage
            headKeeper.addAllocatedCage(4);
            
            assertEquals(4, headKeeper.getAllocatedCageIds().size());
            assertFalse(headKeeper.canAcceptMoreCages(), 
                       "Keeper with 4 cages should not accept more");
        }
        
        @Test
        @DisplayName("Should validate multiple prey animals in same cage")
        void testMultiplePreyInCage() {
            // Add multiple prey to a cage
            Cage multiPreyCage = new Cage("Multi", "Multi prey cage", 10);
            multiPreyCage.setCageId(500);
            
            Animal prey1 = new Animal("Prey1", "Zebra", Animal.Category.PREY,
                                      validBirthDate, validAcquisitionDate, Animal.Sex.MALE);
            prey1.setAnimalId(101);
            
            Animal prey2 = new Animal("Prey2", "Rabbit", Animal.Category.PREY,
                                      validBirthDate, validAcquisitionDate, Animal.Sex.FEMALE);
            prey2.setAnimalId(102);
            
            Animal prey3 = new Animal("Prey3", "Guinea Pig", Animal.Category.PREY,
                                      validBirthDate, validAcquisitionDate, Animal.Sex.MALE);
            prey3.setAnimalId(103);
            
            // All should pass
            assertDoesNotThrow(() -> validator.validateAnimalToCage(prey1, multiPreyCage));
            
            multiPreyCage.addAnimal(prey1.getAnimalId());
            Animals.add(prey1);
            
            assertDoesNotThrow(() -> validator.validateAnimalToCage(prey2, multiPreyCage));
            
            multiPreyCage.addAnimal(prey2.getAnimalId());
            Animals.add(prey2);
            
            assertDoesNotThrow(() -> validator.validateAnimalToCage(prey3, multiPreyCage));
        }
    }
}
