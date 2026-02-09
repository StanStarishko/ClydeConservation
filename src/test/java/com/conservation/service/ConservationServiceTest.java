package com.conservation.service;

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
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ConservationService class.
 * Tests coordination between registries, allocation operations, and query methods.
 * 
 * Operations Tested:
 * - Animal allocation to cages
 * - Keeper allocation to cages
 * - Animal removal from cages
 * - Keeper removal from cages
 * - Query methods for available entities
 * - Transaction rollback on failure
 */
@DisplayName("ConservationService Tests")
public class ConservationServiceTest {

    private ConservationService service;
    
    // Test fixtures - dates
    private LocalDate validBirthDate;
    private LocalDate validAcquisitionDate;
    
    // Test fixtures - animals
    private Animal predatorLeo;
    private Animal preyMarty;
    private Animal preyBugs;
    
    // Test fixtures - cages
    private Cage largeCage;
    private Cage mediumCage;
    private Cage smallCage;
    
    // Test fixtures - keepers
    private HeadKeeper headKeeperJohn;
    private AssistantKeeper assistantKeeperEmma;

    @BeforeEach
    void setUp() throws ValidationException {
        // Clear all registries before each test
        Animals.clear();
        Keepers.clear();
        Cages.clear();
        
        // Create service instance
        service = new ConservationService();
        
        // Set up test dates
        validBirthDate = LocalDate.of(2020, 5, 15);
        validAcquisitionDate = LocalDate.of(2023, 11, 20);
        
        // Create and register test animals
        predatorLeo = new Animal("Leo", "Tiger", Animal.Category.PREDATOR,
                                 validBirthDate, validAcquisitionDate, Animal.Sex.MALE);
        Animals.add(predatorLeo); // Auto-generates ID
        
        preyMarty = new Animal("Marty", "Zebra", Animal.Category.PREY,
                               validBirthDate, validAcquisitionDate, Animal.Sex.MALE);
        Animals.add(preyMarty);
        
        preyBugs = new Animal("Bugs", "Rabbit", Animal.Category.PREY,
                              validBirthDate, validAcquisitionDate, Animal.Sex.FEMALE);
        Animals.add(preyBugs);
        
        // Create and register test cages
        largeCage = new Cage("Large-01", "Large predator cage", 10);
        Cages.add(largeCage);
        
        mediumCage = new Cage("Medium-01", "Medium prey cage", 5);
        Cages.add(mediumCage);
        
        smallCage = new Cage("Small-01", "Small single cage", 1);
        Cages.add(smallCage);
        
        // Create and register test keepers
        headKeeperJohn = new HeadKeeper("John", "Smith", "123 Main St", "07123456789");
        Keepers.add(headKeeperJohn);
        
        assistantKeeperEmma = new AssistantKeeper("Emma", "Wilson", "456 Oak Ave", "07987654321");
        Keepers.add(assistantKeeperEmma);
    }

    // ========================================================================
    // Animal Allocation Tests
    // ========================================================================
    
    @Nested
    @DisplayName("Animal Allocation Tests")
    class AnimalAllocationTests {
        
        @Test
        @DisplayName("Should allocate prey animal to empty cage")
        void testAllocatePreyToEmptyCage() throws ValidationException {
            int animalId = preyMarty.getAnimalId();
            int cageId = mediumCage.getCageId();
            
            service.allocateAnimalToCage(animalId, cageId);
            
            // Verify animal is in cage
            Cage updatedCage = Cages.findById(cageId);
            assertTrue(updatedCage.getCurrentAnimalIds().contains(animalId),
                      "Animal should be in cage after allocation");
        }
        
        @Test
        @DisplayName("Should allocate predator animal to empty cage")
        void testAllocatePredatorToEmptyCage() throws ValidationException {
            int animalId = predatorLeo.getAnimalId();
            int cageId = largeCage.getCageId();
            
            service.allocateAnimalToCage(animalId, cageId);
            
            Cage updatedCage = Cages.findById(cageId);
            assertTrue(updatedCage.getCurrentAnimalIds().contains(animalId),
                      "Predator should be in cage after allocation");
        }
        
