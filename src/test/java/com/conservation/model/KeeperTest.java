package com.conservation.model;

import com.conservation.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for the Keeper hierarchy.
 * Tests abstract Keeper class behaviour through HeadKeeper and AssistantKeeper,
 * including inheritance relationships, polymorphism, and specific permissions.
 */
@DisplayName("Keeper Hierarchy Tests")
class KeeperTest {

    // ==================== Abstract Keeper Class Tests ====================

    @Nested
    @DisplayName("Abstract Keeper Base Class Tests")
    class AbstractKeeperTests {

        @Test
        @DisplayName("Should create HeadKeeper with valid parameters")
        void shouldCreateHeadKeeperWithValidParameters() {
            HeadKeeper headKeeper = new HeadKeeper(
                    "John",
                    "Smith",
                    "123 Main St, Glasgow",
                    "07123456789"
            );

            assertNotNull(headKeeper);
            assertEquals("John", headKeeper.getFirstName());
            assertEquals("Smith", headKeeper.getSurname());
            assertEquals("123 Main St, Glasgow", headKeeper.getAddress());
            assertEquals("07123456789", headKeeper.getContactNumber());
            assertEquals(Keeper.Position.HEAD_KEEPER, headKeeper.getPosition());
        }

        @Test
        @DisplayName("Should create AssistantKeeper with valid parameters")
        void shouldCreateAssistantKeeperWithValidParameters() {
            AssistantKeeper assistant = new AssistantKeeper(
                    "Jane",
                    "Doe",
                    "456 High St, Glasgow",
                    "07987654321"
            );

            assertNotNull(assistant);
            assertEquals("Jane", assistant.getFirstName());
            assertEquals("Doe", assistant.getSurname());
            assertEquals("456 High St, Glasgow", assistant.getAddress());
            assertEquals("07987654321", assistant.getContactNumber());
            assertEquals(Keeper.Position.ASSISTANT_KEEPER, assistant.getPosition());
        }

        @Test
        @DisplayName("Should have empty allocated cages list when first created")
        void shouldHaveEmptyAllocatedCagesListWhenCreated() {
            HeadKeeper keeper = new HeadKeeper("John", "Smith", "Address", "Phone");
            
            assertTrue(keeper.getAllocatedCageIds().isEmpty());
            assertEquals(0, keeper.getAllocatedCageIds().size());
        }

        @Test
        @DisplayName("Should throw ValidationException when first name is null")
        void shouldThrowExceptionWhenFirstNameIsNull() {
            assertThrows(ValidationException.class, () ->
                    new HeadKeeper(null, "Smith", "Address", "Phone")
            );
        }

        @Test
        @DisplayName("Should throw ValidationException when first name is empty")
        void shouldThrowExceptionWhenFirstNameIsEmpty() {
            assertThrows(ValidationException.class, () ->
                    new HeadKeeper("", "Smith", "Address", "Phone")
            );
        }

        @Test
        @DisplayName("Should throw ValidationException when surname is null")
        void shouldThrowExceptionWhenSurnameIsNull() {
            assertThrows(ValidationException.class, () ->
                    new HeadKeeper("John", null, "Address", "Phone")
            );
        }

        @Test
        @DisplayName("Should throw ValidationException when address is null")
        void shouldThrowExceptionWhenAddressIsNull() {
            assertThrows(ValidationException.class, () ->
                    new HeadKeeper("John", "Smith", null, "Phone")
            );
        }

        @Test
        @DisplayName("Should throw ValidationException when contact number is null")
        void shouldThrowExceptionWhenContactNumberIsNull() {
            assertThrows(ValidationException.class, () ->
                    new HeadKeeper("John", "Smith", "Address", null)
            );
        }
    }

    // ==================== Setter Validation Tests ====================

    @Nested
    @DisplayName("Setter Validation Tests")
    class SetterValidationTests {

        private HeadKeeper testKeeper;

        @BeforeEach
        void setUpKeeper() {
            testKeeper = new HeadKeeper("John", "Smith", "Address", "Phone");
        }

        @Test
        @DisplayName("Should update first name with valid value")
        void shouldUpdateFirstNameWithValidValue() {
            testKeeper.setFirstName("Michael");
            assertEquals("Michael", testKeeper.getFirstName());
        }

        @Test
        @DisplayName("Should throw ValidationException when setting null first name")
        void shouldThrowExceptionWhenSettingNullFirstName() {
            assertThrows(ValidationException.class, () ->
                    testKeeper.setFirstName(null)
            );
        }

