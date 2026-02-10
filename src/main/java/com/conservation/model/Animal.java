package com.conservation.model;

import java.time.LocalDate;
import java.util.Objects;
import com.conservation.exception.ValidationException;

/**
 * Entity class representing an animal in the conservation system.
 * 
 * Animals are classified as either PREDATOR or PREY which determines
 * their cage allocation rules (predators must be housed alone).
 * 
 * Implements Comparable for sorting animals alphabetically by name.
 */
public class Animal implements Comparable<Animal> {
    
    /**
     * Category enum for classifying animals as predators or prey.
     * 
     * PREDATOR: Must be housed alone in a cage
     * PREY: Can share cages with other prey animals
     */
    public enum Category {
        PREDATOR,
        PREY
    }
    
    /**
     * Sex enum for animal gender.
     */
    public enum Sex {
        MALE,
        FEMALE
    }
    
    private int animalId;
    private String name;
    private String type;
    private Category category;
    private LocalDate dateOfBirth;
    private LocalDate dateOfAcquisition;
    private Sex sex;
    
    /**
     * Default constructor for creating empty Animal instance.
     * Required for XML deserialization.
     */
    public Animal() {
    }
    
    /**
     * Full constructor for creating an Animal with all attributes.
     * 
     * @param name animal's name
     * @param type animal's type/species (e.g., "Tiger", "Zebra")
     * @param category PREDATOR or PREY classification
     * @param dateOfBirth animal's date of birth
     * @param dateOfAcquisition date when animal was acquired by conservation
     * @param sex animal's gender (MALE or FEMALE)
     * @throws IllegalArgumentException if any required field is null or invalid
     */
    public Animal(String name, String type, Category category,
                  LocalDate dateOfBirth, LocalDate dateOfAcquisition, Sex sex) throws ValidationException {
        validateFields(name, type, category, dateOfBirth, dateOfAcquisition, sex);
        
        this.name = name;
        this.type = type;
        this.category = category;
        this.dateOfBirth = dateOfBirth;
        this.dateOfAcquisition = dateOfAcquisition;
        this.sex = sex;
    }
    
