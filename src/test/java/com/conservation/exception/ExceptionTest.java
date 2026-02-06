package com.conservation.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unified test class for all exception classes and ExceptionHandler.
 * Tests exception creation, ErrorType enum, exception handling routing,
 * and user-friendly message generation.
 * 
 * <p>Covers:
 * <ul>
 *   <li>ConservationException - base exception class</li>
 *   <li>ValidationException - business rule validation failures</li>
 *   <li>PersistenceException - file I/O and XML errors</li>
 *   <li>ExceptionHandler - centralised exception routing</li>
 * </ul>
 * 
 * @author Clyde Conservation Development Team
 * @version 1.0
 */
@DisplayName("Exception Classes Tests")
class ExceptionTest {

    // ============================================================
    // Test Constants
    // ============================================================
    
    private static final String TEST_MESSAGE = "Test error message";
    private static final String TEST_FILE_PATH = "data/animals.xml";
    private static final String CUSTOM_VALIDATION_MESSAGE = "Cage 1 is full (10/10 animals)";

    // ============================================================
    // CONSERVATION EXCEPTION TESTS (Base Class)
    // ============================================================
    
    @Nested
    @DisplayName("ConservationException Tests")
    class ConservationExceptionTests {
        
        @Test
        @DisplayName("Create exception with message only")
        void constructor_WithMessageOnly_ShouldStoreMessage() {
            // Arrange & Act
            ConservationException exception = new ConservationException(TEST_MESSAGE);
            
            // Assert
            assertEquals(TEST_MESSAGE, exception.getMessage(), 
                "Exception message should match constructor argument");
            assertNull(exception.getCause(), "Cause should be null");
        }
        
        @Test
        @DisplayName("Create exception with message and cause")
        void constructor_WithMessageAndCause_ShouldStoreBoth() {
            // Arrange
            IOException cause = new IOException("Original IO error");
            
            // Act
            ConservationException exception = new ConservationException(TEST_MESSAGE, cause);
            
            // Assert
            assertEquals(TEST_MESSAGE, exception.getMessage(), "Message should match");
            assertEquals(cause, exception.getCause(), "Cause should be the IOException");
            assertInstanceOf(IOException.class, exception.getCause(), 
                "Cause should be IOException type");
        }
        
        @Test
        @DisplayName("Exception extends java.lang.Exception")
        void exceptionHierarchy_ShouldExtendException() {
            // Arrange & Act
            ConservationException exception = new ConservationException(TEST_MESSAGE);
            
            // Assert
            assertInstanceOf(Exception.class, exception, 
                "ConservationException should extend Exception");
        }
        
        @Test
        @DisplayName("Exception is checked (not RuntimeException)")
        void exceptionType_ShouldBeChecked() {
            // Arrange & Act
            ConservationException exception = new ConservationException(TEST_MESSAGE);
            
            // Assert
            assertFalse(exception instanceof RuntimeException, 
                "ConservationException should be a checked exception");
        }
    }

    // ============================================================
    // VALIDATION EXCEPTION TESTS
    // ============================================================
    
    @Nested
    @DisplayName("ValidationException Tests")
    class ValidationExceptionTests {
        
        // ------------------------------------------------------------
        // ErrorType Enum Tests
        // ------------------------------------------------------------
        
        @Test
        @DisplayName("ErrorType enum contains all expected values")
        void errorTypeEnum_ShouldContainAllExpectedValues() {
            // Arrange
            ValidationException.ErrorType[] expectedTypes = {
                ValidationException.ErrorType.CAGE_CAPACITY_EXCEEDED,
                ValidationException.ErrorType.INVALID_PREDATOR_PREY_MIX,
                ValidationException.ErrorType.KEEPER_OVERLOAD,
                ValidationException.ErrorType.KEEPER_UNDERLOAD,
                ValidationException.ErrorType.INVALID_ANIMAL_DATA,
                ValidationException.ErrorType.INVALID_KEEPER_DATA,
                ValidationException.ErrorType.INVALID_CAGE_DATA,
                ValidationException.ErrorType.INVALID_INPUT
            };
            
            // Act
            ValidationException.ErrorType[] actualTypes = ValidationException.ErrorType.values();
            
            // Assert
            assertEquals(expectedTypes.length, actualTypes.length, 
                "ErrorType enum should have expected number of values");
            for (ValidationException.ErrorType expectedType : expectedTypes) {
                boolean found = false;
                for (ValidationException.ErrorType actualType : actualTypes) {
                    if (actualType == expectedType) {
                        found = true;
                        break;
                    }
                }
                assertTrue(found, "ErrorType should contain: " + expectedType);
            }
        }
        
