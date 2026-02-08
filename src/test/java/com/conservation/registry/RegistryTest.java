package com.conservation.registry;

import com.conservation.model.Animal;
import com.conservation.model.Animal.Category;
import com.conservation.model.Animal.Sex;
import com.conservation.model.Keeper;
import com.conservation.model.Keeper.Position;
import com.conservation.model.HeadKeeper;
import com.conservation.model.AssistantKeeper;
import com.conservation.model.Cage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unified test class for all Registry classes: Animals, Keepers, Cages.
 * Tests CRUD operations, auto-increment ID generation, custom finders, and edge cases.
 * 
 * <p>Registry classes implement IRegistry interface with common operations:
 * add(), findById(), getAll(), remove(), count(), clear()
 * 
 * @author Clyde Conservation Development Team
 * @version 1.0
 */
@DisplayName("Registry Classes Tests")
class RegistryTest {

    // ============================================================
    // Test Data Constants
    // ============================================================
    
    private static final LocalDate SAMPLE_BIRTH_DATE = LocalDate.of(2020, 5, 15);
    private static final LocalDate SAMPLE_ACQUISITION_DATE = LocalDate.of(2023, 11, 20);
    
    // ============================================================
    // Setup - Clear all registries before each test
    // ============================================================
    
    @BeforeEach
    void clearAllRegistries() {
        Animals.clear();
        Keepers.clear();
        Cages.clear();
    }

    // ============================================================
    // ANIMALS REGISTRY TESTS
    // ============================================================
    
    @Nested
    @DisplayName("Animals Registry Tests")
    class AnimalsRegistryTests {
        
        // ------------------------------------------------------------
        // Basic CRUD Operations
        // ------------------------------------------------------------
        
        @Test
        @DisplayName("Add animal and verify auto-generated ID")
        void addAnimal_ShouldAutoGenerateId() {
            // Arrange
            Animal tiger = createSampleAnimal("Leo", "Tiger", Category.PREDATOR);
            
            // Act
            Animals.add(tiger);
            
            // Assert
            assertTrue(tiger.getAnimalId() > 0, "Animal ID should be auto-generated and positive");
            assertEquals(1, Animals.count(), "Registry should contain exactly one animal");
        }
        
        @Test
        @DisplayName("Add multiple animals with sequential IDs")
        void addMultipleAnimals_ShouldHaveSequentialIds() {
            // Arrange
            Animal tiger = createSampleAnimal("Leo", "Tiger", Category.PREDATOR);
            Animal zebra = createSampleAnimal("Marty", "Zebra", Category.PREY);
            Animal rabbit = createSampleAnimal("Bugs", "Rabbit", Category.PREY);
            
            // Act
            Animals.add(tiger);
            Animals.add(zebra);
            Animals.add(rabbit);
            
            // Assert
            assertEquals(3, Animals.count(), "Registry should contain three animals");
            assertTrue(zebra.getAnimalId() > tiger.getAnimalId(), "IDs should be sequential");
            assertTrue(rabbit.getAnimalId() > zebra.getAnimalId(), "IDs should be sequential");
        }
        
        @Test
        @DisplayName("Find animal by ID returns correct animal")
        void findById_ExistingAnimal_ShouldReturnAnimal() {
            // Arrange
            Animal tiger = createSampleAnimal("Leo", "Tiger", Category.PREDATOR);
            Animals.add(tiger);
            int animalId = tiger.getAnimalId();
            
            // Act
            Animal foundAnimal = Animals.findById(animalId);
            
            // Assert
            assertNotNull(foundAnimal, "Found animal should not be null");
            assertEquals("Leo", foundAnimal.getName(), "Animal name should match");
            assertEquals("Tiger", foundAnimal.getType(), "Animal type should match");
        }
        