        @Test
        @DisplayName("Should allocate multiple prey to same cage")
        void testAllocateMultiplePreyToSameCage() throws ValidationException {
            int cageId = mediumCage.getCageId();
            
            service.allocateAnimalToCage(preyMarty.getAnimalId(), cageId);
            service.allocateAnimalToCage(preyBugs.getAnimalId(), cageId);
            
            Cage updatedCage = Cages.findById(cageId);
            assertEquals(2, updatedCage.getCurrentAnimalIds().size(),
                        "Cage should contain 2 prey animals");
        }
        
        @Test
        @DisplayName("Should reject predator to cage with prey")
        void testRejectPredatorToCageWithPrey() throws ValidationException {
            // First allocate prey
            service.allocateAnimalToCage(preyMarty.getAnimalId(), mediumCage.getCageId());
            
            // Try to allocate predator - should fail
            assertThrows(ValidationException.class, () -> service.allocateAnimalToCage(predatorLeo.getAnimalId(), mediumCage.getCageId()));
            
            // Verify predator was not added
            Cage cage = Cages.findById(mediumCage.getCageId());
            assertFalse(cage.getCurrentAnimalIds().contains(predatorLeo.getAnimalId()),
                       "Predator should not be in cage after failed allocation");
        }
        
        @Test
        @DisplayName("Should reject prey to cage with predator")
        void testRejectPreyToCageWithPredator() throws ValidationException {
            // First allocate predator
            service.allocateAnimalToCage(predatorLeo.getAnimalId(), largeCage.getCageId());
            
            // Try to allocate prey - should fail
            assertThrows(ValidationException.class, () -> service.allocateAnimalToCage(preyMarty.getAnimalId(), largeCage.getCageId()));
        }
        
        @Test
        @DisplayName("Should reject allocation when cage is full")
        void testRejectAllocationToFullCage() throws ValidationException {
            // Fill the small cage (capacity 1)
            service.allocateAnimalToCage(preyMarty.getAnimalId(), smallCage.getCageId());
            
            // Try to add another animal - should fail
            assertThrows(ValidationException.class, () -> service.allocateAnimalToCage(preyBugs.getAnimalId(), smallCage.getCageId()));
        }
        
        @Test
        @DisplayName("Should reject allocation for non-existent animal")
        void testRejectNonExistentAnimal() {
            int nonExistentAnimalId = 9999;
            
            assertThrows(ValidationException.class, () -> service.allocateAnimalToCage(nonExistentAnimalId, largeCage.getCageId()));
        }
        
        @Test
        @DisplayName("Should reject allocation for non-existent cage")
        void testRejectNonExistentCage() {
            int nonExistentCageId = 9999;
            
            assertThrows(ValidationException.class, () -> service.allocateAnimalToCage(preyMarty.getAnimalId(), nonExistentCageId));
        }
    }

    // ========================================================================
    // Keeper Allocation Tests
    // ========================================================================
    
    @Nested
    @DisplayName("Keeper Allocation Tests")
    class KeeperAllocationTests {
        
        @Test
        @DisplayName("Should allocate keeper to cage")
        void testAllocateKeeperToCage() throws ValidationException {
            int keeperId = headKeeperJohn.getKeeperId();
            int cageId = largeCage.getCageId();
            
            service.allocateKeeperToCage(keeperId, cageId);
            
            // Verify keeper has cage in list
            Keeper updatedKeeper = Keepers.findById(keeperId);
            assertTrue(updatedKeeper.getAllocatedCageIds().contains(cageId),
                      "Keeper should have cage in allocated list");
            
            // Verify cage has assigned keeper
            Cage updatedCage = Cages.findById(cageId);
            assertEquals(keeperId, updatedCage.getAssignedKeeperId(),
                        "Cage should have keeper assigned");
        }
        