        @Test
        @DisplayName("Should update surname with valid value")
        void shouldUpdateSurnameWithValidValue() {
            testKeeper.setSurname("Johnson");
            assertEquals("Johnson", testKeeper.getSurname());
        }

        @Test
        @DisplayName("Should update address with valid value")
        void shouldUpdateAddressWithValidValue() {
            testKeeper.setAddress("789 New St, Glasgow");
            assertEquals("789 New St, Glasgow", testKeeper.getAddress());
        }

        @Test
        @DisplayName("Should update contact number with valid value")
        void shouldUpdateContactNumberWithValidValue() {
            testKeeper.setContactNumber("07111222333");
            assertEquals("07111222333", testKeeper.getContactNumber());
        }
    }

    // ==================== Allocated Cages Management Tests ====================

    @Nested
    @DisplayName("Allocated Cages Management Tests")
    class AllocatedCagesManagementTests {

        private HeadKeeper testKeeper;

        @BeforeEach
        void setUpKeeper() {
            testKeeper = new HeadKeeper("John", "Smith", "Address", "Phone");
        }

        @Test
        @DisplayName("Should add cage ID to allocated cages list")
        void shouldAddCageIdToAllocatedCagesList() {
            testKeeper.allocateCage(1);
            
            List<Integer> allocatedCages = testKeeper.getAllocatedCageIds();
            assertEquals(1, allocatedCages.size());
            assertTrue(allocatedCages.contains(1));
        }

        @Test
        @DisplayName("Should add multiple cage IDs")
        void shouldAddMultipleCageIds() {
            testKeeper.allocateCage(1);
            testKeeper.allocateCage(2);
            testKeeper.allocateCage(3);
            
            List<Integer> allocatedCages = testKeeper.getAllocatedCageIds();
            assertEquals(3, allocatedCages.size());
            assertTrue(allocatedCages.contains(1));
            assertTrue(allocatedCages.contains(2));
            assertTrue(allocatedCages.contains(3));
        }

        @Test
        @DisplayName("Should remove cage ID from allocated cages list")
        void shouldRemoveCageIdFromAllocatedCagesList() {
            testKeeper.allocateCage(1);
            testKeeper.allocateCage(2);
            
            boolean removed = testKeeper.removeCage(1);
            
            assertTrue(removed);
            List<Integer> allocatedCages = testKeeper.getAllocatedCageIds();
            assertEquals(1, allocatedCages.size());
            assertFalse(allocatedCages.contains(1));
            assertTrue(allocatedCages.contains(2));
        }

        @Test
        @DisplayName("Should return false when removing non-existent cage ID")
        void shouldReturnFalseWhenRemovingNonExistentCageId() {
            testKeeper.allocateCage(1);
            
            boolean removed = testKeeper.removeCage(999);
            
            assertFalse(removed);
            assertEquals(1, testKeeper.getAllocatedCageIds().size());
        }

        @Test
        @DisplayName("Should not allow duplicate cage IDs")
        void shouldNotAllowDuplicateCageIds() {
            testKeeper.allocateCage(1);
            testKeeper.allocateCage(1);
            testKeeper.allocateCage(1);
            
            // Should only be added once
            assertEquals(1, testKeeper.getAllocatedCageIds().size());
        }

        @Test
        @DisplayName("Should return defensive copy of allocated cages list")
        void shouldReturnDefensiveCopyOfAllocatedCagesList() {
            testKeeper.allocateCage(1);
            
            List<Integer> allocatedCages1 = testKeeper.getAllocatedCageIds();
            List<Integer> allocatedCages2 = testKeeper.getAllocatedCageIds();
            
            // Should be different instances
            assertNotSame(allocatedCages1, allocatedCages2);
            
            // But with same content
            assertEquals(allocatedCages1, allocatedCages2);
        }

        @Test
        @DisplayName("Should not modify internal list when modifying returned list")
        void shouldNotModifyInternalListWhenModifyingReturnedList() {
            testKeeper.allocateCage(1);
            
            List<Integer> allocatedCages = testKeeper.getAllocatedCageIds();
            allocatedCages.add(999); // Try to modify returned list
            
            // Internal list should not be affected
            assertEquals(1, testKeeper.getAllocatedCageIds().size());
            assertFalse(testKeeper.getAllocatedCageIds().contains(999));
        }
    }

    // ==================== Can Accept More Cages Tests ====================

    @Nested
    @DisplayName("Can Accept More Cages Tests")
    class CanAcceptMoreCagesTests {