        @Test
        @DisplayName("Find animal by non-existent ID returns null")
        void findById_NonExistentId_ShouldReturnNull() {
            // Arrange
            Animal tiger = createSampleAnimal("Leo", "Tiger", Category.PREDATOR);
            Animals.add(tiger);
            
            // Act
            Animal foundAnimal = Animals.findById(9999);
            
            // Assert
            assertNull(foundAnimal, "Should return null for non-existent ID");
        }
        
        @Test
        @DisplayName("Get all animals returns defensive copy")
        void getAll_ShouldReturnDefensiveCopy() {
            // Arrange
            Animals.add(createSampleAnimal("Leo", "Tiger", Category.PREDATOR));
            Animals.add(createSampleAnimal("Marty", "Zebra", Category.PREY));
            
            // Act
            Collection<Animal> allAnimals = Animals.getAll();
            int originalSize = allAnimals.size();
            
            // Attempt to modify returned collection (should not affect registry)
            try {
                allAnimals.clear();
            } catch (UnsupportedOperationException ignored) {
                // Some implementations may return unmodifiable collection
            }
            
            // Assert
            assertEquals(2, Animals.count(), "Registry should still contain original animals");
        }
        
        @Test
        @DisplayName("Remove animal by ID returns true and decreases count")
        void remove_ExistingAnimal_ShouldReturnTrueAndDecreaseCount() {
            // Arrange
            Animal tiger = createSampleAnimal("Leo", "Tiger", Category.PREDATOR);
            Animals.add(tiger);
            int animalId = tiger.getAnimalId();
            
            // Act
            boolean removed = Animals.remove(animalId);
            
            // Assert
            assertTrue(removed, "Remove should return true for existing animal");
            assertEquals(0, Animals.count(), "Registry should be empty after removal");
            assertNull(Animals.findById(animalId), "Removed animal should not be found");
        }
        
        @Test
        @DisplayName("Remove non-existent animal returns false")
        void remove_NonExistentAnimal_ShouldReturnFalse() {
            // Arrange
            Animals.add(createSampleAnimal("Leo", "Tiger", Category.PREDATOR));
            
            // Act
            boolean removed = Animals.remove(9999);
            
            // Assert
            assertFalse(removed, "Remove should return false for non-existent ID");
            assertEquals(1, Animals.count(), "Registry count should remain unchanged");
        }
        
        @Test
        @DisplayName("Clear removes all animals")
        void clear_ShouldRemoveAllAnimals() {
            // Arrange
            Animals.add(createSampleAnimal("Leo", "Tiger", Category.PREDATOR));
            Animals.add(createSampleAnimal("Marty", "Zebra", Category.PREY));
            Animals.add(createSampleAnimal("Bugs", "Rabbit", Category.PREY));
            
            // Act
            Animals.clear();
            
            // Assert
            assertEquals(0, Animals.count(), "Registry should be empty after clear");
            assertTrue(Animals.getAll().isEmpty(), "GetAll should return empty collection");
        }
        
        // ------------------------------------------------------------
        // Custom Finder Methods
        // ------------------------------------------------------------
        
        @Test
        @DisplayName("Find animal by name returns correct animal")
        void findByName_ExistingName_ShouldReturnAnimal() {
            // Arrange
            Animals.add(createSampleAnimal("Leo", "Tiger", Category.PREDATOR));
            Animals.add(createSampleAnimal("Marty", "Zebra", Category.PREY));
            
            // Act
            Animal foundAnimal = Animals.findByName("Leo");
            
            // Assert
            assertNotNull(foundAnimal, "Should find animal by name");
            assertEquals("Leo", foundAnimal.getName(), "Found animal name should match");
        }
        
        @Test
        @DisplayName("Find animals by category returns correct list")
        void findByCategory_ShouldReturnMatchingAnimals() {
            // Arrange
            Animals.add(createSampleAnimal("Leo", "Tiger", Category.PREDATOR));
            Animals.add(createSampleAnimal("Shadow", "Eagle", Category.PREDATOR));
            Animals.add(createSampleAnimal("Marty", "Zebra", Category.PREY));
            
            // Act
            Collection<Animal> predators = Animals.findByCategory(Category.PREDATOR);
            
            // Assert
            assertEquals(2, predators.size(), "Should find two predators");
            assertTrue(predators.stream().allMatch(animal -> 
                animal.getCategory() == Category.PREDATOR), 
                "All found animals should be predators");
        }
        