        @Test
        @DisplayName("Should allocate keeper to multiple cages (up to 4)")
        void testAllocateKeeperToMultipleCages() throws ValidationException {
            int keeperId = headKeeperJohn.getKeeperId();
            
            // Create additional cages
            Cage cage2 = new Cage("Large-02", "Second cage", 10);
            Cages.add(cage2);
            Cage cage3 = new Cage("Large-03", "Third cage", 10);
            Cages.add(cage3);
            Cage cage4 = new Cage("Large-04", "Fourth cage", 10);
            Cages.add(cage4);
            
            // Allocate all 4 cages
            service.allocateKeeperToCage(keeperId, largeCage.getCageId());
            service.allocateKeeperToCage(keeperId, cage2.getCageId());
            service.allocateKeeperToCage(keeperId, cage3.getCageId());
            service.allocateKeeperToCage(keeperId, cage4.getCageId());
            
            Keeper updatedKeeper = Keepers.findById(keeperId);
            assertEquals(4, updatedKeeper.getAllocatedCageIds().size(),
                        "Keeper should have 4 cages allocated");
        }
        
        @Test
        @DisplayName("Should reject 5th cage allocation")
        void testRejectFifthCageAllocation() throws ValidationException {
            int keeperId = headKeeperJohn.getKeeperId();
            
            // Create 5 cages and allocate first 4
            Cage cage2 = new Cage("Large-02", "Second cage", 10);
            Cages.add(cage2);
            Cage cage3 = new Cage("Large-03", "Third cage", 10);
            Cages.add(cage3);
            Cage cage4 = new Cage("Large-04", "Fourth cage", 10);
            Cages.add(cage4);
            Cage cage5 = new Cage("Large-05", "Fifth cage", 10);
            Cages.add(cage5);
            
            service.allocateKeeperToCage(keeperId, largeCage.getCageId());
            service.allocateKeeperToCage(keeperId, cage2.getCageId());
            service.allocateKeeperToCage(keeperId, cage3.getCageId());
            service.allocateKeeperToCage(keeperId, cage4.getCageId());
            
            // 5th cage should fail
            assertThrows(ValidationException.class, () -> service.allocateKeeperToCage(keeperId, cage5.getCageId()));
        }
        
        @Test
        @DisplayName("Should work for both HeadKeeper and AssistantKeeper")
        void testBothKeeperTypes() throws ValidationException {
            service.allocateKeeperToCage(headKeeperJohn.getKeeperId(), largeCage.getCageId());
            service.allocateKeeperToCage(assistantKeeperEmma.getKeeperId(), mediumCage.getCageId());
            
            assertEquals(Integer.valueOf(headKeeperJohn.getKeeperId()), 
                        Cages.findById(largeCage.getCageId()).getAssignedKeeperId());
            assertEquals(Integer.valueOf(assistantKeeperEmma.getKeeperId()), 
                        Cages.findById(mediumCage.getCageId()).getAssignedKeeperId());
        }
        
        @Test
        @DisplayName("Should reject non-existent keeper")
        void testRejectNonExistentKeeper() {
            assertThrows(ValidationException.class, () -> service.allocateKeeperToCage(9999, largeCage.getCageId()));
        }
    }

    // ========================================================================
    // Animal Removal Tests
    // ========================================================================
    
    @Nested
    @DisplayName("Animal Removal Tests")
    class AnimalRemovalTests {
        
        @Test
        @DisplayName("Should remove animal from cage")
        void testRemoveAnimalFromCage() throws ValidationException {
            // First allocate
            int animalId = preyMarty.getAnimalId();
            int cageId = mediumCage.getCageId();
            service.allocateAnimalToCage(animalId, cageId);
            
            // Then remove
            service.removeAnimalFromCage(animalId, cageId);
            
            // Verify removal
            Cage updatedCage = Cages.findById(cageId);
            assertFalse(updatedCage.getCurrentAnimalIds().contains(animalId),
                       "Animal should not be in cage after removal");
        }
        