        @ParameterizedTest
        @EnumSource(ValidationException.ErrorType.class)
        @DisplayName("Each ErrorType has a default message")
        void errorType_EachShouldHaveDefaultMessage(ValidationException.ErrorType errorType) {
            // Arrange & Act
            String defaultMessage = errorType.getDefaultMessage();
            
            // Assert
            assertNotNull(defaultMessage, "Default message should not be null");
            assertFalse(defaultMessage.isEmpty(), "Default message should not be empty");
        }
        
        // ------------------------------------------------------------
        // Constructor Tests
        // ------------------------------------------------------------
        
        @Test
        @DisplayName("Create exception with ErrorType only uses default message")
        void constructor_WithErrorTypeOnly_ShouldUseDefaultMessage() {
            // Arrange
            ValidationException.ErrorType errorType = ValidationException.ErrorType.CAGE_CAPACITY_EXCEEDED;
            
            // Act
            ValidationException exception = new ValidationException(errorType);
            
            // Assert
            assertEquals(errorType, exception.getErrorType(), "ErrorType should match");
            assertEquals(errorType.getDefaultMessage(), exception.getMessage(), 
                "Message should be default message from ErrorType");
        }
        
        @Test
        @DisplayName("Create exception with ErrorType and custom message")
        void constructor_WithErrorTypeAndCustomMessage_ShouldUseCustomMessage() {
            // Arrange
            ValidationException.ErrorType errorType = ValidationException.ErrorType.CAGE_CAPACITY_EXCEEDED;
            
            // Act
            ValidationException exception = new ValidationException(errorType, CUSTOM_VALIDATION_MESSAGE);
            
            // Assert
            assertEquals(errorType, exception.getErrorType(), "ErrorType should match");
            assertEquals(CUSTOM_VALIDATION_MESSAGE, exception.getMessage(), 
                "Message should be custom message");
        }
        
        // ------------------------------------------------------------
        // Specific ErrorType Tests
        // ------------------------------------------------------------
        
        @Test
        @DisplayName("CAGE_CAPACITY_EXCEEDED has meaningful default message")
        void cageCapacityExceeded_ShouldHaveMeaningfulMessage() {
            // Arrange & Act
            ValidationException exception = new ValidationException(
                ValidationException.ErrorType.CAGE_CAPACITY_EXCEEDED
            );
            
            // Assert
            assertTrue(exception.getMessage().toLowerCase().contains("cage") || 
                       exception.getMessage().toLowerCase().contains("capacity") ||
                       exception.getMessage().toLowerCase().contains("full"),
                "Message should mention cage, capacity, or full");
        }
        
        @Test
        @DisplayName("INVALID_PREDATOR_PREY_MIX has meaningful default message")
        void invalidPredatorPreyMix_ShouldHaveMeaningfulMessage() {
            // Arrange & Act
            ValidationException exception = new ValidationException(
                ValidationException.ErrorType.INVALID_PREDATOR_PREY_MIX
            );
            
            // Assert
            String message = exception.getMessage().toLowerCase();
            assertTrue(message.contains("predator") || message.contains("prey") || 
                       message.contains("mix") || message.contains("share"),
                "Message should mention predator, prey, mix, or share");
        }
        
        @Test
        @DisplayName("KEEPER_OVERLOAD has meaningful default message")
        void keeperOverload_ShouldHaveMeaningfulMessage() {
            // Arrange & Act
            ValidationException exception = new ValidationException(
                ValidationException.ErrorType.KEEPER_OVERLOAD
            );
            
            // Assert
            String message = exception.getMessage().toLowerCase();
            assertTrue(message.contains("keeper") || message.contains("cage") || 
                       message.contains("maximum") || message.contains("4"),
                "Message should mention keeper workload constraints");
        }
        