        @Test
        @DisplayName("Find animals by type returns correct list")
        void findByType_ShouldReturnMatchingAnimals() {
            // Arrange
            Animals.add(createSampleAnimal("Leo", "Tiger", Category.PREDATOR));
            Animals.add(createSampleAnimal("Stripe", "Tiger", Category.PREDATOR));
            Animals.add(createSampleAnimal("Marty", "Zebra", Category.PREY));
            
            // Act
            Collection<Animal> tigers = Animals.findByType("Tiger");
            
            // Assert
            assertEquals(2, tigers.size(), "Should find two tigers");
            assertTrue(tigers.stream().allMatch(animal -> 
                "Tiger".equals(animal.getType())), 
                "All found animals should be tigers");
        }
        
        @Test
        @DisplayName("Find by name with non-existent name returns null")
        void findByName_NonExistentName_ShouldReturnNull() {
            // Arrange
            Animals.add(createSampleAnimal("Leo", "Tiger", Category.PREDATOR));
            
            // Act
            Animal foundAnimal = Animals.findByName("NonExistent");
            
            // Assert
            assertNull(foundAnimal, "Should return null for non-existent name");
        }
        
        // ------------------------------------------------------------
        // Persistence Initialisation
        // ------------------------------------------------------------
        
        @Test
        @DisplayName("Initialize from persistence restores ID counter")
        void initializeFromPersistence_ShouldRestoreIdCounter() {
            // Arrange - Simulate loaded animals with IDs 5, 10, 15
            Animal animal1 = createSampleAnimalWithId(5, "Leo", "Tiger", Category.PREDATOR);
            Animal animal2 = createSampleAnimalWithId(10, "Marty", "Zebra", Category.PREY);
            Animal animal3 = createSampleAnimalWithId(15, "Bugs", "Rabbit", Category.PREY);
            List<Animal> loadedAnimals = List.of(animal1, animal2, animal3);
            
            // Act
            Animals.initializeFromPersistence(loadedAnimals);
            
            // Add new animal - should get ID > 15
            Animal newAnimal = createSampleAnimal("NewAnimal", "Eagle", Category.PREDATOR);
            Animals.add(newAnimal);
            
            // Assert
            assertTrue(newAnimal.getAnimalId() > 15, 
                "New animal ID should be greater than max loaded ID");
            assertEquals(4, Animals.count(), "Should have 4 animals total");
        }
        
        // ------------------------------------------------------------
        // Edge Cases
        // ------------------------------------------------------------
        
        @Test
        @DisplayName("Count on empty registry returns zero")
        void count_EmptyRegistry_ShouldReturnZero() {
            // Assert
            assertEquals(0, Animals.count(), "Empty registry should have count of zero");
        }
        
        @Test
        @DisplayName("GetAll on empty registry returns empty collection")
        void getAll_EmptyRegistry_ShouldReturnEmptyCollection() {
            // Act
            Collection<Animal> allAnimals = Animals.getAll();
            
            // Assert
            assertNotNull(allAnimals, "Should return collection, not null");
            assertTrue(allAnimals.isEmpty(), "Collection should be empty");
        }
    }

    // ============================================================
    // KEEPERS REGISTRY TESTS
    // ============================================================
    
    @Nested
    @DisplayName("Keepers Registry Tests")
    class KeepersRegistryTests {
        
        // ------------------------------------------------------------
        // Basic CRUD Operations
        // ------------------------------------------------------------
        