    /**
     * Validates all required fields are present and valid.
     * 
     * @throws IllegalArgumentException if validation fails
     */
    private void validateFields(String name, String type, Category category,
                                LocalDate dateOfBirth, LocalDate dateOfAcquisition, Sex sex) throws ValidationException {
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException(ValidationException.ErrorType.INVALID_ANIMAL_DATA,  "Animal name cannot be null or empty");
        }
        if (type == null || type.trim().isEmpty()) {
            throw new ValidationException(ValidationException.ErrorType.INVALID_ANIMAL_DATA,  "Animal type cannot be null or empty");
        }
        if (category == null) {
            throw new ValidationException(ValidationException.ErrorType.INVALID_ANIMAL_DATA,  "Animal category cannot be null");
        }
        if (dateOfBirth == null) {
            throw new ValidationException(ValidationException.ErrorType.INVALID_ANIMAL_DATA,  "Date of birth cannot be null");
        }
        if (dateOfBirth.isAfter(LocalDate.now())) {
            throw new ValidationException(ValidationException.ErrorType.INVALID_ANIMAL_DATA,  "Date of birth cannot be in the future");
        }
        if (dateOfAcquisition == null) {
            throw new ValidationException(ValidationException.ErrorType.INVALID_ANIMAL_DATA,  "Date of acquisition cannot be null");
        }
        if (dateOfAcquisition.isAfter(LocalDate.now())) {
            throw new ValidationException(
                    ValidationException.ErrorType.INVALID_ANIMAL_DATA,
                    "Date of acquisition cannot be in the future"
            );
        }
        if (dateOfAcquisition.isBefore(dateOfBirth)) {
            throw new ValidationException(
                    ValidationException.ErrorType.INVALID_ANIMAL_DATA,
                    "Date of acquisition cannot be before date of birth"
            );
        }
        if (sex == null) {
            throw new ValidationException(ValidationException.ErrorType.INVALID_ANIMAL_DATA,  "Animal sex cannot be null");
        }
        if (dateOfBirth.isAfter(LocalDate.now())) {
            throw new ValidationException(ValidationException.ErrorType.INVALID_ANIMAL_DATA,  "Date of birth cannot be in the future");
        }
        if (dateOfAcquisition.isBefore(dateOfBirth)) {
            throw new ValidationException(ValidationException.ErrorType.INVALID_ANIMAL_DATA,  "Date of acquisition cannot be before date of birth");
        }
    }

    // Getters and Setters

    public int getAnimalId() {
        return animalId;
    }

    public void setAnimalId(int animalId) {
        this.animalId = animalId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) throws ValidationException {
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException(ValidationException.ErrorType.INVALID_ANIMAL_DATA,  "Animal name cannot be null or empty");
        }
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) throws ValidationException {
        if (type == null || type.trim().isEmpty()) {
            throw new ValidationException(ValidationException.ErrorType.INVALID_ANIMAL_DATA,  "Animal type cannot be null or empty");
        }
        this.type = type;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) throws ValidationException {
        if (category == null) {
            throw new ValidationException(ValidationException.ErrorType.INVALID_ANIMAL_DATA,  "Animal category cannot be null");
        }
        this.category = category;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) throws ValidationException {
        if (dateOfBirth == null) {
            throw new ValidationException(ValidationException.ErrorType.INVALID_ANIMAL_DATA,  "Date of birth cannot be null");
        }
        if (dateOfBirth.isAfter(LocalDate.now())) {
            throw new ValidationException(ValidationException.ErrorType.INVALID_ANIMAL_DATA,  "Date of birth cannot be in the future");
        }
        this.dateOfBirth = dateOfBirth;
    }

    public LocalDate getDateOfAcquisition() {
        return dateOfAcquisition;
    }

    public void setDateOfAcquisition(LocalDate dateOfAcquisition) throws ValidationException {
        if (dateOfAcquisition == null) {
            throw new ValidationException(ValidationException.ErrorType.INVALID_ANIMAL_DATA,  "Date of acquisition cannot be null");
        }
        if (dateOfAcquisition.isAfter(LocalDate.now())) {
            throw new ValidationException(ValidationException.ErrorType.INVALID_ANIMAL_DATA,  "Date of acquisition cannot be in the future");
        }
        if (this.dateOfBirth != null && dateOfAcquisition.isBefore(this.dateOfBirth)) {
            throw new ValidationException(ValidationException.ErrorType.INVALID_ANIMAL_DATA,  "Date of acquisition cannot be before date of birth");
        }
        this.dateOfAcquisition = dateOfAcquisition;
    }

    public Sex getSex() {
        return sex;
    }

    public void setSex(Sex sex) throws ValidationException {
        if (sex == null) {
            throw new ValidationException(ValidationException.ErrorType.INVALID_ANIMAL_DATA,  "Animal sex cannot be null");
        }
        this.sex = sex;
    }
    
    /**
     * Compares animals alphabetically by name for sorting.
     * 
     * @param otherAnimal the animal to compare to
     * @return negative if this comes before other, positive if after, 0 if equal
     */
    @Override
    public int compareTo(Animal otherAnimal) {
        return this.name.compareToIgnoreCase(otherAnimal.name);
    }
    
    /**
     * Checks equality based on animal ID.
     * 
     * Two animals are considered equal if they have the same ID.
     * 
     * @param obj the object to compare
     * @return true if objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Animal animal = (Animal) obj;
        return animalId == animal.animalId;
    }
    
    /**
     * Generates hash code based on animal ID.
     * 
     * @return hash code for this animal
     */
    @Override
    public int hashCode() {
        return Objects.hash(animalId);
    }
    
    /**
     * Returns string representation of the animal.
     * 
     * Format: "Animal{id=1, name='Leo', type='Tiger', category=PREDATOR}"
     * 
     * @return string representation
     */
    @Override
    public String toString() {
        return String.format("Animal{id=%d, name='%s', type='%s', category=%s, sex=%s}",
                animalId, name, type, category, sex);
    }
}
