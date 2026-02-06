package com.conservation.model;

import com.conservation.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for the Cage entity class.
 * Tests constructor validation, capacity management, animal addition/removal,
 * keeper assignment, and status methods.
 */
@DisplayName("Cage Entity Tests")
class CageTest {

    // ==================== Constructor Tests ====================

    @Nested
    @DisplayName("Constructor Validation Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create valid Cage with all correct parameters")
        void shouldCreateValidCage() {
            Cage cage = new Cage(
                    "Large-01",
                    "Large predator cage",
                    10
            );

            assertNotNull(cage);
            assertEquals("Large-01", cage.getCageNumber());
            assertEquals("Large predator cage", cage.getDescription());
            assertEquals(10, cage.getAnimalCapacity());
            assertEquals(0, cage.getCageId()); // ID not set until added to registry
            assertTrue(cage.getCurrentAnimalIds().isEmpty());
            assertNull(cage.getAssignedKeeperId());
        }

        @Test
        @DisplayName("Should throw ValidationException when cage number is null")
        void shouldThrowExceptionWhenCageNumberIsNull() {
            ValidationException exception = assertThrows(ValidationException.class, () ->
                    new Cage(null, "Description", 10)
            );
            assertTrue(exception.getMessage().toLowerCase().contains("cage number"));
        }

        @Test
        @DisplayName("Should throw ValidationException when cage number is empty")
        void shouldThrowExceptionWhenCageNumberIsEmpty() {
            ValidationException exception = assertThrows(ValidationException.class, () ->
                    new Cage("", "Description", 10)
            );
            assertTrue(exception.getMessage().toLowerCase().contains("cage number"));
        }

        @Test
        @DisplayName("Should throw ValidationException when description is null")
        void shouldThrowExceptionWhenDescriptionIsNull() {
            ValidationException exception = assertThrows(ValidationException.class, () ->
                    new Cage("Large-01", null, 10)
            );
            assertTrue(exception.getMessage().toLowerCase().contains("description"));
        }

        @Test
        @DisplayName("Should throw ValidationException when capacity is zero")
        void shouldThrowExceptionWhenCapacityIsZero() {
            ValidationException exception = assertThrows(ValidationException.class, () ->
                    new Cage("Large-01", "Description", 0)
            );
            assertTrue(exception.getMessage().toLowerCase().contains("capacity"));
        }

        @Test
        @DisplayName("Should throw ValidationException when capacity is negative")
        void shouldThrowExceptionWhenCapacityIsNegative() {
            ValidationException exception = assertThrows(ValidationException.class, () ->
                    new Cage("Large-01", "Description", -5)
            );
            assertTrue(exception.getMessage().toLowerCase().contains("capacity"));
        }

        @Test
        @DisplayName("Should create small cage with capacity of 1")
        void shouldCreateSmallCageWithCapacityOfOne() {
            Cage smallCage = new Cage("Small-01", "Small cage", 1);
            assertEquals(1, smallCage.getAnimalCapacity());
        }