        @Test
        @DisplayName("Should accept more cages when keeper has 0 cages")
        void shouldAcceptMoreCagesWhenKeeperHasZeroCages() {
            HeadKeeper keeper = new HeadKeeper("John", "Smith", "Address", "Phone");
            assertTrue(keeper.canAcceptMoreCages());
        }

        @Test
        @DisplayName("Should accept more cages when keeper has 3 cages")
        void shouldAcceptMoreCagesWhenKeeperHasThreeCages() {
            HeadKeeper keeper = new HeadKeeper("John", "Smith", "Address", "Phone");
            keeper.allocateCage(1);
            keeper.allocateCage(2);
            keeper.allocateCage(3);
            
            assertTrue(keeper.canAcceptMoreCages());
        }

        @Test
        @DisplayName("Should not accept more cages when keeper has 4 cages (maximum)")
        void shouldNotAcceptMoreCagesWhenKeeperHasFourCages() {
            HeadKeeper keeper = new HeadKeeper("John", "Smith", "Address", "Phone");
            keeper.allocateCage(1);
            keeper.allocateCage(2);
            keeper.allocateCage(3);
            keeper.allocateCage(4);
            
            assertFalse(keeper.canAcceptMoreCages());
        }

        @Test
        @DisplayName("Should accept more cages after removing one cage")
        void shouldAcceptMoreCagesAfterRemovingOneCage() {
            HeadKeeper keeper = new HeadKeeper("John", "Smith", "Address", "Phone");
            keeper.allocateCage(1);
            keeper.allocateCage(2);
            keeper.allocateCage(3);
            keeper.allocateCage(4);
            
            assertFalse(keeper.canAcceptMoreCages());
            
            keeper.removeCage(1);
            
            assertTrue(keeper.canAcceptMoreCages());
        }
    }

    // ==================== HeadKeeper Specific Tests ====================

    @Nested
    @DisplayName("HeadKeeper Specific Tests")
    class HeadKeeperSpecificTests {

        @Test
        @DisplayName("Should have HEAD_KEEPER position")
        void shouldHaveHeadKeeperPosition() {
            HeadKeeper headKeeper = new HeadKeeper("John", "Smith", "Address", "Phone");
            assertEquals(Keeper.Position.HEAD_KEEPER, headKeeper.getPosition());
        }

        @Test
        @DisplayName("Should return full permissions description")
        void shouldReturnFullPermissionsDescription() {
            HeadKeeper headKeeper = new HeadKeeper("John", "Smith", "Address", "Phone");
            String permissions = headKeeper.getResponsibilities();
            
            assertNotNull(permissions);
            assertFalse(permissions.isEmpty());
            assertTrue(permissions.toLowerCase().contains("manage") || 
                      permissions.toLowerCase().contains("supervise") ||
                      permissions.toLowerCase().contains("allocate"));
        }

        @Test
        @DisplayName("Should return full title with position and name")
        void shouldReturnFullTitleWithPositionAndName() {
            HeadKeeper headKeeper = new HeadKeeper("John", "Smith", "Address", "Phone");
            String fullTitle = headKeeper.getFullTitle();
            
            assertNotNull(fullTitle);
            assertTrue(fullTitle.contains("Head Keeper"));
            assertTrue(fullTitle.contains("John") || fullTitle.contains("Smith"));
        }

        @Test
        @DisplayName("Should be able to manage maximum 4 cages")
        void shouldBeAbleToManageMaximumFourCages() {
            HeadKeeper headKeeper = new HeadKeeper("John", "Smith", "Address", "Phone");
            
            headKeeper.allocateCage(1);
            headKeeper.allocateCage(2);
            headKeeper.allocateCage(3);
            headKeeper.allocateCage(4);
            
            assertEquals(4, headKeeper.getAllocatedCageIds().size());
            assertFalse(headKeeper.canAcceptMoreCages());
        }
    }

    // ==================== AssistantKeeper Specific Tests ====================

    @Nested
    @DisplayName("AssistantKeeper Specific Tests")
    class AssistantKeeperSpecificTests {

        @Test
        @DisplayName("Should have ASSISTANT_KEEPER position")
        void shouldHaveAssistantKeeperPosition() {
            AssistantKeeper assistant = new AssistantKeeper("Jane", "Doe", "Address", "Phone");
            assertEquals(Keeper.Position.ASSISTANT_KEEPER, assistant.getPosition());
        }

        @Test
        @DisplayName("Should return limited permissions description")
        void shouldReturnLimitedPermissionsDescription() {
            AssistantKeeper assistant = new AssistantKeeper("Jane", "Doe", "Address", "Phone");
            String permissions = assistant.getResponsibilities();
            
            assertNotNull(permissions);
            assertFalse(permissions.isEmpty());
            assertTrue(permissions.toLowerCase().contains("care") || 
                      permissions.toLowerCase().contains("monitor") ||
                      permissions.toLowerCase().contains("assist"));
        }

