package com.conservation.exception;

/**
 * Base exception class for all Conservation system exceptions.
 * 
 * All custom exceptions in the system extend from this class,
 * allowing for centralised exception handling and logging.
 * 
 * Extends Exception (checked exception) to enforce proper error handling
 * throughout the application.
 */
public class ConservationException extends Exception {
    
    /**
     * Constructs a new ConservationException with the specified error message.
     * 
     * @param message detailed error message describing what went wrong
     */
    public ConservationException(String message) {
        super(message);
    }
    
    /**
     * Constructs a new ConservationException with error message and underlying cause.
     * 
     * Used when wrapping lower-level exceptions (e.g., IOException, XMLException)
     * to provide context whilst preserving the original error.
     * 
     * @param message detailed error message
     * @param cause the underlying exception that caused this error
     */
    public ConservationException(String message, Throwable cause) {
        super(message, cause);
    }
}