        @Test
        @DisplayName("Should create large cage with high capacity")
        void shouldCreateLargeCageWithHighCapacity() {
            Cage largeCage = new Cage("Large-01", "Large cage", 50);
            assertEquals(50, largeCage.getAnimalCapacity());
        }
    }

    // ==================== Animal Management Tests ====================

    @Nested
    @DisplayName("Animal Addition and Removal Tests")
    class AnimalManagementTests {

        private Cage testCage;

        @BeforeEach
        void setUpCage() {
            testCage = new Cage("Test-01", "Test cage", 5);
        }

        @Test
        @DisplayName("Should add animal ID to cage")
        void shouldAddAnimalIdToCage() {
            testCage.addAnimal(1);

            List<Integer> animals = testCage.getCurrentAnimalIds();
            assertEquals(1, animals.size());
            assertTrue(animals.contains(1));
        }

        @Test
        @DisplayName("Should add multiple animal IDs")
        void shouldAddMultipleAnimalIds() {
            testCage.addAnimal(1);
            testCage.addAnimal(2);
            testCage.addAnimal(3);

            List<Integer> animals = testCage.getCurrentAnimalIds();
            assertEquals(3, animals.size());
            assertTrue(animals.contains(1));
            assertTrue(animals.contains(2));
            assertTrue(animals.contains(3));
        }

        @Test
        @DisplayName("Should remove animal ID from cage")
        void shouldRemoveAnimalIdFromCage() {
            testCage.addAnimal(1);
            testCage.addAnimal(2);

            boolean removed = testCage.removeAnimal(1);

            assertTrue(removed);
            List<Integer> animals = testCage.getCurrentAnimalIds();
            assertEquals(1, animals.size());
            assertFalse(animals.contains(1));
            assertTrue(animals.contains(2));
        }

        @Test
        @DisplayName("Should return false when removing non-existent animal ID")
        void shouldReturnFalseWhenRemovingNonExistentAnimalId() {
            testCage.addAnimal(1);

            boolean removed = testCage.removeAnimal(999);

            assertFalse(removed);
            assertEquals(1, testCage.getCurrentAnimalIds().size());
        }

        @Test
        @DisplayName("Should not allow duplicate animal IDs")
        void shouldNotAllowDuplicateAnimalIds() {
            testCage.addAnimal(1);
            testCage.addAnimal(1);
            testCage.addAnimal(1);

            // Should only be added once
            assertEquals(1, testCage.getCurrentAnimalIds().size());
        }

        @Test
        @DisplayName("Should return defensive copy of animal IDs list")
        void shouldReturnDefensiveCopyOfAnimalIdsList() {
            testCage.addAnimal(1);

            List<Integer> animals1 = testCage.getCurrentAnimalIds();
            List<Integer> animals2 = testCage.getCurrentAnimalIds();

            // Should be different instances
            assertNotSame(animals1, animals2);

            // But with same content
            assertEquals(animals1, animals2);
        }

        @Test
        @DisplayName("Should not modify internal list when modifying returned list")
        void shouldNotModifyInternalListWhenModifyingReturnedList() {
            testCage.addAnimal(1);

            List<Integer> animals = testCage.getCurrentAnimalIds();
            animals.add(999); // Try to modify returned list

            // Internal list should not be affected
            assertEquals(1, testCage.getCurrentAnimalIds().size());
            assertFalse(testCage.getCurrentAnimalIds().contains(999));
        }
    }

    // ==================== Capacity Management Tests ====================

    @Nested
    @DisplayName("Capacity Management Tests")
    class CapacityManagementTests {

        @Test
        @DisplayName("Should be empty when no animals are added")
        void shouldBeEmptyWhenNoAnimalsAreAdded() {
            Cage cage = new Cage("Test-01", "Test cage", 5);
            assertTrue(cage.isEmpty());
        }

        @Test
        @DisplayName("Should not be empty after adding one animal")
        void shouldNotBeEmptyAfterAddingOneAnimal() {
            Cage cage = new Cage("Test-01", "Test cage", 5);
            cage.addAnimal(1);
            assertFalse(cage.isEmpty());
        }

        @Test
        @DisplayName("Should not be full when cage has space")
        void shouldNotBeFullWhenCageHasSpace() {
            Cage cage = new Cage("Test-01", "Test cage", 5);
            cage.addAnimal(1);
            cage.addAnimal(2);
            assertFalse(cage.isFull());
        }

        @Test
        @DisplayName("Should be full when capacity is reached")
        void shouldBeFullWhenCapacityIsReached() {
            Cage cage = new Cage("Test-01", "Test cage", 3);
            cage.addAnimal(1);
            cage.addAnimal(2);
            cage.addAnimal(3);
            assertTrue(cage.isFull());
        }

        @Test
        @DisplayName("Should return correct available space")
        void shouldReturnCorrectAvailableSpace() {
            Cage cage = new Cage("Test-01", "Test cage", 10);
            assertEquals(10, cage.getAvailableSpace());

            cage.addAnimal(1);
            assertEquals(9, cage.getAvailableSpace());

            cage.addAnimal(2);
            cage.addAnimal(3);
            assertEquals(7, cage.getAvailableSpace());
        }

        @Test
        @DisplayName("Should return zero available space when full")
        void shouldReturnZeroAvailableSpaceWhenFull() {
            Cage cage = new Cage("Test-01", "Test cage", 2);
            cage.addAnimal(1);
            cage.addAnimal(2);

            assertEquals(0, cage.getAvailableSpace());
            assertTrue(cage.isFull());
        }

        @Test
        @DisplayName("Should increase available space after removing animal")
        void shouldIncreaseAvailableSpaceAfterRemovingAnimal() {
            Cage cage = new Cage("Test-01", "Test cage", 5);
            cage.addAnimal(1);
            cage.addAnimal(2);
            cage.addAnimal(3);

            assertEquals(2, cage.getAvailableSpace());

            cage.removeAnimal(1);

            assertEquals(3, cage.getAvailableSpace());
        }

        @Test
        @DisplayName("Should handle single capacity cage")
        void shouldHandleSingleCapacityCage() {
            Cage cage = new Cage("Small-01", "Small cage", 1);

            assertTrue(cage.isEmpty());
            assertEquals(1, cage.getAvailableSpace());

            cage.addAnimal(1);

            assertFalse(cage.isEmpty());
            assertTrue(cage.isFull());
            assertEquals(0, cage.getAvailableSpace());
        }
    }

    // ==================== Status and Information Tests ====================

    @Nested
    @DisplayName("Status and Information Tests")
    class StatusAndInformationTests {

        @Test
        @DisplayName("Should return correct occupancy info format")
        void shouldReturnCorrectOccupancyInfoFormat() {
            Cage cage = new Cage("Test-01", "Test cage", 10);
            cage.addAnimal(1);
            cage.addAnimal(2);
            cage.addAnimal(3);

            String occupancyInfo = cage.getOccupancyInfo();

            assertNotNull(occupancyInfo);
            assertTrue(occupancyInfo.contains("3"));
            assertTrue(occupancyInfo.contains("10"));
        }

        @Test
        @DisplayName("Should return EMPTY status when no animals")
        void shouldReturnEmptyStatusWhenNoAnimals() {
            Cage cage = new Cage("Test-01", "Test cage", 10);

            String status = cage.getStatus();

            assertNotNull(status);
            assertTrue(status.equalsIgnoreCase("EMPTY") || 
                      status.toLowerCase().contains("empty"));
        }

        @Test
        @DisplayName("Should return FULL status when at capacity")
        void shouldReturnFullStatusWhenAtCapacity() {
            Cage cage = new Cage("Test-01", "Test cage", 3);
            cage.addAnimal(1);
            cage.addAnimal(2);
            cage.addAnimal(3);

            String status = cage.getStatus();

            assertNotNull(status);
            assertTrue(status.equalsIgnoreCase("FULL") || 
                      status.toLowerCase().contains("full"));
        }

        @Test
        @DisplayName("Should return AVAILABLE status when has space")
        void shouldReturnAvailableStatusWhenHasSpace() {
            Cage cage = new Cage("Test-01", "Test cage", 10);
            cage.addAnimal(1);
            cage.addAnimal(2);

            String status = cage.getStatus();

            assertNotNull(status);
            assertTrue(status.equalsIgnoreCase("AVAILABLE") || 
                      status.toLowerCase().contains("available"));
        }
    }

    // ==================== Keeper Assignment Tests ====================

    @Nested
    @DisplayName("Keeper Assignment Tests")
    class KeeperAssignmentTests {

        private Cage testCage;

        @BeforeEach
        void setUpCage() {
            testCage = new Cage("Test-01", "Test cage", 5);
        }

        @Test
        @DisplayName("Should have no assigned keeper initially")
        void shouldHaveNoAssignedKeeperInitially() {
            assertNull(testCage.getAssignedKeeperId());
        }

        @Test
        @DisplayName("Should assign keeper ID to cage")
        void shouldAssignKeeperIdToCage() {
            testCage.setAssignedKeeperId(1);
            assertEquals(1, testCage.getAssignedKeeperId());
        }

        @Test
        @DisplayName("Should update keeper ID when reassigning")
        void shouldUpdateKeeperIdWhenReassigning() {
            testCage.setAssignedKeeperId(1);
            testCage.setAssignedKeeperId(2);
            assertEquals(2, testCage.getAssignedKeeperId());
        }

        @Test
        @DisplayName("Should allow setting keeper ID to null")
        void shouldAllowSettingKeeperIdToNull() {
            testCage.setAssignedKeeperId(1);
            testCage.setAssignedKeeperId(null);
            assertNull(testCage.getAssignedKeeperId());
        }

        @Test
        @DisplayName("Should maintain keeper assignment after adding animals")
        void shouldMaintainKeeperAssignmentAfterAddingAnimals() {
            testCage.setAssignedKeeperId(1);
            testCage.addAnimal(1);
            testCage.addAnimal(2);

            assertEquals(1, testCage.getAssignedKeeperId());
        }
    }

    // ==================== Setter Validation Tests ====================

    @Nested
    @DisplayName("Setter Validation Tests")
    class SetterValidationTests {

        private Cage testCage;

        @BeforeEach
        void setUpCage() {
            testCage = new Cage("Test-01", "Test cage", 5);
        }

        @Test
        @DisplayName("Should update cage number with valid value")
        void shouldUpdateCageNumberWithValidValue() {
            testCage.setCageNumber("Large-02");
            assertEquals("Large-02", testCage.getCageNumber());
        }

        @Test
        @DisplayName("Should throw ValidationException when setting null cage number")
        void shouldThrowExceptionWhenSettingNullCageNumber() {
            assertThrows(ValidationException.class, () ->
                    testCage.setCageNumber(null)
            );
        }

        @Test
        @DisplayName("Should throw ValidationException when setting empty cage number")
        void shouldThrowExceptionWhenSettingEmptyCageNumber() {
            assertThrows(ValidationException.class, () ->
                    testCage.setCageNumber("")
            );
        }

        @Test
        @DisplayName("Should update description with valid value")
        void shouldUpdateDescriptionWithValidValue() {
            testCage.setDescription("Updated description");
            assertEquals("Updated description", testCage.getDescription());
        }

        @Test
        @DisplayName("Should throw ValidationException when setting null description")
        void shouldThrowExceptionWhenSettingNullDescription() {
            assertThrows(ValidationException.class, () ->
                    testCage.setDescription(null)
            );
        }

        @Test
        @DisplayName("Should update capacity with valid value")
        void shouldUpdateCapacityWithValidValue() {
            testCage.setAnimalCapacity(15);
            assertEquals(15, testCage.getAnimalCapacity());
        }

        @Test
        @DisplayName("Should throw ValidationException when setting zero capacity")
        void shouldThrowExceptionWhenSettingZeroCapacity() {
            assertThrows(ValidationException.class, () ->
                    testCage.setAnimalCapacity(0)
            );
        }

        @Test
        @DisplayName("Should throw ValidationException when setting negative capacity")
        void shouldThrowExceptionWhenSettingNegativeCapacity() {
            assertThrows(ValidationException.class, () ->
                    testCage.setAnimalCapacity(-5)
            );
        }
    }

    // ==================== ID Management Tests ====================

    @Nested
    @DisplayName("ID Management Tests")
    class IdManagementTests {

        @Test
        @DisplayName("Should have ID of 0 when first created")
        void shouldHaveZeroIdWhenCreated() {
            Cage cage = new Cage("Test-01", "Test cage", 5);
            assertEquals(0, cage.getCageId());
        }

        @Test
        @DisplayName("Should update ID when set by registry")
        void shouldUpdateIdWhenSetByRegistry() {
            Cage cage = new Cage("Test-01", "Test cage", 5);
            cage.setCageId(42);
            assertEquals(42, cage.getCageId());
        }

        @Test
        @DisplayName("Should allow ID to be updated multiple times")
        void shouldAllowIdToBeUpdatedMultipleTimes() {
            Cage cage = new Cage("Test-01", "Test cage", 5);
            cage.setCageId(1);
            cage.setCageId(2);
            cage.setCageId(3);
            assertEquals(3, cage.getCageId());
        }
    }

    // ==================== Edge Cases ====================

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle very large capacity")
        void shouldHandleVeryLargeCapacity() {
            Cage largeCage = new Cage("Mega-01", "Huge enclosure", 1000);
            assertEquals(1000, largeCage.getAnimalCapacity());
            assertTrue(largeCage.isEmpty());
        }

        @Test
        @DisplayName("Should handle long cage number")
        void shouldHandleLongCageNumber() {
            String longNumber = "VeryLongCageNumber-" + "1".repeat(50);
            Cage cage = new Cage(longNumber, "Description", 5);
            assertEquals(longNumber, cage.getCageNumber());
        }

        @Test
        @DisplayName("Should handle long description")
        void shouldHandleLongDescription() {
            String longDescription = "This is a very long description. ".repeat(20);
            Cage cage = new Cage("Test-01", longDescription, 5);
            assertEquals(longDescription, cage.getDescription());
        }

        @Test
        @DisplayName("Should handle adding and removing animals repeatedly")
        void shouldHandleAddingAndRemovingAnimalsRepeatedly() {
            Cage cage = new Cage("Test-01", "Test cage", 3);

            cage.addAnimal(1);
            cage.addAnimal(2);
            assertEquals(2, cage.getCurrentAnimalIds().size());

            cage.removeAnimal(1);
            assertEquals(1, cage.getCurrentAnimalIds().size());

            cage.addAnimal(3);
            cage.addAnimal(4);
            assertEquals(3, cage.getCurrentAnimalIds().size());

            cage.removeAnimal(2);
            cage.removeAnimal(3);
            assertEquals(1, cage.getCurrentAnimalIds().size());

            assertTrue(cage.getCurrentAnimalIds().contains(4));
        }

        @Test
        @DisplayName("Should handle filling cage to capacity and then emptying")
        void shouldHandleFillingCageToCapacityAndThenEmptying() {
            Cage cage = new Cage("Test-01", "Test cage", 3);

            cage.addAnimal(1);
            cage.addAnimal(2);
            cage.addAnimal(3);

            assertTrue(cage.isFull());

            cage.removeAnimal(1);
            cage.removeAnimal(2);
            cage.removeAnimal(3);

            assertTrue(cage.isEmpty());
            assertEquals(3, cage.getAvailableSpace());
        }

        @Test
        @DisplayName("Should handle capacity reduction with existing animals")
        void shouldHandleCapacityReductionWithExistingAnimals() {
            Cage cage = new Cage("Test-01", "Test cage", 10);
            cage.addAnimal(1);
            cage.addAnimal(2);
            cage.addAnimal(3);

            // Reduce capacity below current occupancy
            cage.setAnimalCapacity(5);

            assertEquals(5, cage.getAnimalCapacity());
            assertEquals(3, cage.getCurrentAnimalIds().size());
            assertEquals(2, cage.getAvailableSpace());
        }

        @Test
        @DisplayName("Should maintain animal list integrity after multiple operations")
        void shouldMaintainAnimalListIntegrityAfterMultipleOperations() {
            Cage cage = new Cage("Test-01", "Test cage", 10);

            // Add animals
            for (int animalId = 1; animalId <= 5; animalId++) {
                cage.addAnimal(animalId);
            }

            assertEquals(5, cage.getCurrentAnimalIds().size());

            // Remove some
            cage.removeAnimal(2);
            cage.removeAnimal(4);

            assertEquals(3, cage.getCurrentAnimalIds().size());
            assertTrue(cage.getCurrentAnimalIds().contains(1));
            assertTrue(cage.getCurrentAnimalIds().contains(3));
            assertTrue(cage.getCurrentAnimalIds().contains(5));
            assertFalse(cage.getCurrentAnimalIds().contains(2));
            assertFalse(cage.getCurrentAnimalIds().contains(4));
        }
    }
}