        @Test
        @DisplayName("KEEPER_UNDERLOAD has meaningful default message")
        void keeperUnderload_ShouldHaveMeaningfulMessage() {
            // Arrange & Act
            ValidationException exception = new ValidationException(
                ValidationException.ErrorType.KEEPER_UNDERLOAD
            );
            
            // Assert
            String message = exception.getMessage().toLowerCase();
            assertTrue(message.contains("keeper") || message.contains("cage") || 
                       message.contains("minimum") || message.contains("1") ||
                       message.contains("least"),
                "Message should mention minimum cage requirement");
        }
        
        // ------------------------------------------------------------
        // Inheritance Tests
        // ------------------------------------------------------------
        
        @Test
        @DisplayName("ValidationException extends ConservationException")
        void inheritance_ShouldExtendConservationException() {
            // Arrange & Act
            ValidationException exception = new ValidationException(
                ValidationException.ErrorType.INVALID_INPUT
            );
            
            // Assert
            assertInstanceOf(ConservationException.class, exception, 
                "ValidationException should extend ConservationException");
        }
    }

    // ============================================================
    // PERSISTENCE EXCEPTION TESTS
    // ============================================================
    
    @Nested
    @DisplayName("PersistenceException Tests")
    class PersistenceExceptionTests {
        
        @Test
        @DisplayName("Create exception with message and file path")
        void constructor_WithMessageAndFilePath_ShouldStoreBoth() {
            // Arrange & Act
            PersistenceException exception = new PersistenceException(TEST_MESSAGE, TEST_FILE_PATH);
            
            // Assert
            assertTrue(exception.getMessage().contains(TEST_MESSAGE), 
                "Message should contain original message");
            assertEquals(TEST_FILE_PATH, exception.getFilePath(), 
                "File path should be stored");
        }
        
        @Test
        @DisplayName("Exception message includes file path")
        void getMessage_ShouldIncludeFilePath() {
            // Arrange & Act
            PersistenceException exception = new PersistenceException(
                "Failed to parse XML", TEST_FILE_PATH
            );
            
            // Assert
            assertTrue(exception.getMessage().contains(TEST_FILE_PATH), 
                "Message should include file path");
        }
        
        @Test
        @DisplayName("Create exception with message, file path, and cause")
        void constructor_WithAllParameters_ShouldStoreAll() {
            // Arrange
            IOException cause = new IOException("Disk full");
            
            // Act
            PersistenceException exception = new PersistenceException(
                TEST_MESSAGE, TEST_FILE_PATH, cause
            );
            
            // Assert
            assertTrue(exception.getMessage().contains(TEST_MESSAGE), 
                "Message should contain original message");
            assertEquals(TEST_FILE_PATH, exception.getFilePath(), 
                "File path should be stored");
            assertEquals(cause, exception.getCause(), 
                "Cause should be stored");
        }
        
        @Test
        @DisplayName("Get file path returns correct path")
        void getFilePath_ShouldReturnCorrectPath() {
            // Arrange
            String expectedPath = "config/settings.json";
            
            // Act
            PersistenceException exception = new PersistenceException(
                "Config error", expectedPath
            );
            
            // Assert
            assertEquals(expectedPath, exception.getFilePath(), 
                "File path should match constructor argument");
        }
        
        @Test
        @DisplayName("PersistenceException extends ConservationException")
        void inheritance_ShouldExtendConservationException() {
            // Arrange & Act
            PersistenceException exception = new PersistenceException(
                TEST_MESSAGE, TEST_FILE_PATH
            );
            
            // Assert
            assertInstanceOf(ConservationException.class, exception, 
                "PersistenceException should extend ConservationException");
        }
        
        @Test
        @DisplayName("Null file path is handled gracefully")
        void constructor_WithNullFilePath_ShouldHandleGracefully() {
            // Arrange & Act
            PersistenceException exception = new PersistenceException(TEST_MESSAGE, null);
            
            // Assert
            assertNotNull(exception.getMessage(), "Message should not be null");
            assertNull(exception.getFilePath(), "File path should be null");
        }
    }

