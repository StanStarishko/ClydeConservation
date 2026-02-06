package com.conservation.ui;

import com.conservation.util.DateUtils;
import com.conservation.util.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

/**
 * Utility class for validating and parsing user input from the console.
 * Provides methods for validating different input types with appropriate error messages.
 * 
 * <p>All methods handle invalid input gracefully, prompting the user to re-enter
 * until valid input is provided or the operation is cancelled.
 * 
 * <p>Input validation includes:
 * <ul>
 *   <li>Integer input within specified ranges</li>
 *   <li>String input matching regex patterns</li>
 *   <li>Date input in DD-MM-YYYY format</li>
 *   <li>Yes/No confirmation input</li>
 *   <li>Menu selection input</li>
 * </ul>
 * 
 * @author Clyde Conservation Development Team
 * @version 1.0
 */
public class InputValidator {

    // ============================================================
    // Constants
    // ============================================================
    
    /** Maximum number of retry attempts before giving up */
    private static final int MAX_RETRIES = 5;
    
    /** Regex for valid names (letters, spaces, hyphens) */
    public static final String NAME_PATTERN = "^[A-Za-z][A-Za-z\\s\\-']{1,49}$";
    
    /** Regex for valid UK phone numbers */
    public static final String UK_PHONE_PATTERN = "^(0[1-9][0-9]{8,9}|\\+44[1-9][0-9]{8,9})$";
    
    /** Regex for valid addresses */
    public static final String ADDRESS_PATTERN = "^[A-Za-z0-9][A-Za-z0-9\\s,.'\\-]{4,99}$";
    
    /** Regex for valid cage numbers (e.g., Large-01, Medium-02) */
    public static final String CAGE_NUMBER_PATTERN = "^[A-Za-z][A-Za-z0-9\\-]{2,19}$";

    // ============================================================
    // Instance Variables
    // ============================================================
    
    private final Scanner scanner;

    // ============================================================
    // Constructor
    // ============================================================
    
    /**
     * Creates a new InputValidator with the specified Scanner.
     * 
     * @param scanner the Scanner to use for reading input
     */
    public InputValidator(Scanner scanner) {
        this.scanner = scanner;
    }

    // ============================================================
    // Integer Input Methods
    // ============================================================
    
    /**
     * Validates and returns an integer input within the specified range.
     * Prompts the user repeatedly until valid input is provided.
     * 
     * @param prompt   the prompt to display to the user
     * @param minValue the minimum acceptable value (inclusive)
     * @param maxValue the maximum acceptable value (inclusive)
     * @return the validated integer, or -1 if max retries exceeded
     */
    public int validateIntInput(String prompt, int minValue, int maxValue) {
        int retryCount = 0;
        
        while (retryCount < MAX_RETRIES) {
            DisplayFormatter.displayPrompt(prompt + " (" + minValue + "-" + maxValue + ")");
            String input = scanner.nextLine().trim();
            
            // Check for cancel command
            if (isCancelCommand(input)) {
                return -1;
            }
            
            try {
                int value = Integer.parseInt(input);
                
                if (value >= minValue && value <= maxValue) {
                    return value;
                } else {
                    DisplayFormatter.printError(
                        "Please enter a number between " + minValue + " and " + maxValue + "."
                    );
                }
            } catch (NumberFormatException exception) {
                DisplayFormatter.printError("Invalid number format. Please enter a whole number.");
            }
            
            retryCount++;
        }
        
        DisplayFormatter.printError("Maximum retries exceeded. Returning to menu.");
        return -1;
    }
    
    /**
     * Validates a positive integer input (greater than zero).
     * 
     * @param prompt the prompt to display to the user
     * @return the validated positive integer, or -1 if invalid
     */
    public int validatePositiveInt(String prompt) {
        return validateIntInput(prompt, 1, Integer.MAX_VALUE);
    }
    
    /**
     * Validates a menu choice input.
     * 
     * @param optionCount the number of menu options available
     * @return the selected option (1-based), or -1 if invalid
     */
    public int validateMenuChoice(int optionCount) {
        DisplayFormatter.displayMenuPrompt(1, optionCount);
        String input = scanner.nextLine().trim();
        
        // Check for back/quit commands
        if (isBackCommand(input)) {
            return 0; // Signal to go back
        }
        
        try {
            int choice = Integer.parseInt(input);
            if (choice >= 1 && choice <= optionCount) {
                return choice;
            }
        } catch (NumberFormatException ignored) {
            // Fall through to error
        }
        
        DisplayFormatter.printError("Invalid option. Please select 1-" + optionCount + ".");
        return -1;
    }

    // ============================================================
    // String Input Methods
    // ============================================================
    