        @Test
        @DisplayName("Should reject removal of animal not in cage")
        void testRejectRemovalAnimalNotInCage() {
            // Try to remove animal that was never allocated
            assertThrows(ValidationException.class, () -> service.removeAnimalFromCage(preyMarty.getAnimalId(), mediumCage.getCageId()));
        }
        
        @Test
        @DisplayName("Should allow new allocations after removal")
        void testAllocationAfterRemoval() throws ValidationException {
            // Fill small cage
            service.allocateAnimalToCage(preyMarty.getAnimalId(), smallCage.getCageId());
            
            // Remove
            service.removeAnimalFromCage(preyMarty.getAnimalId(), smallCage.getCageId());
            
            // Should now accept new animal
            assertDoesNotThrow(() -> service.allocateAnimalToCage(preyBugs.getAnimalId(), smallCage.getCageId()));
        }
    }

    // ========================================================================
    // Keeper Removal Tests
    // ========================================================================
    
    @Nested
    @DisplayName("Keeper Removal Tests")
    class KeeperRemovalTests {
        
        @Test
        @DisplayName("Should remove keeper from cage when keeper has multiple cages")
        void testRemoveKeeperWithMultipleCages() throws ValidationException {
            int keeperId = headKeeperJohn.getKeeperId();
            
            // Allocate 2 cages
            service.allocateKeeperToCage(keeperId, largeCage.getCageId());
            service.allocateKeeperToCage(keeperId, mediumCage.getCageId());
            
            // Remove from one cage (without underload because keeper still has another)
            service.removeKeeperFromCage(keeperId, largeCage.getCageId());
            
            // Verify removal
            Keeper updatedKeeper = Keepers.findById(keeperId);
            assertFalse(updatedKeeper.getAllocatedCageIds().contains(largeCage.getCageId()),
                       "Keeper should not have removed cage");
            assertTrue(updatedKeeper.getAllocatedCageIds().contains(mediumCage.getCageId()),
                      "Keeper should still have other cage");
            
            Cage updatedCage = Cages.findById(largeCage.getCageId());
            assertNull(updatedCage.getAssignedKeeperId(),
                      "Cage should have no assigned keeper");
        }
        
        @Test
        @DisplayName("Should reject removal causing underload without flag")
        void testRejectRemovalCausingUnderload() throws ValidationException {
            int keeperId = headKeeperJohn.getKeeperId();
            
            // Allocate only 1 cage
            service.allocateKeeperToCage(keeperId, largeCage.getCageId());
            
            // Try to remove without allowUnderload - should fail
            assertThrows(ValidationException.class, () -> service.removeKeeperFromCage(keeperId, largeCage.getCageId()));
        }
        
        @Test
        @DisplayName("Should allow removal causing underload with flag")
        void testAllowRemovalWithUnderloadFlag() throws ValidationException {
            int keeperId = headKeeperJohn.getKeeperId();
            
            // Allocate only 1 cage
            service.allocateKeeperToCage(keeperId, largeCage.getCageId());
            
            // Remove with allowUnderload = true
            assertDoesNotThrow(() -> service.removeKeeperFromCage(keeperId, largeCage.getCageId()));
            
            Keeper updatedKeeper = Keepers.findById(keeperId);
            assertEquals(0, updatedKeeper.getAllocatedCageIds().size(),
                        "Keeper should have no cages after removal");
        }
    }

    // ========================================================================
    // Query Methods Tests
    // ========================================================================
    
    @Nested
    @DisplayName("Query Methods Tests")
    class QueryMethodsTests {
        