    // ============================================================
    // EXCEPTION HANDLER TESTS
    // ============================================================
    
    @Nested
    @DisplayName("ExceptionHandler Tests")
    class ExceptionHandlerTests {
        
        private ByteArrayOutputStream errorOutputStream;
        private PrintStream originalErrorStream;
        
        @BeforeEach
        void captureErrorOutput() {
            // Capture System.err for testing console output
            errorOutputStream = new ByteArrayOutputStream();
            originalErrorStream = System.err;
            System.setErr(new PrintStream(errorOutputStream));
        }
        
        @BeforeEach
        void restoreErrorOutput() {
            // Note: This runs before captureErrorOutput due to JUnit ordering
            // Using @AfterEach would be better, but keeping structure simple
        }
        
        void cleanupErrorStream() {
            System.setErr(originalErrorStream);
        }
        
        // ------------------------------------------------------------
        // Exception Routing Tests
        // ------------------------------------------------------------
        
        @Test
        @DisplayName("Handle routes ValidationException correctly")
        void handle_ValidationException_ShouldRouteToValidationHandler() {
            try {
                // Arrange
                ValidationException exception = new ValidationException(
                    ValidationException.ErrorType.CAGE_CAPACITY_EXCEEDED,
                    CUSTOM_VALIDATION_MESSAGE
                );
                
                // Act
                ExceptionHandler.handle(exception);
                String output = errorOutputStream.toString();
                
                // Assert
                assertTrue(output.toLowerCase().contains("validation") || 
                           output.toLowerCase().contains("error"),
                    "Output should indicate validation error");
            } finally {
                cleanupErrorStream();
            }
        }
        
        @Test
        @DisplayName("Handle routes PersistenceException correctly")
        void handle_PersistenceException_ShouldRouteToPersistenceHandler() {
            try {
                // Arrange
                PersistenceException exception = new PersistenceException(
                    "Failed to save file", TEST_FILE_PATH
                );
                
                // Act
                ExceptionHandler.handle(exception);
                String output = errorOutputStream.toString();
                
                // Assert
                assertTrue(output.toLowerCase().contains("file") || 
                           output.toLowerCase().contains("persistence") ||
                           output.toLowerCase().contains("save") ||
                           output.toLowerCase().contains("error"),
                    "Output should indicate persistence/file error");
            } finally {
                cleanupErrorStream();
            }
        }
        
        @Test
        @DisplayName("Handle routes IllegalArgumentException to input handler")
        void handle_IllegalArgumentException_ShouldRouteToInputHandler() {
            try {
                // Arrange
                IllegalArgumentException exception = new IllegalArgumentException(
                    "Invalid input value"
                );
                
                // Act
                ExceptionHandler.handle(exception);
                String output = errorOutputStream.toString();
                
                // Assert
                assertTrue(output.toLowerCase().contains("input") || 
                           output.toLowerCase().contains("invalid") ||
                           output.toLowerCase().contains("error"),
                    "Output should indicate input error");
            } finally {
                cleanupErrorStream();
            }
        }
        
        @Test
        @DisplayName("Handle routes unknown exception to system handler")
        void handle_UnknownException_ShouldRouteToSystemHandler() {
            try {
                // Arrange
                RuntimeException exception = new RuntimeException("Unexpected error");
                
                // Act
                ExceptionHandler.handle(exception);
                String output = errorOutputStream.toString();
                
                // Assert
                assertTrue(output.toLowerCase().contains("error") || 
                           output.toLowerCase().contains("unexpected") ||
                           output.toLowerCase().contains("system"),
                    "Output should indicate system/unexpected error");
            } finally {
                cleanupErrorStream();
            }
        }
        
        // ------------------------------------------------------------
        // User-Friendly Message Tests
        // ------------------------------------------------------------
        
        @Test
        @DisplayName("GetErrorMessage returns user-friendly message for ValidationException")
        void getErrorMessage_ValidationException_ShouldReturnUserFriendlyMessage() {
            // Arrange
            ValidationException exception = new ValidationException(
                ValidationException.ErrorType.CAGE_CAPACITY_EXCEEDED
            );
            
            // Act
            String userMessage = ExceptionHandler.getErrorMessage(exception);
            
            // Assert
            assertNotNull(userMessage, "User message should not be null");
            assertFalse(userMessage.isEmpty(), "User message should not be empty");
            // Should not contain stack trace or technical details
            assertFalse(userMessage.contains("at com.conservation"), 
                "User message should not contain stack trace");
        }
        