    /**
     * Validates a string input against a regex pattern.
     * 
     * @param prompt       the prompt to display to the user
     * @param pattern      the regex pattern to match
     * @param errorMessage the error message if validation fails
     * @return the validated string, or null if max retries exceeded
     */
    public String validateStringInput(String prompt, String pattern, String errorMessage) {
        int retryCount = 0;
        
        while (retryCount < MAX_RETRIES) {
            DisplayFormatter.displayPrompt(prompt);
            String input = scanner.nextLine().trim();
            
            // Check for cancel command
            if (isCancelCommand(input)) {
                return null;
            }
            
            if (StringUtils.hasContent(input) && input.matches(pattern)) {
                return input;
            } else {
                DisplayFormatter.printError(errorMessage);
            }
            
            retryCount++;
        }
        
        DisplayFormatter.printError("Maximum retries exceeded. Returning to menu.");
        return null;
    }
    
    /**
     * Validates a name input (letters, spaces, hyphens, apostrophes).
     * 
     * @param prompt the prompt to display to the user
     * @return the validated name, or null if invalid
     */
    public String validateNameInput(String prompt) {
        return validateStringInput(
            prompt,
            NAME_PATTERN,
            "Invalid name. Please use letters only (2-50 characters)."
        );
    }
    
    /**
     * Validates a UK phone number input.
     * 
     * @param prompt the prompt to display to the user
     * @return the validated phone number, or null if invalid
     */
    public String validatePhoneInput(String prompt) {
        return validateStringInput(
            prompt,
            UK_PHONE_PATTERN,
            "Invalid phone number. Please enter a valid UK number (e.g., 07123456789)."
        );
    }
    
    /**
     * Validates an address input.
     * 
     * @param prompt the prompt to display to the user
     * @return the validated address, or null if invalid
     */
    public String validateAddressInput(String prompt) {
        return validateStringInput(
            prompt,
            ADDRESS_PATTERN,
            "Invalid address. Please enter a valid address (5-100 characters)."
        );
    }
    
    /**
     * Validates a cage number input.
     * 
     * @param prompt the prompt to display to the user
     * @return the validated cage number, or null if invalid
     */
    public String validateCageNumberInput(String prompt) {
        return validateStringInput(
            prompt,
            CAGE_NUMBER_PATTERN,
            "Invalid cage number. Please use format like 'Large-01' (3-20 characters)."
        );
    }
    
    /**
     * Validates a non-empty string input without pattern matching.
     * 
     * @param prompt the prompt to display to the user
     * @return the validated non-empty string, or null if invalid
     */
    public String validateNonEmptyInput(String prompt) {
        int retryCount = 0;
        
        while (retryCount < MAX_RETRIES) {
            DisplayFormatter.displayPrompt(prompt);
            String input = scanner.nextLine().trim();
            
            if (isCancelCommand(input)) {
                return null;
            }
            
            if (StringUtils.hasContent(input)) {
                return input;
            } else {
                DisplayFormatter.printError("This field cannot be empty. Please enter a value.");
            }
            
            retryCount++;
        }
        
        DisplayFormatter.printError("Maximum retries exceeded. Returning to menu.");
        return null;
    }

    // ============================================================
    // Date Input Methods
    // ============================================================
    
    /**
     * Validates a date input in DD-MM-YYYY format.
     * 
     * @param prompt the prompt to display to the user
     * @return the validated LocalDate, or null if invalid
     */
    public LocalDate validateDateInput(String prompt) {
        int retryCount = 0;
        
        while (retryCount < MAX_RETRIES) {
            DisplayFormatter.displayPrompt(prompt + " (DD-MM-YYYY)");
            String input = scanner.nextLine().trim();
            
            if (isCancelCommand(input)) {
                return null;
            }
            
            try {
                LocalDate date = DateUtils.parseFromUI(input);
                return date;
            } catch (DateTimeParseException exception) {
                DisplayFormatter.printError(
                    "Invalid date format. Please use DD-MM-YYYY (e.g., 15-05-2020)."
                );
            }
            
            retryCount++;
        }
        
        DisplayFormatter.printError("Maximum retries exceeded. Returning to menu.");
        return null;
    }
    
    /**
     * Validates a date input that must not be in the future.
     * 
     * @param prompt the prompt to display to the user
     * @return the validated LocalDate (not in future), or null if invalid
     */
    public LocalDate validatePastOrPresentDate(String prompt) {
        int retryCount = 0;
        
        while (retryCount < MAX_RETRIES) {
            DisplayFormatter.displayPrompt(prompt + " (DD-MM-YYYY)");
            String input = scanner.nextLine().trim();
            
            if (isCancelCommand(input)) {
                return null;
            }
            
            try {
                LocalDate date = DateUtils.parseFromUI(input);
                
                if (DateUtils.isNotFuture(date)) {
                    return date;
                } else {
                    DisplayFormatter.printError("Date cannot be in the future.");
                }
            } catch (DateTimeParseException exception) {
                DisplayFormatter.printError(
                    "Invalid date format. Please use DD-MM-YYYY (e.g., 15-05-2020)."
                );
            }
            
            retryCount++;
        }
        
        DisplayFormatter.printError("Maximum retries exceeded. Returning to menu.");
        return null;
    }
    
