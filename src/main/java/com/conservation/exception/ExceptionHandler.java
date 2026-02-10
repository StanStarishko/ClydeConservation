package com.conservation.exception;

/**
 * Centralised exception handler for the conservation system.
 * 
 * Routes different exception types to appropriate handler methods
 * and provides user-friendly error messages.
 * 
 * Handles:
 * - ValidationException (business rule violations)
 * - PersistenceException (file I/O and XML errors)
 * - IllegalArgumentException (input validation errors)
 * - General exceptions (unexpected errors)
 */
public class ExceptionHandler {
    
    /**
     * Handles any exception and routes it to the appropriate handler method.
     * 
     * This is the main entry point for exception handling in the system.
     * 
     * @param exception the exception to handle
     */
    public static void handle(Exception exception) {
        if (exception == null) {
            System.err.println("Unknown error occurred");
            return;
        }
        if (exception instanceof ValidationException) {
            handleValidation((ValidationException) exception);
        } else if (exception instanceof PersistenceException) {
            handlePersistence((PersistenceException) exception);
        } else if (exception instanceof IllegalArgumentException) {
            handleInput((IllegalArgumentException) exception);
        } else {
            handleSystem(exception);
        }
    }
    
    /**
     * Handles validation exceptions (business rule violations).
     * 
     * Provides user-friendly messages based on error type.
     * 
     * @param validationException the validation exception to handle
     */
    private static void handleValidation(ValidationException validationException) {
        System.err.println("\n=== VALIDATION ERROR ===");
        
        ValidationException.ErrorType errorType = validationException.getErrorType();
        
        switch (errorType) {
            case CAGE_CAPACITY_EXCEEDED:
                System.err.println("Error: The cage is full and cannot accept more animals.");
                System.err.println("Details: " + validationException.getMessage());
                System.err.println("Suggestion: Choose a different cage or remove animals from this cage.");
                break;
                
            case INVALID_PREDATOR_PREY_MIX:
                System.err.println("Error: Cannot mix predator and prey animals in the same cage.");
                System.err.println("Details: " + validationException.getMessage());
                System.err.println("Suggestion: Predator animals must be housed alone. Choose an empty cage.");
                break;
                
            case KEEPER_OVERLOAD:
                System.err.println("Error: Keeper has reached maximum cage allocation (4 cages).");
                System.err.println("Details: " + validationException.getMessage());
                System.err.println("Suggestion: Choose a different keeper or unassign cages from this keeper.");
                break;
                
            case KEEPER_UNDERLOAD:
                System.err.println("Error: Keeper must be assigned to at least 1 cage.");
                System.err.println("Details: " + validationException.getMessage());
                System.err.println("Suggestion: Assign at least one cage to this keeper.");
                break;
                
            case INVALID_ANIMAL_DATA:
                System.err.println("Error: Animal data is invalid or incomplete.");
                System.err.println("Details: " + validationException.getMessage());
                System.err.println("Suggestion: Check all required fields are filled correctly.");
                break;
                
            case INVALID_KEEPER_DATA:
                System.err.println("Error: Keeper data is invalid or incomplete.");
                System.err.println("Details: " + validationException.getMessage());
                System.err.println("Suggestion: Check all required fields are filled correctly.");
                break;
                
            case INVALID_CAGE_DATA:
                System.err.println("Error: Cage data is invalid or incomplete.");
                System.err.println("Details: " + validationException.getMessage());
                System.err.println("Suggestion: Check all required fields are filled correctly.");
                break;
                
            case INVALID_INPUT:
                System.err.println("Error: Invalid input provided.");
                System.err.println("Details: " + validationException.getMessage());
                System.err.println("Suggestion: Please check your input and try again.");
                break;
                
            default:
                System.err.println("Error: Validation failed.");
                System.err.println("Details: " + validationException.getMessage());
                break;
        }
        
        System.err.println("========================\n");
    }
    
    /**
     * Handles persistence exceptions (file I/O and XML errors).
     * 
     * Provides information about file operations that failed.
     * 
     * @param persistenceException the persistence exception to handle
     */
    private static void handlePersistence(PersistenceException persistenceException) {
        System.err.println("\n=== PERSISTENCE ERROR ===");
        System.err.println("Error: Failed to save or load data.");
        System.err.println("Details: " + persistenceException.getMessage());
        
        if (persistenceException.getFilePath() != null) {
            System.err.println("File: " + persistenceException.getFilePath());
        }
        
        if (persistenceException.getCause() != null) {
            System.err.println("Underlying cause: " + persistenceException.getCause().getMessage());
        }
        
        System.err.println("Suggestion: Check file permissions and ensure data files are not corrupted.");
        System.err.println("==========================\n");
    }
    
    /**
     * Handles input validation exceptions (IllegalArgumentException).
     * 
     * Typically thrown when user input is invalid at the field level.
     * 
     * @param illegalArgumentException the input exception to handle
     */
    private static void handleInput(IllegalArgumentException illegalArgumentException) {
        System.err.println("\n=== INPUT ERROR ===");
        System.err.println("Error: Invalid input provided.");
        System.err.println("Details: " + illegalArgumentException.getMessage());
        System.err.println("Suggestion: Please check your input and try again.");
        System.err.println("===================\n");
    }
    
    /**
     * Handles unexpected system exceptions.
     * 
     * Catches any exceptions not specifically handled by other methods.
     * Logs full stack trace for debugging purposes.
     * 
     * @param exception the system exception to handle
     */
    private static void handleSystem(Exception exception) {
        System.err.println("\n=== SYSTEM ERROR ===");
        System.err.println("Error: An unexpected error occurred.");
        System.err.println("Details: " + exception.getMessage());
        System.err.println("Type: " + exception.getClass().getSimpleName());
        
        System.err.println("\nStack trace:");
        exception.printStackTrace();
        
        System.err.println("Suggestion: Please contact system administrator if problem persists.");
        System.err.println("====================\n");
    }
    
    /**
     * Handles an exception and returns a user-friendly message string.
     * 
     * Useful for GUI applications where messages need to be displayed in dialogs.
     * 
     * @param exception the exception to handle
     * @return user-friendly error message
     */
    public static String getErrorMessage(Exception exception) {
        if (exception == null) {
            return "Unknown error occurred";
        } else if (exception instanceof ValidationException validationException) {
            return validationException.getErrorType().getDefaultMessage() +
                   "\n\n" + exception.getMessage();
        } else if (exception instanceof PersistenceException) {
            return "Failed to save or load data.\n\n" + exception.getMessage();
        } else if (exception instanceof IllegalArgumentException) {
            return "Invalid input provided.\n\n" + exception.getMessage();
        } else {
            return "An unexpected error occurred.\n\n" + exception.getMessage();
        }
    }
    
    /**
     * Checks if an exception is recoverable (user can retry).
     * 
     * Validation and input exceptions are typically recoverable,
     * whilst system exceptions may not be.
     * 
     * @param exception the exception to check
     * @return true if user can retry the operation, false otherwise
     */
    public static boolean isRecoverable(Exception exception) {
        return exception instanceof ValidationException ||
               exception instanceof IllegalArgumentException;
    }
}