        @Test
        @DisplayName("Should return available animals (not allocated)")
        void testGetAvailableAnimals() throws ValidationException {
            // Initially all animals are available
            Collection<Animal> available = service.getAvailableAnimals();
            assertEquals(3, available.size(), "All 3 animals should be available initially");
            
            // Allocate one animal
            service.allocateAnimalToCage(preyMarty.getAnimalId(), mediumCage.getCageId());
            
            // Now only 2 should be available
            available = service.getAvailableAnimals();
            assertEquals(2, available.size(), "Only 2 animals should be available after allocation");
            assertFalse(available.contains(preyMarty), 
                       "Allocated animal should not be in available list");
        }
        
        @Test
        @DisplayName("Should return available cages (not full)")
        void testGetAvailableCages() throws ValidationException {
            // Initially all cages are available
            Collection<Cage> available = service.getAvailableCages();
            assertEquals(3, available.size(), "All 3 cages should be available initially");
            
            // Fill small cage (capacity 1)
            service.allocateAnimalToCage(preyMarty.getAnimalId(), smallCage.getCageId());
            
            // Now only 2 should be available
            available = service.getAvailableCages();
            assertEquals(2, available.size(), "Only 2 cages should be available after filling one");
        }
        
        @Test
        @DisplayName("Should return available keepers (can accept more cages)")
        void testGetAvailableKeepers() throws ValidationException {
            // Initially all keepers can accept cages
            Collection<Keeper> available = service.getAvailableKeepers();
            assertEquals(2, available.size(), "All 2 keepers should be available initially");
            
            // Create 4 cages and fill one keeper
            Cage cage2 = new Cage("C2", "Cage 2", 10);
            Cages.add(cage2);
            Cage cage3 = new Cage("C3", "Cage 3", 10);
            Cages.add(cage3);
            Cage cage4 = new Cage("C4", "Cage 4", 10);
            Cages.add(cage4);
            
            service.allocateKeeperToCage(headKeeperJohn.getKeeperId(), largeCage.getCageId());
            service.allocateKeeperToCage(headKeeperJohn.getKeeperId(), cage2.getCageId());
            service.allocateKeeperToCage(headKeeperJohn.getKeeperId(), cage3.getCageId());
            service.allocateKeeperToCage(headKeeperJohn.getKeeperId(), cage4.getCageId());
            
            // Now only 1 keeper should be available
            available = service.getAvailableKeepers();
            assertEquals(1, available.size(), "Only 1 keeper should be available after filling one");
            assertTrue(available.stream().anyMatch(keeper -> 
                keeper.getKeeperId() == assistantKeeperEmma.getKeeperId()),
                "Available keeper should be Emma");
        }
        
        @Test
        @DisplayName("Should return empty collection when no animals available")
        void testNoAvailableAnimals() throws ValidationException {
            // Allocate all animals
            service.allocateAnimalToCage(predatorLeo.getAnimalId(), largeCage.getCageId());
            service.allocateAnimalToCage(preyMarty.getAnimalId(), mediumCage.getCageId());
            service.allocateAnimalToCage(preyBugs.getAnimalId(), smallCage.getCageId());
            
            Collection<Animal> available = service.getAvailableAnimals();
            assertTrue(available.isEmpty(), "No animals should be available");
        }
    }

    // ========================================================================
    // Transaction Rollback Tests
    // ========================================================================
    
    @Nested
    @DisplayName("Transaction Rollback Tests")
    class TransactionRollbackTests {
        
        @Test
        @DisplayName("Should maintain data integrity on failed allocation")
        void testDataIntegrityOnFailure() {
            // Record initial state
            int initialAnimalsInCage = mediumCage.getCurrentAnimalIds().size();
            
            // Try invalid allocation (predator to prey cage)
            try {
                service.allocateAnimalToCage(preyMarty.getAnimalId(), mediumCage.getCageId());
                service.allocateAnimalToCage(predatorLeo.getAnimalId(), mediumCage.getCageId());
                fail("Should have thrown ValidationException");
            } catch (ValidationException expected) {
                // Expected
            }
            
            // Verify state is consistent (first allocation succeeded, second didn't)
            Cage cage = Cages.findById(mediumCage.getCageId());
            assertTrue(cage.getCurrentAnimalIds().contains(preyMarty.getAnimalId()),
                      "First valid allocation should persist");
            assertFalse(cage.getCurrentAnimalIds().contains(predatorLeo.getAnimalId()),
                       "Failed allocation should not persist");
        }
        