        @Test
        @DisplayName("Should return full title with position and name")
        void shouldReturnFullTitleWithPositionAndName() {
            AssistantKeeper assistant = new AssistantKeeper("Jane", "Doe", "Address", "Phone");
            String fullTitle = assistant.getFullTitle();
            
            assertNotNull(fullTitle);
            assertTrue(fullTitle.contains("Assistant Keeper"));
            assertTrue(fullTitle.contains("Jane") || fullTitle.contains("Doe"));
        }

        @Test
        @DisplayName("Should have different permissions than HeadKeeper")
        void shouldHaveDifferentPermissionsThanHeadKeeper() {
            HeadKeeper headKeeper = new HeadKeeper("John", "Smith", "Address", "Phone");
            AssistantKeeper assistant = new AssistantKeeper("Jane", "Doe", "Address", "Phone");
            
            String headPermissions = headKeeper.getResponsibilities();
            String assistantPermissions = assistant.getResponsibilities();
            
            assertNotEquals(headPermissions, assistantPermissions);
        }

        @Test
        @DisplayName("Should be able to manage maximum 4 cages like HeadKeeper")
        void shouldBeAbleToManageMaximumFourCages() {
            AssistantKeeper assistant = new AssistantKeeper("Jane", "Doe", "Address", "Phone");
            
            assistant.allocateCage(1);
            assistant.allocateCage(2);
            assistant.allocateCage(3);
            assistant.allocateCage(4);
            
            assertEquals(4, assistant.getAllocatedCageIds().size());
            assertFalse(assistant.canAcceptMoreCages());
        }
    }

    // ==================== Inheritance and Polymorphism Tests ====================

    @Nested
    @DisplayName("Inheritance and Polymorphism Tests")
    class InheritanceAndPolymorphismTests {

        @Test
        @DisplayName("HeadKeeper should be instance of Keeper")
        void headKeeperShouldBeInstanceOfKeeper() {
            HeadKeeper headKeeper = new HeadKeeper("John", "Smith", "Address", "Phone");
            assertTrue(headKeeper instanceof Keeper);
        }

        @Test
        @DisplayName("AssistantKeeper should be instance of Keeper")
        void assistantKeeperShouldBeInstanceOfKeeper() {
            AssistantKeeper assistant = new AssistantKeeper("Jane", "Doe", "Address", "Phone");
            assertTrue(assistant instanceof Keeper);
        }

        @Test
        @DisplayName("Should store different keeper types in Keeper reference")
        void shouldStoreDifferentKeeperTypesInKeeperReference() {
            Keeper keeper1 = new HeadKeeper("John", "Smith", "Address", "Phone");
            Keeper keeper2 = new AssistantKeeper("Jane", "Doe", "Address", "Phone");
            
            assertNotNull(keeper1);
            assertNotNull(keeper2);
            assertEquals(Keeper.Position.HEAD_KEEPER, keeper1.getPosition());
            assertEquals(Keeper.Position.ASSISTANT_KEEPER, keeper2.getPosition());
        }

        @Test
        @DisplayName("Should call correct getResponsibilities implementation through polymorphism")
        void shouldCallCorrectgetResponsibilitiesImplementationThroughPolymorphism() {
            Keeper headKeeper = new HeadKeeper("John", "Smith", "Address", "Phone");
            Keeper assistant = new AssistantKeeper("Jane", "Doe", "Address", "Phone");
            
            String headPermissions = headKeeper.getResponsibilities();
            String assistantPermissions = assistant.getResponsibilities();
            
            assertNotEquals(headPermissions, assistantPermissions);
        }

        @Test
        @DisplayName("Should call correct getFullTitle implementation through polymorphism")
        void shouldCallCorrectGetFullTitleImplementationThroughPolymorphism() {
            Keeper headKeeper = new HeadKeeper("John", "Smith", "Address", "Phone");
            Keeper assistant = new AssistantKeeper("Jane", "Doe", "Address", "Phone");
            
            String headTitle = headKeeper.getFullTitle();
            String assistantTitle = assistant.getFullTitle();
            
            assertTrue(headTitle.contains("Head Keeper"));
            assertTrue(assistantTitle.contains("Assistant Keeper"));
        }