        @Test
        @DisplayName("Add head keeper and verify auto-generated ID")
        void addHeadKeeper_ShouldAutoGenerateId() {
            // Arrange
            Keeper headKeeper = new HeadKeeper(
                "John", "Smith", "123 Main St", "07123456789"
            );
            
            // Act
            Keepers.add(headKeeper);
            
            // Assert
            assertTrue(headKeeper.getKeeperId() > 0, "Keeper ID should be auto-generated");
            assertEquals(1, Keepers.count(), "Registry should contain one keeper");
        }
        
        @Test
        @DisplayName("Add assistant keeper with auto-generated ID")
        void addAssistantKeeper_ShouldAutoGenerateId() {
            // Arrange
            Keeper assistantKeeper = new AssistantKeeper(
                "Jane", "Doe", "456 Oak Ave", "07987654321"
            );
            
            // Act
            Keepers.add(assistantKeeper);
            
            // Assert
            assertTrue(assistantKeeper.getKeeperId() > 0, "Keeper ID should be auto-generated");
            assertEquals(Position.ASSISTANT_KEEPER, assistantKeeper.getPosition(), 
                "Position should be ASSISTANT_KEEPER");
        }
        
        @Test
        @DisplayName("Find keeper by ID with polymorphism")
        void findById_ShouldReturnCorrectKeeperType() {
            // Arrange
            Keeper headKeeper = new HeadKeeper("John", "Smith", "123 Main St", "07123456789");
            Keeper assistantKeeper = new AssistantKeeper("Jane", "Doe", "456 Oak Ave", "07987654321");
            Keepers.add(headKeeper);
            Keepers.add(assistantKeeper);
            
            // Act
            Keeper foundHead = Keepers.findById(headKeeper.getKeeperId());
            Keeper foundAssistant = Keepers.findById(assistantKeeper.getKeeperId());
            
            // Assert
            assertInstanceOf(HeadKeeper.class, foundHead, "Should return HeadKeeper instance");
            assertInstanceOf(AssistantKeeper.class, foundAssistant, "Should return AssistantKeeper instance");
        }
        
        @Test
        @DisplayName("Remove keeper returns true and decreases count")
        void remove_ExistingKeeper_ShouldReturnTrue() {
            // Arrange
            Keeper keeper = new HeadKeeper("John", "Smith", "123 Main St", "07123456789");
            Keepers.add(keeper);
            int keeperId = keeper.getKeeperId();
            
            // Act
            boolean removed = Keepers.remove(keeperId);
            
            // Assert
            assertTrue(removed, "Remove should return true");
            assertEquals(0, Keepers.count(), "Registry should be empty");
        }
        
        // ------------------------------------------------------------
        // Custom Finder Methods
        // ------------------------------------------------------------
        
        @Test
        @DisplayName("Find keeper by full name")
        void findByFullName_ShouldReturnMatchingKeeper() {
            // Arrange
            Keepers.add(new HeadKeeper("John", "Smith", "123 Main St", "07123456789"));
            Keepers.add(new AssistantKeeper("Jane", "Doe", "456 Oak Ave", "07987654321"));
            
            // Act
            Keeper foundKeeper = Keepers.findByFullName("John Smith");
            
            // Assert
            assertNotNull(foundKeeper, "Should find keeper by full name");
            assertEquals("John", foundKeeper.getFirstName(), "First name should match");
            assertEquals("Smith", foundKeeper.getSurname(), "Surname should match");
        }
        
        @Test
        @DisplayName("Find keepers by position")
        void findByPosition_ShouldReturnMatchingKeepers() {
            // Arrange
            Keepers.add(new HeadKeeper("John", "Smith", "123 Main St", "07123456789"));
            Keepers.add(new HeadKeeper("Sarah", "Johnson", "789 Elm St", "07111222333"));
            Keepers.add(new AssistantKeeper("Jane", "Doe", "456 Oak Ave", "07987654321"));
            
            // Act
            Collection<Keeper> headKeepers = Keepers.findByPosition(Position.HEAD_KEEPER);
            
            // Assert
            assertEquals(2, headKeepers.size(), "Should find two head keepers");
            assertTrue(headKeepers.stream().allMatch(keeper -> 
                keeper.getPosition() == Position.HEAD_KEEPER),
                "All found keepers should be head keepers");
        }
        
