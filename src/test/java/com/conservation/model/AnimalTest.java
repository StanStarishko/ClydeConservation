package com.conservation.model;

import com.conservation.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for the Animal entity class.
 * Tests constructor validation, getters/setters, equals/hashCode,
 * Comparable interface, and date validation logic.
 */
@DisplayName("Animal Entity Tests")
class AnimalTest {

    private LocalDate validBirthDate;
    private LocalDate validAcquisitionDate;

    @BeforeEach
    void setUp() {
        validBirthDate = LocalDate.of(2020, 5, 15);
        validAcquisitionDate = LocalDate.of(2023, 11, 20);
    }

    // ==================== Constructor Tests ====================

    @Nested
    @DisplayName("Constructor Validation Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create valid Animal with all correct parameters")
        void shouldCreateValidAnimal() {
            // Given: valid animal parameters
            // When: creating new animal
            Animal animal = new Animal(
                    "Leo",
                    "Tiger",
                    Animal.Category.PREDATOR,
                    validBirthDate,
                    validAcquisitionDate,
                    Animal.Sex.MALE
            );

            // Then: animal should be created with correct values
            assertNotNull(animal);
            assertEquals("Leo", animal.getName());
            assertEquals("Tiger", animal.getType());
            assertEquals(Animal.Category.PREDATOR, animal.getCategory());
            assertEquals(validBirthDate, animal.getDateOfBirth());
            assertEquals(validAcquisitionDate, animal.getDateOfAcquisition());
            assertEquals(Animal.Sex.MALE, animal.getSex());
            assertEquals(0, animal.getAnimalId()); // ID not set until added to registry
        }

        @Test
        @DisplayName("Should throw ValidationException when name is null")
        void shouldThrowExceptionWhenNameIsNull() {
            ValidationException exception = assertThrows(ValidationException.class, () ->
                    new Animal(null, "Tiger", Animal.Category.PREDATOR,
                            validBirthDate, validAcquisitionDate, Animal.Sex.MALE)
            );
            assertTrue(exception.getMessage().contains("name"));
        }

        @Test
        @DisplayName("Should throw ValidationException when name is empty")
        void shouldThrowExceptionWhenNameIsEmpty() {
            ValidationException exception = assertThrows(ValidationException.class, () ->
                    new Animal("", "Tiger", Animal.Category.PREDATOR,
                            validBirthDate, validAcquisitionDate, Animal.Sex.MALE)
            );
            assertTrue(exception.getMessage().contains("name"));
        }

        @Test
        @DisplayName("Should throw ValidationException when type is null")
        void shouldThrowExceptionWhenTypeIsNull() {
            ValidationException exception = assertThrows(ValidationException.class, () ->
                    new Animal("Leo", null, Animal.Category.PREDATOR,
                            validBirthDate, validAcquisitionDate, Animal.Sex.MALE)
            );
            assertTrue(exception.getMessage().contains("type"));
        }

        @Test
        @DisplayName("Should throw ValidationException when category is null")
        void shouldThrowExceptionWhenCategoryIsNull() {
            ValidationException exception = assertThrows(ValidationException.class, () ->
                    new Animal("Leo", "Tiger", null,
                            validBirthDate, validAcquisitionDate, Animal.Sex.MALE)
            );
            assertTrue(exception.getMessage().contains("category"));
        }