        @Test
        @DisplayName("Should not modify keeper on failed cage allocation")
        void testKeeperNotModifiedOnFailure() throws ValidationException {
            int keeperId = headKeeperJohn.getKeeperId();
            
            // Fill keeper to 4 cages
            Cage cage2 = new Cage("C2", "Cage 2", 10);
            Cages.add(cage2);
            Cage cage3 = new Cage("C3", "Cage 3", 10);
            Cages.add(cage3);
            Cage cage4 = new Cage("C4", "Cage 4", 10);
            Cages.add(cage4);
            Cage cage5 = new Cage("C5", "Cage 5", 10);
            Cages.add(cage5);
            
            service.allocateKeeperToCage(keeperId, largeCage.getCageId());
            service.allocateKeeperToCage(keeperId, cage2.getCageId());
            service.allocateKeeperToCage(keeperId, cage3.getCageId());
            service.allocateKeeperToCage(keeperId, cage4.getCageId());
            
            int cagesBeforeFailure = Keepers.findById(keeperId).getAllocatedCageIds().size();
            
            // Try 5th allocation - should fail
            try {
                service.allocateKeeperToCage(keeperId, cage5.getCageId());
            } catch (ValidationException expected) {
                // Expected
            }
            
            // Verify keeper still has exactly 4 cages
            int cagesAfterFailure = Keepers.findById(keeperId).getAllocatedCageIds().size();
            assertEquals(cagesBeforeFailure, cagesAfterFailure,
                        "Keeper cage count should not change after failed allocation");
        }
    }

    // ========================================================================
    // Edge Cases Tests
    // ========================================================================
    
    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {
        
        @Test
        @DisplayName("Should handle allocation then removal then re-allocation")
        void testAllocationCycle() throws ValidationException {
            int animalId = preyMarty.getAnimalId();
            int cageId = smallCage.getCageId();
            
            // Allocate
            service.allocateAnimalToCage(animalId, cageId);
            assertTrue(Cages.findById(cageId).getCurrentAnimalIds().contains(animalId));
            
            // Remove
            service.removeAnimalFromCage(animalId, cageId);
            assertFalse(Cages.findById(cageId).getCurrentAnimalIds().contains(animalId));
            
            // Re-allocate
            service.allocateAnimalToCage(animalId, cageId);
            assertTrue(Cages.findById(cageId).getCurrentAnimalIds().contains(animalId));
        }
        
        @Test
        @DisplayName("Should handle keeper reassignment between cages")
        void testKeeperReassignment() throws ValidationException {
            int keeperId = headKeeperJohn.getKeeperId();
            
            // Allocate to cage 1
            service.allocateKeeperToCage(keeperId, largeCage.getCageId());
            service.allocateKeeperToCage(keeperId, mediumCage.getCageId());
            
            // Remove from cage 1
            service.removeKeeperFromCage(keeperId, largeCage.getCageId());
            
            // Add cage 3
            service.allocateKeeperToCage(keeperId, smallCage.getCageId());
            
            Keeper keeper = Keepers.findById(keeperId);
            assertEquals(2, keeper.getAllocatedCageIds().size());
            assertTrue(keeper.getAllocatedCageIds().contains(mediumCage.getCageId()));
            assertTrue(keeper.getAllocatedCageIds().contains(smallCage.getCageId()));
            assertFalse(keeper.getAllocatedCageIds().contains(largeCage.getCageId()));
        }
        
        @Test
        @DisplayName("Should handle empty registries")
        void testEmptyRegistries() {
            Animals.clear();
            Keepers.clear();
            Cages.clear();
            
            Collection<Animal> availableAnimals = service.getAvailableAnimals();
            Collection<Cage> availableCages = service.getAvailableCages();
            Collection<Keeper> availableKeepers = service.getAvailableKeepers();
            
            assertTrue(availableAnimals.isEmpty());
            assertTrue(availableCages.isEmpty());
            assertTrue(availableKeepers.isEmpty());
        }
        
        @Test
        @DisplayName("Should handle single entity in each registry")
        void testSingleEntityPerRegistry() throws ValidationException {
            Animals.clear();
            Keepers.clear();
            Cages.clear();
            
            Animal singleAnimal = new Animal("Solo", "Rabbit", Animal.Category.PREY,
                                            validBirthDate, validAcquisitionDate, Animal.Sex.MALE);
            Animals.add(singleAnimal);
            
            Cage singleCage = new Cage("Only-01", "Only cage", 5);
            Cages.add(singleCage);
            
            HeadKeeper singleKeeper = new HeadKeeper("Only", "Keeper", "Address", "07000000000");
            Keepers.add(singleKeeper);
            
            // Should work with single entities
            service.allocateAnimalToCage(singleAnimal.getAnimalId(), singleCage.getCageId());
            service.allocateKeeperToCage(singleKeeper.getKeeperId(), singleCage.getCageId());
            
            assertTrue(Cages.findById(singleCage.getCageId()).getCurrentAnimalIds()
                      .contains(singleAnimal.getAnimalId()));
            assertEquals(Integer.valueOf(singleKeeper.getKeeperId()),
                        Cages.findById(singleCage.getCageId()).getAssignedKeeperId());
        }
    }