        @Test
        @DisplayName("Find available keepers (less than 4 cages)")
        void findAvailableKeepers_ShouldReturnKeepersWithSpareCapacity() {
            // Arrange
            HeadKeeper busyKeeper = new HeadKeeper("John", "Smith", "123 Main St", "07123456789");
            HeadKeeper availableKeeper = new HeadKeeper("Sarah", "Johnson", "789 Elm St", "07111222333");
            
            // Simulate busy keeper with 4 cages
            busyKeeper.allocateCage(1);
            busyKeeper.allocateCage(2);
            busyKeeper.allocateCage(3);
            busyKeeper.allocateCage(4);
            
            // Available keeper has only 1 cage
            availableKeeper.allocateCage(5);
            
            Keepers.add(busyKeeper);
            Keepers.add(availableKeeper);
            
            // Act
            Collection<Keeper> availableKeepers = Keepers.findAvailableKeepers();
            
            // Assert
            assertEquals(1, availableKeepers.size(), "Should find one available keeper");
            assertTrue(availableKeepers.stream().anyMatch(keeper -> 
                "Sarah".equals(keeper.getFirstName())),
                "Available keeper should be Sarah");
        }
        
        @Test
        @DisplayName("Find overloaded keepers (at maximum 4 cages)")
        void findOverloadedKeepers_ShouldReturnKeepersAtMaxCapacity() {
            // Arrange
            HeadKeeper overloadedKeeper = new HeadKeeper("John", "Smith", "123 Main St", "07123456789");
            overloadedKeeper.allocateCage(1);
            overloadedKeeper.allocateCage(2);
            overloadedKeeper.allocateCage(3);
            overloadedKeeper.allocateCage(4);
            
            HeadKeeper normalKeeper = new HeadKeeper("Sarah", "Johnson", "789 Elm St", "07111222333");
            normalKeeper.allocateCage(5);
            
            Keepers.add(overloadedKeeper);
            Keepers.add(normalKeeper);
            
            // Act
            Collection<Keeper> overloadedKeepers = Keepers.findOverloadedKeepers();
            
            // Assert
            assertEquals(1, overloadedKeepers.size(), "Should find one overloaded keeper");
        }
        
        @Test
        @DisplayName("Find keepers by cage ID")
        void findByCageId_ShouldReturnKeepersAssignedToCage() {
            // Arrange
            HeadKeeper keeper1 = new HeadKeeper("John", "Smith", "123 Main St", "07123456789");
            HeadKeeper keeper2 = new HeadKeeper("Sarah", "Johnson", "789 Elm St", "07111222333");
            
            keeper1.allocateCage(1);
            keeper1.allocateCage(2);
            keeper2.allocateCage(3);
            
            Keepers.add(keeper1);
            Keepers.add(keeper2);
            
            // Act
            Collection<Keeper> keepersForCage1 = Keepers.findByCageId(1);
            
            // Assert
            assertEquals(1, keepersForCage1.size(), "Should find one keeper for cage 1");
            assertTrue(keepersForCage1.stream().anyMatch(keeper -> 
                "John".equals(keeper.getFirstName())),
                "John should be assigned to cage 1");
        }
    }

    // ============================================================
    // CAGES REGISTRY TESTS
    // ============================================================
    
    @Nested
    @DisplayName("Cages Registry Tests")
    class CagesRegistryTests {
        
        // ------------------------------------------------------------
        // Basic CRUD Operations
        // ------------------------------------------------------------
        
        @Test
        @DisplayName("Add cage and verify auto-generated ID")
        void addCage_ShouldAutoGenerateId() {
            // Arrange
            Cage cage = new Cage("Large-01", "Large predator cage", 10);
            
            // Act
            Cages.add(cage);
            
            // Assert
            assertTrue(cage.getCageId() > 0, "Cage ID should be auto-generated");
            assertEquals(1, Cages.count(), "Registry should contain one cage");
        }
        