        @Test
        @DisplayName("Both keeper types should share common Keeper behaviour")
        void bothKeeperTypesShouldShareCommonKeeperBehaviour() {
            Keeper keeper1 = new HeadKeeper("John", "Smith", "Address1", "Phone1");
            Keeper keeper2 = new AssistantKeeper("Jane", "Doe", "Address2", "Phone2");
            
            keeper1.allocateCage(1);
            keeper2.allocateCage(2);
            
            assertEquals(1, keeper1.getAllocatedCageIds().size());
            assertEquals(1, keeper2.getAllocatedCageIds().size());
            
            assertTrue(keeper1.canAcceptMoreCages());
            assertTrue(keeper2.canAcceptMoreCages());
        }
    }

    // ==================== ID Management Tests ====================

    @Nested
    @DisplayName("ID Management Tests")
    class IdManagementTests {

        @Test
        @DisplayName("Should have ID of 0 when first created")
        void shouldHaveZeroIdWhenCreated() {
            HeadKeeper keeper = new HeadKeeper("John", "Smith", "Address", "Phone");
            assertEquals(0, keeper.getKeeperId());
        }

        @Test
        @DisplayName("Should update ID when set by registry")
        void shouldUpdateIdWhenSetByRegistry() {
            HeadKeeper keeper = new HeadKeeper("John", "Smith", "Address", "Phone");
            keeper.setKeeperId(42);
            assertEquals(42, keeper.getKeeperId());
        }

        @Test
        @DisplayName("Should allow ID to be updated for both keeper types")
        void shouldAllowIdToBeUpdatedForBothKeeperTypes() {
            HeadKeeper headKeeper = new HeadKeeper("John", "Smith", "Address", "Phone");
            AssistantKeeper assistant = new AssistantKeeper("Jane", "Doe", "Address", "Phone");
            
            headKeeper.setKeeperId(1);
            assistant.setKeeperId(2);
            
            assertEquals(1, headKeeper.getKeeperId());
            assertEquals(2, assistant.getKeeperId());
        }
    }

    // ==================== Edge Cases ====================

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should accept very long name")
        void shouldAcceptVeryLongName() {
            String longFirstName = "A".repeat(100);
            String longSurname = "B".repeat(100);
            
            HeadKeeper keeper = new HeadKeeper(longFirstName, longSurname, "Address", "Phone");
            
            assertEquals(longFirstName, keeper.getFirstName());
            assertEquals(longSurname, keeper.getSurname());
        }

        @Test
        @DisplayName("Should accept address with special characters")
        void shouldAcceptAddressWithSpecialCharacters() {
            String address = "Flat 2/1, 123 Main St, Glasgow, G1 1AA";
            HeadKeeper keeper = new HeadKeeper("John", "Smith", address, "Phone");
            assertEquals(address, keeper.getAddress());
        }

        @Test
        @DisplayName("Should accept UK phone number formats")
        void shouldAcceptUKPhoneNumberFormats() {
            HeadKeeper keeper1 = new HeadKeeper("John", "Smith", "Address", "07123456789");
            HeadKeeper keeper2 = new HeadKeeper("Jane", "Doe", "Address", "0141 123 4567");
            HeadKeeper keeper3 = new HeadKeeper("Bob", "Johnson", "Address", "+44 7123 456789");
            
            assertEquals("07123456789", keeper1.getContactNumber());
            assertEquals("0141 123 4567", keeper2.getContactNumber());
            assertEquals("+44 7123 456789", keeper3.getContactNumber());
        }

        @Test
        @DisplayName("Should handle adding and removing same cage multiple times")
        void shouldHandleAddingAndRemovingSameCageMultipleTimes() {
            HeadKeeper keeper = new HeadKeeper("John", "Smith", "Address", "Phone");
            
            keeper.allocateCage(1);
            keeper.removeCage(1);
            keeper.allocateCage(1);
            keeper.removeCage(1);
            keeper.allocateCage(1);
            
            assertEquals(1, keeper.getAllocatedCageIds().size());
            assertTrue(keeper.getAllocatedCageIds().contains(1));
        }

        @Test
        @DisplayName("Should handle clearing all cages one by one")
        void shouldHandleClearingAllCagesOneByOne() {
            HeadKeeper keeper = new HeadKeeper("John", "Smith", "Address", "Phone");
            keeper.allocateCage(1);
            keeper.allocateCage(2);
            keeper.allocateCage(3);
            
            keeper.removeCage(1);
            keeper.removeCage(2);
            keeper.removeCage(3);
            
            assertTrue(keeper.getAllocatedCageIds().isEmpty());
            assertTrue(keeper.canAcceptMoreCages());
        }
    }
}