    /**
     * Validates a date input that must be after another date.
     * 
     * @param prompt    the prompt to display to the user
     * @param afterDate the date that input must be after
     * @param afterDesc description of the after date (for error message)
     * @return the validated LocalDate, or null if invalid
     */
    public LocalDate validateDateAfter(String prompt, LocalDate afterDate, String afterDesc) {
        int retryCount = 0;
        
        while (retryCount < MAX_RETRIES) {
            DisplayFormatter.displayPrompt(prompt + " (DD-MM-YYYY)");
            String input = scanner.nextLine().trim();
            
            if (isCancelCommand(input)) {
                return null;
            }
            
            try {
                LocalDate date = DateUtils.parseFromUI(input);
                
                if (!date.isBefore(afterDate)) {
                    return date;
                } else {
                    DisplayFormatter.printError(
                        "Date must be on or after " + afterDesc + 
                        " (" + DateUtils.formatForUI(afterDate) + ")."
                    );
                }
            } catch (DateTimeParseException exception) {
                DisplayFormatter.printError(
                    "Invalid date format. Please use DD-MM-YYYY (e.g., 15-05-2020)."
                );
            }
            
            retryCount++;
        }
        
        DisplayFormatter.printError("Maximum retries exceeded. Returning to menu.");
        return null;
    }

    // ============================================================
    // Confirmation Input Methods
    // ============================================================
    
    /**
     * Validates a Yes/No confirmation input.
     * 
     * @param message the confirmation message to display
     * @return true for Yes, false for No
     */
    public boolean validateConfirmation(String message) {
        int retryCount = 0;
        
        while (retryCount < MAX_RETRIES) {
            DisplayFormatter.displayConfirmationPrompt(message);
            String input = scanner.nextLine().trim().toUpperCase();
            
            if ("Y".equals(input) || "YES".equals(input)) {
                return true;
            } else if ("N".equals(input) || "NO".equals(input)) {
                return false;
            } else {
                DisplayFormatter.printError("Please enter Y for Yes or N for No.");
            }
            
            retryCount++;
        }
        
        // Default to No if max retries exceeded
        return false;
    }

    // ============================================================
    // Selection Input Methods
    // ============================================================
    
    /**
     * Validates selection from a list of string options.
     * 
     * @param prompt  the prompt to display
     * @param options the available options
     * @return the selected option (1-based index), or -1 if invalid
     */
    public int validateChoice(String prompt, String[] options) {
        DisplayFormatter.printBlankLine();
        DisplayFormatter.printInfo(prompt);
        
        for (int optionIndex = 0; optionIndex < options.length; optionIndex++) {
            System.out.printf("  %d. %s%n", optionIndex + 1, options[optionIndex]);
        }
        
        DisplayFormatter.printBlankLine();
        return validateIntInput("Select option", 1, options.length);
    }
    
    /**
     * Validates an ID input (positive integer).
     * 
     * @param prompt the prompt to display
     * @return the validated ID, or -1 if invalid
     */
    public int validateIdInput(String prompt) {
        DisplayFormatter.displayPrompt(prompt);
        String input = scanner.nextLine().trim();
        
        if (isCancelCommand(input) || isBackCommand(input)) {
            return -1;
        }
        
        try {
            int idValue = Integer.parseInt(input);
            if (idValue > 0) {
                return idValue;
            } else {
                DisplayFormatter.printError("ID must be a positive number.");
            }
        } catch (NumberFormatException exception) {
            DisplayFormatter.printError("Invalid ID format. Please enter a number.");
        }
        
        return -1;
    }

    // ============================================================
    // Wait for Input Methods
    // ============================================================
    
    /**
     * Waits for the user to press ENTER to continue.
     */
    public void waitForEnter() {
        DisplayFormatter.displayContinuePrompt();
        scanner.nextLine();
    }
    
    /**
     * Reads raw input without validation.
     * 
     * @param prompt the prompt to display
     * @return the raw input string
     */
    public String readRawInput(String prompt) {
        DisplayFormatter.displayPrompt(prompt);
        return scanner.nextLine().trim();
    }

    // ============================================================
    // Helper Methods
    // ============================================================
    
    /**
     * Checks if the input is a cancel command.
     * 
     * @param input the user input
     * @return true if input is a cancel command
     */
    private boolean isCancelCommand(String input) {
        if (input == null) {
            return false;
        }
        String upperInput = input.toUpperCase();
        return "CANCEL".equals(upperInput) || "C".equals(upperInput) || "Q".equals(upperInput);
    }
    
    /**
     * Checks if the input is a back command.
     * 
     * @param input the user input
     * @return true if input is a back command
     */
    private boolean isBackCommand(String input) {
        if (input == null) {
            return false;
        }
        String upperInput = input.toUpperCase();
        return "BACK".equals(upperInput) || "B".equals(upperInput) || "0".equals(input);
    }
}