        @Test
        @DisplayName("Find cage by ID returns correct cage")
        void findById_ExistingCage_ShouldReturnCage() {
            // Arrange
            Cage cage = new Cage("Large-01", "Large predator cage", 10);
            Cages.add(cage);
            
            // Act
            Cage foundCage = Cages.findById(cage.getCageId());
            
            // Assert
            assertNotNull(foundCage, "Should find cage by ID");
            assertEquals("Large-01", foundCage.getCageNumber(), "Cage number should match");
            assertEquals(10, foundCage.getAnimalCapacity(), "Capacity should match");
        }
        
        @Test
        @DisplayName("Remove cage returns true and decreases count")
        void remove_ExistingCage_ShouldReturnTrue() {
            // Arrange
            Cage cage = new Cage("Large-01", "Large predator cage", 10);
            Cages.add(cage);
            int cageId = cage.getCageId();
            
            // Act
            boolean removed = Cages.remove(cageId);
            
            // Assert
            assertTrue(removed, "Remove should return true");
            assertEquals(0, Cages.count(), "Registry should be empty");
        }
        
        // ------------------------------------------------------------
        // Custom Finder Methods
        // ------------------------------------------------------------
        
        @Test
        @DisplayName("Find cage by cage number")
        void findByCageNumber_ShouldReturnMatchingCage() {
            // Arrange
            Cages.add(new Cage("Large-01", "Large predator cage", 10));
            Cages.add(new Cage("Medium-01", "Medium prey cage", 5));
            
            // Act
            Cage foundCage = Cages.findByCageNumber("Large-01");
            
            // Assert
            assertNotNull(foundCage, "Should find cage by number");
            assertEquals("Large-01", foundCage.getCageNumber(), "Cage number should match");
        }
        
        @Test
        @DisplayName("Find empty cages")
        void findEmptyCages_ShouldReturnCagesWithNoAnimals() {
            // Arrange
            Cage emptyCage = new Cage("Large-01", "Empty cage", 10);
            Cage occupiedCage = new Cage("Large-02", "Occupied cage", 10);
            occupiedCage.addAnimal(1); // Add an animal
            
            Cages.add(emptyCage);
            Cages.add(occupiedCage);
            
            // Act
            Collection<Cage> emptyCages = Cages.findEmptyCages();
            
            // Assert
            assertEquals(1, emptyCages.size(), "Should find one empty cage");
            assertTrue(emptyCages.stream().allMatch(Cage::isEmpty), 
                "All found cages should be empty");
        }
        
        @Test
        @DisplayName("Find full cages")
        void findFullCages_ShouldReturnCagesAtMaxCapacity() {
            // Arrange
            Cage smallCage = new Cage("Small-01", "Small cage", 1);
            smallCage.addAnimal(1); // Fill the small cage
            
            Cage largeCage = new Cage("Large-01", "Large cage", 10);
            largeCage.addAnimal(2); // Not full
            
            Cages.add(smallCage);
            Cages.add(largeCage);
            
            // Act
            Collection<Cage> fullCages = Cages.findFullCages();
            
            // Assert
            assertEquals(1, fullCages.size(), "Should find one full cage");
            assertTrue(fullCages.stream().allMatch(Cage::isFull), 
                "All found cages should be full");
        }
        
        @Test
        @DisplayName("Find available cages (have space)")
        void findAvailableCages_ShouldReturnCagesWithSpace() {
            // Arrange
            Cage fullCage = new Cage("Small-01", "Full cage", 1);
            fullCage.addAnimal(1);
            
            Cage availableCage = new Cage("Large-01", "Available cage", 10);
            availableCage.addAnimal(2);
            availableCage.addAnimal(3); // Still has space
            
            Cages.add(fullCage);
            Cages.add(availableCage);
            
            // Act
            Collection<Cage> availableCages = Cages.findAvailableCages();
            
            // Assert
            assertEquals(1, availableCages.size(), "Should find one available cage");
            assertTrue(availableCages.stream().noneMatch(Cage::isFull), 
                "No found cage should be full");
        }
        