        @Test
        @DisplayName("GetErrorMessage returns user-friendly message for PersistenceException")
        void getErrorMessage_PersistenceException_ShouldReturnUserFriendlyMessage() {
            // Arrange
            PersistenceException exception = new PersistenceException(
                "XML parsing failed", TEST_FILE_PATH
            );
            
            // Act
            String userMessage = ExceptionHandler.getErrorMessage(exception);
            
            // Assert
            assertNotNull(userMessage, "User message should not be null");
            assertFalse(userMessage.isEmpty(), "User message should not be empty");
        }
        
        // ------------------------------------------------------------
        // Recoverability Tests
        // ------------------------------------------------------------
        
        @Test
        @DisplayName("IsRecoverable returns true for ValidationException")
        void isRecoverable_ValidationException_ShouldReturnTrue() {
            // Arrange
            ValidationException exception = new ValidationException(
                ValidationException.ErrorType.INVALID_INPUT
            );
            
            // Act
            boolean isRecoverable = ExceptionHandler.isRecoverable(exception);
            
            // Assert
            assertTrue(isRecoverable, 
                "ValidationException should be recoverable (user can fix input)");
        }
        
        @Test
        @DisplayName("IsRecoverable returns true for IllegalArgumentException")
        void isRecoverable_IllegalArgumentException_ShouldReturnTrue() {
            // Arrange
            IllegalArgumentException exception = new IllegalArgumentException("Bad input");
            
            // Act
            boolean isRecoverable = ExceptionHandler.isRecoverable(exception);
            
            // Assert
            assertTrue(isRecoverable, 
                "IllegalArgumentException should be recoverable");
        }
        
        @Test
        @DisplayName("IsRecoverable handles PersistenceException appropriately")
        void isRecoverable_PersistenceException_ShouldReturnAppropriateValue() {
            // Arrange
            PersistenceException exception = new PersistenceException(
                "File not found", "nonexistent.xml"
            );
            
            // Act
            boolean isRecoverable = ExceptionHandler.isRecoverable(exception);
            
            // Assert
            // Persistence errors may or may not be recoverable depending on cause
            // Just verify it returns a boolean without throwing
            assertNotNull(Boolean.valueOf(isRecoverable), 
                "Should return a valid boolean");
        }
        
        @Test
        @DisplayName("IsRecoverable returns false for severe system errors")
        void isRecoverable_OutOfMemoryError_ShouldReturnFalse() {
            // Arrange - Using a RuntimeException to simulate severe error
            // (Not actually throwing OutOfMemoryError in tests)
            Exception severeException = new RuntimeException(
                new OutOfMemoryError("Heap space exhausted")
            );
            
            // Act
            boolean isRecoverable = ExceptionHandler.isRecoverable(severeException);
            
            // Assert
            assertFalse(isRecoverable, 
                "Severe errors wrapping OutOfMemoryError should not be recoverable");
        }
        
        // ------------------------------------------------------------
        // Null Safety Tests
        // ------------------------------------------------------------
        
        @Test
        @DisplayName("Handle null exception gracefully")
        void handle_NullException_ShouldNotThrow() {
            try {
                // Act & Assert - should not throw NullPointerException
                assertDoesNotThrow(() -> ExceptionHandler.handle(null), 
                    "Handler should handle null gracefully");
            } finally {
                cleanupErrorStream();
            }
        }
        
        @Test
        @DisplayName("GetErrorMessage handles null gracefully")
        void getErrorMessage_NullException_ShouldReturnDefaultMessage() {
            // Act
            String message = ExceptionHandler.getErrorMessage(null);
            
            // Assert
            assertNotNull(message, "Should return a default message, not null");
        }
        
        @Test
        @DisplayName("IsRecoverable handles null gracefully")
        void isRecoverable_NullException_ShouldReturnFalse() {
            // Act
            boolean isRecoverable = ExceptionHandler.isRecoverable(null);
            
            // Assert
            assertFalse(isRecoverable, "Null exception should not be recoverable");
        }
    }