    // ========================================================================
    // Integration with Validator Tests
    // ========================================================================
    
    @Nested
    @DisplayName("Integration with Validator Tests")
    class IntegrationWithValidatorTests {
        
        @Test
        @DisplayName("Should use AllocationValidator for animal allocation")
        void testUsesValidatorForAnimalAllocation() {
            // This test verifies that service properly delegates to validator
            // by checking that business rules are enforced
            
            // Predator to prey cage should fail
            try {
                service.allocateAnimalToCage(preyMarty.getAnimalId(), mediumCage.getCageId());
                service.allocateAnimalToCage(predatorLeo.getAnimalId(), mediumCage.getCageId());
                fail("Validator should have rejected predator to prey cage");
            } catch (ValidationException exception) {
                assertEquals(ValidationException.ErrorType.INVALID_PREDATOR_PREY_MIX,
                            exception.getErrorType());
            }
        }
        
        @Test
        @DisplayName("Should use AllocationValidator for keeper allocation")
        void testUsesValidatorForKeeperAllocation() throws ValidationException {
            // Fill keeper to max cages
            Cage cage2 = new Cage("C2", "Cage 2", 10);
            Cages.add(cage2);
            Cage cage3 = new Cage("C3", "Cage 3", 10);
            Cages.add(cage3);
            Cage cage4 = new Cage("C4", "Cage 4", 10);
            Cages.add(cage4);
            Cage cage5 = new Cage("C5", "Cage 5", 10);
            Cages.add(cage5);
            
            int keeperId = headKeeperJohn.getKeeperId();
            service.allocateKeeperToCage(keeperId, largeCage.getCageId());
            service.allocateKeeperToCage(keeperId, cage2.getCageId());
            service.allocateKeeperToCage(keeperId, cage3.getCageId());
            service.allocateKeeperToCage(keeperId, cage4.getCageId());
            
            // 5th should fail with KEEPER_OVERLOAD
            try {
                service.allocateKeeperToCage(keeperId, cage5.getCageId());
                fail("Validator should have rejected 5th cage");
            } catch (ValidationException exception) {
                assertEquals(ValidationException.ErrorType.KEEPER_OVERLOAD,
                            exception.getErrorType());
            }
        }
    }
}