        @Test
        @DisplayName("Find cages by keeper ID")
        void findByKeeperId_ShouldReturnCagesAssignedToKeeper() {
            // Arrange
            Cage cage1 = new Cage("Large-01", "Cage 1", 10);
            Cage cage2 = new Cage("Large-02", "Cage 2", 10);
            Cage cage3 = new Cage("Medium-01", "Cage 3", 5);
            
            cage1.setAssignedKeeperId(1);
            cage2.setAssignedKeeperId(1);
            cage3.setAssignedKeeperId(2);
            
            Cages.add(cage1);
            Cages.add(cage2);
            Cages.add(cage3);
            
            // Act
            Collection<Cage> keeperCages = Cages.findByKeeperId(1);
            
            // Assert
            assertEquals(2, keeperCages.size(), "Should find two cages for keeper 1");
        }
        
        @Test
        @DisplayName("Find cage by animal ID")
        void findByAnimalId_ShouldReturnCageContainingAnimal() {
            // Arrange
            Cage cage1 = new Cage("Large-01", "Cage 1", 10);
            Cage cage2 = new Cage("Large-02", "Cage 2", 10);
            
            cage1.addAnimal(1);
            cage1.addAnimal(2);
            cage2.addAnimal(3);
            
            Cages.add(cage1);
            Cages.add(cage2);
            
            // Act
            Cage foundCage = (Cage) Cages.findByAnimalId(2);
            
            // Assert
            assertNotNull(foundCage, "Should find cage containing animal");
            assertEquals("Large-01", foundCage.getCageNumber(), 
                "Should be the cage containing animal 2");
        }
        
        @Test
        @DisplayName("Find cages by capacity")
        void findByCapacity_ShouldReturnCagesWithMatchingCapacity() {
            // Arrange
            Cages.add(new Cage("Large-01", "Large cage", 10));
            Cages.add(new Cage("Large-02", "Large cage", 10));
            Cages.add(new Cage("Medium-01", "Medium cage", 5));
            Cages.add(new Cage("Small-01", "Small cage", 1));
            
            // Act
            Collection<Cage> largeCages = Cages.findByCapacity(10);
            
            // Assert
            assertEquals(2, largeCages.size(), "Should find two large cages");
            assertTrue(largeCages.stream().allMatch(cage -> 
                cage.getAnimalCapacity() == 10),
                "All found cages should have capacity 10");
        }
        
        // ------------------------------------------------------------
        // Statistics Methods
        // ------------------------------------------------------------
        
        @Test
        @DisplayName("Get total capacity returns sum of all cage capacities")
        void getTotalCapacity_ShouldReturnSumOfCapacities() {
            // Arrange
            Cages.add(new Cage("Large-01", "Large cage", 10));
            Cages.add(new Cage("Medium-01", "Medium cage", 5));
            Cages.add(new Cage("Small-01", "Small cage", 1));
            
            // Act
            int totalCapacity = Cages.getTotalCapacity();
            
            // Assert
            assertEquals(16, totalCapacity, "Total capacity should be 16");
        }
        
        @Test
        @DisplayName("Get total occupancy returns count of all animals")
        void getTotalOccupancy_ShouldReturnTotalAnimalCount() {
            // Arrange
            Cage cage1 = new Cage("Large-01", "Large cage", 10);
            cage1.addAnimal(1);
            cage1.addAnimal(2);
            
            Cage cage2 = new Cage("Medium-01", "Medium cage", 5);
            cage2.addAnimal(3);
            
            Cages.add(cage1);
            Cages.add(cage2);
            
            // Act
            int totalOccupancy = Cages.getTotalOccupancy();
            
            // Assert
            assertEquals(3, totalOccupancy, "Total occupancy should be 3");
        }
        