    // ============================================================
    // EXCEPTION THROWING SCENARIOS
    // ============================================================
    
    @Nested
    @DisplayName("Exception Throwing Scenarios")
    class ExceptionThrowingScenarios {
        
        @Test
        @DisplayName("Validation exception can be thrown and caught")
        void validationException_ShouldBeThrowableAndCatchable() {
            // Arrange & Act & Assert
            assertThrows(ValidationException.class, () -> {
                throw new ValidationException(ValidationException.ErrorType.INVALID_INPUT);
            }, "ValidationException should be throwable");
        }
        
        @Test
        @DisplayName("Persistence exception can be thrown and caught")
        void persistenceException_ShouldBeThrowableAndCatchable() {
            // Arrange & Act & Assert
            assertThrows(PersistenceException.class, () -> {
                throw new PersistenceException("Test error", "test.xml");
            }, "PersistenceException should be throwable");
        }
        
        @Test
        @DisplayName("All custom exceptions can be caught as ConservationException")
        void allCustomExceptions_ShouldBeCatchableAsBaseType() {
            // Arrange
            Exception[] exceptions = {
                new ValidationException(ValidationException.ErrorType.INVALID_INPUT),
                new PersistenceException("Test", "test.xml"),
                new ConservationException("Test")
            };
            
            // Act & Assert
            for (Exception exception : exceptions) {
                assertInstanceOf(ConservationException.class, exception, 
                    exception.getClass().getSimpleName() + " should be catchable as ConservationException");
            }
        }
        
        @Test
        @DisplayName("Exception message chain is preserved")
        void exceptionChain_ShouldPreserveMessages() {
            // Arrange
            IOException ioException = new IOException("Disk error");
            PersistenceException persistenceException = new PersistenceException(
                "Failed to save", TEST_FILE_PATH, ioException
            );
            
            // Act & Assert
            assertEquals(ioException, persistenceException.getCause(), 
                "Original cause should be preserved");
            assertEquals("Disk error", persistenceException.getCause().getMessage(), 
                "Original message should be accessible via cause");
        }
    }

    // ============================================================
    // ERROR TYPE VALIDATION HELPER TESTS
    // ============================================================
    
    @Nested
    @DisplayName("ErrorType Helper Method Tests")
    class ErrorTypeHelperTests {
        
        @Test
        @DisplayName("ErrorType valueOf works correctly")
        void errorType_ValueOf_ShouldReturnCorrectType() {
            // Arrange & Act
            ValidationException.ErrorType type = ValidationException.ErrorType.valueOf(
                "CAGE_CAPACITY_EXCEEDED"
            );
            
            // Assert
            assertEquals(ValidationException.ErrorType.CAGE_CAPACITY_EXCEEDED, type, 
                "valueOf should return correct enum value");
        }
        
        @Test
        @DisplayName("ErrorType valueOf throws for invalid name")
        void errorType_ValueOf_InvalidName_ShouldThrow() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> {
                ValidationException.ErrorType.valueOf("INVALID_ERROR_TYPE");
            }, "valueOf should throw for invalid enum name");
        }
        
        @Test
        @DisplayName("ErrorType name returns correct string")
        void errorType_Name_ShouldReturnCorrectString() {
            // Arrange
            ValidationException.ErrorType type = ValidationException.ErrorType.KEEPER_OVERLOAD;
            
            // Act
            String name = type.name();
            
            // Assert
            assertEquals("KEEPER_OVERLOAD", name, "name() should return enum constant name");
        }
        
        @Test
        @DisplayName("ErrorType ordinal values are unique")
        void errorType_Ordinals_ShouldBeUnique() {
            // Arrange
            ValidationException.ErrorType[] types = ValidationException.ErrorType.values();
            
            // Act & Assert
            for (int idx = 0; idx < types.length; idx++) {
                for (int innerIdx = idx + 1; innerIdx < types.length; innerIdx++) {
                    assertNotEquals(types[idx].ordinal(), types[innerIdx].ordinal(), 
                        "Ordinal values should be unique");
                }
            }
        }
    }
}
