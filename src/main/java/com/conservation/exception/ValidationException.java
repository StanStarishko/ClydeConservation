package com.conservation.exception;

/**
 * Exception thrown when business rule validation fails.
 * 
 * Uses nested ErrorType enum to classify different validation failures,
 * enabling specific error handling and user-friendly messages.
 * 
 * Common scenarios:
 * - Cage capacity exceeded
 * - Invalid predator/prey mixing
 * - Keeper overload (>4 cages)
 * - Invalid input data
 */
public class ValidationException extends ConservationException {
    
    /**
     * Enum defining all possible validation error types in the system.
     * 
     * Each type has a default error message that can be overridden
     * with more specific details when throwing the exception.
     */
    public enum ErrorType {
        CAGE_CAPACITY_EXCEEDED("Cage has reached maximum capacity"),
        INVALID_PREDATOR_PREY_MIX("Cannot place predator with prey animals"),
        KEEPER_OVERLOAD("Keeper already has maximum number of cages (4)"),
        KEEPER_UNDERLOAD("Keeper must be assigned to at least 1 cage"),
        INVALID_ANIMAL_DATA("Animal data is invalid"),
        INVALID_KEEPER_DATA("Keeper data is invalid"),
        INVALID_CAGE_DATA("Cage data is invalid"),
        INVALID_INPUT("Invalid input provided");
        
        private final String defaultMessage;
        
        /**
         * Constructor for ErrorType enum.
         * 
         * @param defaultMessage the default message for this error type
         */
        ErrorType(String defaultMessage) {
            this.defaultMessage = defaultMessage;
        }
        
        /**
         * Gets the default error message for this error type.
         * 
         * @return default error message
         */
        public String getDefaultMessage() {
            return defaultMessage;
        }
    }
    
    private final ErrorType errorType;
    
    /**
     * Constructs a ValidationException with specified error type.
     * 
     * Uses the default message from the ErrorType enum.
     * 
     * Example: throw new ValidationException(ErrorType.CAGE_CAPACITY_EXCEEDED);
     * 
     * @param errorType the type of validation error that occurred
     */
    public ValidationException(ErrorType errorType) {
        super(errorType.getDefaultMessage());
        this.errorType = errorType;
    }
    
    /**
     * Constructs a ValidationException with error type and custom message.
     * 
     * Use this when you want to provide more specific details than the default message.
     * 
     * Example: throw new ValidationException(
     *     ErrorType.CAGE_CAPACITY_EXCEEDED,
     *     "Cage 1 is full (10/10 animals)"
     * );
     * 
     * @param errorType the type of validation error
     * @param customMessage detailed error message with specific context
     */
    public ValidationException(ErrorType errorType, String customMessage) {
        super(customMessage);
        this.errorType = errorType;
    }
    
    /**
     * Gets the error type of this validation exception.
     * 
     * Useful for exception handlers to determine appropriate action or message.
     * 
     * @return the ErrorType enum value
     */
    public ErrorType getErrorType() {
        return errorType;
    }
}