        // ------------------------------------------------------------
        // Edge Cases
        // ------------------------------------------------------------
        
        @Test
        @DisplayName("Find by animal ID returns null when animal not in any cage")
        void findByAnimalId_AnimalNotInAnyCage_ShouldReturnNull() {
            // Arrange
            Cage cage = new Cage("Large-01", "Large cage", 10);
            cage.addAnimal(1);
            Cages.add(cage);
            
            // Act
            Cage foundCage = (Cage) Cages.findByAnimalId(999);
            
            // Assert
            assertNull(foundCage, "Should return null when animal not in any cage");
        }
    }

    // ============================================================
    // CROSS-REGISTRY INTEGRATION TESTS
    // ============================================================
    
    @Nested
    @DisplayName("Cross-Registry Integration Tests")
    class CrossRegistryIntegrationTests {
        
        @Test
        @DisplayName("All registries maintain independent ID sequences")
        void allRegistries_ShouldHaveIndependentIdSequences() {
            // Arrange & Act
            Animal animal = createSampleAnimal("Leo", "Tiger", Category.PREDATOR);
            Keeper keeper = new HeadKeeper("John", "Smith", "123 Main St", "07123456789");
            Cage cage = new Cage("Large-01", "Large cage", 10);
            
            Animals.add(animal);
            Keepers.add(keeper);
            Cages.add(cage);
            
            // Assert - IDs can be same across different registries
            // This is valid as they're in separate namespaces
            assertEquals(1, Animals.count(), "Animals should have one entry");
            assertEquals(1, Keepers.count(), "Keepers should have one entry");
            assertEquals(1, Cages.count(), "Cages should have one entry");
        }
        
        @Test
        @DisplayName("Clear one registry does not affect others")
        void clearOneRegistry_ShouldNotAffectOthers() {
            // Arrange
            Animals.add(createSampleAnimal("Leo", "Tiger", Category.PREDATOR));
            Keepers.add(new HeadKeeper("John", "Smith", "123 Main St", "07123456789"));
            Cages.add(new Cage("Large-01", "Large cage", 10));
            
            // Act
            Animals.clear();
            
            // Assert
            assertEquals(0, Animals.count(), "Animals should be empty");
            assertEquals(1, Keepers.count(), "Keepers should still have one entry");
            assertEquals(1, Cages.count(), "Cages should still have one entry");
        }
    }

    // ============================================================
    // Helper Methods
    // ============================================================
    
    /**
     * Creates a sample Animal for testing purposes.
     * 
     * @param name     the animal's name
     * @param type     the animal's type (species)
     * @param category the animal's category (PREDATOR or PREY)
     * @return a new Animal instance
     */
    private Animal createSampleAnimal(String name, String type, Category category) {
        return new Animal(
            name,
            type,
            category,
            SAMPLE_BIRTH_DATE,
            SAMPLE_ACQUISITION_DATE,
            Sex.MALE
        );
    }
    
    /**
     * Creates a sample Animal with a specific ID for persistence testing.
     * Uses reflection or direct setter if available.
     * 
     * @param animalId the animal's ID to set
     * @param name     the animal's name
     * @param type     the animal's type
     * @param category the animal's category
     * @return a new Animal instance with specified ID
     */
    private Animal createSampleAnimalWithId(int animalId, String name, String type, Category category) {
        Animal animal = createSampleAnimal(name, type, category);
        // Assuming there's a package-private or test method to set ID for persistence
        // In actual implementation, this might use reflection or a test-specific constructor
        try {
            java.lang.reflect.Field idField = Animal.class.getDeclaredField("animalId");
            idField.setAccessible(true);
            idField.setInt(animal, animalId);
        } catch (Exception exception) {
            // If reflection fails, the test framework should handle this
            fail("Unable to set animal ID for persistence test: " + exception.getMessage());
        }
        return animal;
    }
}