        @Test
        @DisplayName("Should throw ValidationException when sex is null")
        void shouldThrowExceptionWhenSexIsNull() {
            ValidationException exception = assertThrows(ValidationException.class, () ->
                    new Animal("Leo", "Tiger", Animal.Category.PREDATOR,
                            validBirthDate, validAcquisitionDate, null)
            );
            assertTrue(exception.getMessage().contains("sex"));
        }
    }

    // ==================== Date Validation Tests ====================

    @Nested
    @DisplayName("Date Validation Tests")
    class DateValidationTests {

        @Test
        @DisplayName("Should throw ValidationException when birth date is in future")
        void shouldThrowExceptionWhenBirthDateInFuture() {
            LocalDate futureBirthDate = LocalDate.now().plusDays(1);

            ValidationException exception = assertThrows(ValidationException.class, () ->
                    new Animal("Leo", "Tiger", Animal.Category.PREDATOR,
                            futureBirthDate, validAcquisitionDate, Animal.Sex.MALE)
            );
            assertTrue(exception.getMessage().toLowerCase().contains("future"));
        }

        @Test
        @DisplayName("Should throw ValidationException when acquisition date is in future")
        void shouldThrowExceptionWhenAcquisitionDateInFuture() {
            LocalDate futureAcquisitionDate = LocalDate.now().plusDays(1);

            ValidationException exception = assertThrows(ValidationException.class, () ->
                    new Animal("Leo", "Tiger", Animal.Category.PREDATOR,
                            validBirthDate, futureAcquisitionDate, Animal.Sex.MALE)
            );
            assertTrue(exception.getMessage().toLowerCase().contains("future"));
        }

        @Test
        @DisplayName("Should throw ValidationException when acquisition date is before birth date")
        void shouldThrowExceptionWhenAcquisitionBeforeBirth() {
            LocalDate birthDate = LocalDate.of(2020, 5, 15);
            LocalDate acquisitionDate = LocalDate.of(2019, 1, 1); // Before birth

            ValidationException exception = assertThrows(ValidationException.class, () ->
                    new Animal("Leo", "Tiger", Animal.Category.PREDATOR,
                            birthDate, acquisitionDate, Animal.Sex.MALE)
            );
            assertTrue(exception.getMessage().toLowerCase().contains("acquisition"));
        }

        @Test
        @DisplayName("Should accept acquisition date same as birth date")
        void shouldAcceptAcquisitionDateSameAsBirthDate() {
            LocalDate date = LocalDate.of(2020, 5, 15);

            assertDoesNotThrow(() ->
                    new Animal("Leo", "Tiger", Animal.Category.PREDATOR,
                            date, date, Animal.Sex.MALE)
            );
        }

        @Test
        @DisplayName("Should accept today as birth date")
        void shouldAcceptTodayAsBirthDate() {
            LocalDate today = LocalDate.now();

            assertDoesNotThrow(() ->
                    new Animal("Leo", "Tiger", Animal.Category.PREDATOR,
                            today, today, Animal.Sex.MALE)
            );
        }
    }

    // ==================== Setter Tests ====================

    @Nested
    @DisplayName("Setter Validation Tests")
    class SetterTests {

        private Animal testAnimal;

        @BeforeEach
        void setUpAnimal() {
            testAnimal = new Animal("Leo", "Tiger", Animal.Category.PREDATOR,
                    validBirthDate, validAcquisitionDate, Animal.Sex.MALE);
        }

        @Test
        @DisplayName("Should update name with valid value")
        void shouldUpdateNameWithValidValue() {
            testAnimal.setName("Simba");
            assertEquals("Simba", testAnimal.getName());
        }

        @Test
        @DisplayName("Should throw ValidationException when setting null name")
        void shouldThrowExceptionWhenSettingNullName() {
            assertThrows(ValidationException.class, () ->
                    testAnimal.setName(null)
            );
        }

        @Test
        @DisplayName("Should throw ValidationException when setting empty name")
        void shouldThrowExceptionWhenSettingEmptyName() {
            assertThrows(ValidationException.class, () ->
                    testAnimal.setName("")
            );
        }

        @Test
        @DisplayName("Should update type with valid value")
        void shouldUpdateTypeWithValidValue() {
            testAnimal.setType("Lion");
            assertEquals("Lion", testAnimal.getType());
        }

        @Test
        @DisplayName("Should update category with valid value")
        void shouldUpdateCategory() {
            testAnimal.setCategory(Animal.Category.PREY);
            assertEquals(Animal.Category.PREY, testAnimal.getCategory());
        }

        @Test
        @DisplayName("Should update sex with valid value")
        void shouldUpdateSex() {
            testAnimal.setSex(Animal.Sex.FEMALE);
            assertEquals(Animal.Sex.FEMALE, testAnimal.getSex());
        }

        @Test
        @DisplayName("Should update birth date with valid past date")
        void shouldUpdateBirthDateWithValidPastDate() {
            LocalDate newBirthDate = LocalDate.of(2019, 1, 1);
            testAnimal.setDateOfBirth(newBirthDate);
            assertEquals(newBirthDate, testAnimal.getDateOfBirth());
        }

        @Test
        @DisplayName("Should throw ValidationException when setting future birth date")
        void shouldThrowExceptionWhenSettingFutureBirthDate() {
            LocalDate futureBirthDate = LocalDate.now().plusDays(1);
            assertThrows(ValidationException.class, () ->
                    testAnimal.setDateOfBirth(futureBirthDate)
            );
        }

        @Test
        @DisplayName("Should update acquisition date with valid date after birth")
        void shouldUpdateAcquisitionDateWithValidDate() {
            LocalDate newAcquisitionDate = LocalDate.of(2021, 6, 15);
            testAnimal.setDateOfAcquisition(newAcquisitionDate);
            assertEquals(newAcquisitionDate, testAnimal.getDateOfAcquisition());
        }

        @Test
        @DisplayName("Should throw ValidationException when setting acquisition date before birth")
        void shouldThrowExceptionWhenSettingAcquisitionBeforeBirth() {
            LocalDate acquisitionDate = validBirthDate.minusDays(1);
            assertThrows(ValidationException.class, () ->
                    testAnimal.setDateOfAcquisition(acquisitionDate)
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
            Animal animal = new Animal("Leo", "Tiger", Animal.Category.PREDATOR,
                    validBirthDate, validAcquisitionDate, Animal.Sex.MALE);
            assertEquals(0, animal.getAnimalId());
        }

        @Test
        @DisplayName("Should update ID when set by registry")
        void shouldUpdateIdWhenSetByRegistry() {
            Animal animal = new Animal("Leo", "Tiger", Animal.Category.PREDATOR,
                    validBirthDate, validAcquisitionDate, Animal.Sex.MALE);
            animal.setAnimalId(42);
            assertEquals(42, animal.getAnimalId());
        }

        @Test
        @DisplayName("Should allow ID to be updated multiple times")
        void shouldAllowIdToBeUpdatedMultipleTimes() {
            Animal animal = new Animal("Leo", "Tiger", Animal.Category.PREDATOR,
                    validBirthDate, validAcquisitionDate, Animal.Sex.MALE);
            animal.setAnimalId(1);
            animal.setAnimalId(2);
            animal.setAnimalId(3);
            assertEquals(3, animal.getAnimalId());
        }
    }

    // ==================== Equals and HashCode Tests ====================

    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsAndHashCodeTests {

        @Test
        @DisplayName("Should be equal when same ID")
        void shouldBeEqualWhenSameId() {
            Animal animal1 = new Animal("Leo", "Tiger", Animal.Category.PREDATOR,
                    validBirthDate, validAcquisitionDate, Animal.Sex.MALE);
            animal1.setAnimalId(1);

            Animal animal2 = new Animal("Simba", "Lion", Animal.Category.PREDATOR,
                    validBirthDate, validAcquisitionDate, Animal.Sex.FEMALE);
            animal2.setAnimalId(1);

            assertEquals(animal1, animal2);
        }

        @Test
        @DisplayName("Should not be equal when different ID")
        void shouldNotBeEqualWhenDifferentId() {
            Animal animal1 = new Animal("Leo", "Tiger", Animal.Category.PREDATOR,
                    validBirthDate, validAcquisitionDate, Animal.Sex.MALE);
            animal1.setAnimalId(1);

            Animal animal2 = new Animal("Leo", "Tiger", Animal.Category.PREDATOR,
                    validBirthDate, validAcquisitionDate, Animal.Sex.MALE);
            animal2.setAnimalId(2);

            assertNotEquals(animal1, animal2);
        }

        @Test
        @DisplayName("Should have same hashCode when same ID")
        void shouldHaveSameHashCodeWhenSameId() {
            Animal animal1 = new Animal("Leo", "Tiger", Animal.Category.PREDATOR,
                    validBirthDate, validAcquisitionDate, Animal.Sex.MALE);
            animal1.setAnimalId(1);

            Animal animal2 = new Animal("Simba", "Lion", Animal.Category.PREDATOR,
                    validBirthDate, validAcquisitionDate, Animal.Sex.FEMALE);
            animal2.setAnimalId(1);

            assertEquals(animal1.hashCode(), animal2.hashCode());
        }

        @Test
        @DisplayName("Should be equal to itself")
        void shouldBeEqualToItself() {
            Animal animal = new Animal("Leo", "Tiger", Animal.Category.PREDATOR,
                    validBirthDate, validAcquisitionDate, Animal.Sex.MALE);
            animal.setAnimalId(1);

            assertEquals(animal, animal);
        }

        @Test
        @DisplayName("Should not be equal to null")
        void shouldNotBeEqualToNull() {
            Animal animal = new Animal("Leo", "Tiger", Animal.Category.PREDATOR,
                    validBirthDate, validAcquisitionDate, Animal.Sex.MALE);
            animal.setAnimalId(1);

            assertNotEquals(animal, null);
        }

        @Test
        @DisplayName("Should not be equal to different type object")
        void shouldNotBeEqualToDifferentType() {
            Animal animal = new Animal("Leo", "Tiger", Animal.Category.PREDATOR,
                    validBirthDate, validAcquisitionDate, Animal.Sex.MALE);
            animal.setAnimalId(1);

            assertNotEquals(animal, "Not an Animal");
        }
    }

    // ==================== Comparable Interface Tests ====================

    @Nested
    @DisplayName("Comparable Interface Tests (Alphabetical Sorting)")
    class ComparableTests {

        @Test
        @DisplayName("Should sort alphabetically by name (ascending)")
        void shouldSortAlphabeticallyByName() {
            Animal animalA = new Animal("Alice", "Rabbit", Animal.Category.PREY,
                    validBirthDate, validAcquisitionDate, Animal.Sex.FEMALE);
            Animal animalB = new Animal("Bob", "Zebra", Animal.Category.PREY,
                    validBirthDate, validAcquisitionDate, Animal.Sex.MALE);
            Animal animalC = new Animal("Charlie", "Tiger", Animal.Category.PREDATOR,
                    validBirthDate, validAcquisitionDate, Animal.Sex.MALE);

            assertTrue(animalA.compareTo(animalB) < 0);
            assertTrue(animalB.compareTo(animalC) < 0);
            assertTrue(animalA.compareTo(animalC) < 0);
        }

        @Test
        @DisplayName("Should return 0 when comparing animals with same name")
        void shouldReturnZeroForSameName() {
            Animal animal1 = new Animal("Leo", "Tiger", Animal.Category.PREDATOR,
                    validBirthDate, validAcquisitionDate, Animal.Sex.MALE);
            Animal animal2 = new Animal("Leo", "Lion", Animal.Category.PREDATOR,
                    validBirthDate, validAcquisitionDate, Animal.Sex.FEMALE);

            assertEquals(0, animal1.compareTo(animal2));
        }

        @Test
        @DisplayName("Should be case-insensitive when sorting")
        void shouldBeCaseInsensitiveWhenSorting() {
            Animal animalLower = new Animal("alice", "Rabbit", Animal.Category.PREY,
                    validBirthDate, validAcquisitionDate, Animal.Sex.FEMALE);
            Animal animalUpper = new Animal("ALICE", "Rabbit", Animal.Category.PREY,
                    validBirthDate, validAcquisitionDate, Animal.Sex.FEMALE);

            assertEquals(0, animalLower.compareTo(animalUpper));
        }

        @Test
        @DisplayName("Should return positive when this name comes after other name")
        void shouldReturnPositiveWhenNameComesAfter() {
            Animal animalZ = new Animal("Zebra", "Zebra", Animal.Category.PREY,
                    validBirthDate, validAcquisitionDate, Animal.Sex.MALE);
            Animal animalA = new Animal("Alice", "Rabbit", Animal.Category.PREY,
                    validBirthDate, validAcquisitionDate, Animal.Sex.FEMALE);

            assertTrue(animalZ.compareTo(animalA) > 0);
        }
    }

    // ==================== Enum Tests ====================

    @Nested
    @DisplayName("Enum Value Tests")
    class EnumTests {

        @Test
        @DisplayName("Should have PREDATOR and PREY categories")
        void shouldHavePredatorAndPreyCategories() {
            assertEquals(2, Animal.Category.values().length);
            assertNotNull(Animal.Category.PREDATOR);
            assertNotNull(Animal.Category.PREY);
        }

        @Test
        @DisplayName("Should have MALE and FEMALE sex values")
        void shouldHaveMaleAndFemaleSexValues() {
            assertEquals(2, Animal.Sex.values().length);
            assertNotNull(Animal.Sex.MALE);
            assertNotNull(Animal.Sex.FEMALE);
        }

        @Test
        @DisplayName("Should create PREDATOR animal correctly")
        void shouldCreatePredatorAnimal() {
            Animal predator = new Animal("Leo", "Tiger", Animal.Category.PREDATOR,
                    validBirthDate, validAcquisitionDate, Animal.Sex.MALE);
            assertEquals(Animal.Category.PREDATOR, predator.getCategory());
        }

        @Test
        @DisplayName("Should create PREY animal correctly")
        void shouldCreatePreyAnimal() {
            Animal prey = new Animal("Bugs", "Rabbit", Animal.Category.PREY,
                    validBirthDate, validAcquisitionDate, Animal.Sex.MALE);
            assertEquals(Animal.Category.PREY, prey.getCategory());
        }
    }

    // ==================== Edge Cases ====================

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should accept very long name")
        void shouldAcceptVeryLongName() {
            String longName = "A".repeat(200);
            Animal animal = new Animal(longName, "Tiger", Animal.Category.PREDATOR,
                    validBirthDate, validAcquisitionDate, Animal.Sex.MALE);
            assertEquals(longName, animal.getName());
        }

        @Test
        @DisplayName("Should accept name with special characters")
        void shouldAcceptNameWithSpecialCharacters() {
            String specialName = "Leo-1234 (The Great!)";
            Animal animal = new Animal(specialName, "Tiger", Animal.Category.PREDATOR,
                    validBirthDate, validAcquisitionDate, Animal.Sex.MALE);
            assertEquals(specialName, animal.getName());
        }

        @Test
        @DisplayName("Should accept very old animal")
        void shouldAcceptVeryOldAnimal() {
            LocalDate oldBirthDate = LocalDate.of(1900, 1, 1);
            LocalDate oldAcquisitionDate = LocalDate.of(1900, 6, 1);

            Animal oldAnimal = new Animal("Ancient", "Turtle", Animal.Category.PREY,
                    oldBirthDate, oldAcquisitionDate, Animal.Sex.FEMALE);

            assertEquals(oldBirthDate, oldAnimal.getDateOfBirth());
        }

        @Test
        @DisplayName("Should accept animal born and acquired today")
        void shouldAcceptAnimalBornAndAcquiredToday() {
            LocalDate today = LocalDate.now();

            Animal newbornAnimal = new Animal("Baby", "Rabbit", Animal.Category.PREY,
                    today, today, Animal.Sex.FEMALE);

            assertEquals(today, newbornAnimal.getDateOfBirth());
            assertEquals(today, newbornAnimal.getDateOfAcquisition());
        }
    }
}
