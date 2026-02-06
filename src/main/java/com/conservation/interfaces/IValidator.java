package com.conservation.interfaces;

/**
 * Generic interface for validating objects against business rules.
 * 
 * Implementations should validate specific types of objects (entities, allocations)
 * and provide detailed error messages when validation fails.
 * 
 * @param <T> the type of object to validate
 * 
 * Used by: AllocationValidator for validating animal/keeper allocations
 */
public interface IValidator<T> {
    
    /**
     * Validates the given object against business rules.
     * 
     * @param entity the object to validate
     * @return true if validation passes, false otherwise
     * @throws com.conservation.exception.ValidationException if validation fails with critical error
     */
    boolean validate(T entity);
    
    /**
     * Gets the last validation error message.
     * 
     * Returns the error message from the most recent validation attempt.
     * If last validation passed, returns null or empty string.
     * 
     * @return error message from last validation, or null if validation passed
     */
    String getValidationError();
}
