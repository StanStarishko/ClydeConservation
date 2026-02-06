package com.conservation.util;

/**
 * Utility class for string manipulation and validation.
 * 
 * Provides common string operations used throughout the conservation system:
 * - Validation (null, empty, blank checks)
 * - Formatting (capitalisation, trimming)
 * - Sanitisation (removing unwanted characters)
 */
public class StringUtils {
    
    /**
     * Private constructor to prevent instantiation.
     * This is a utility class with only static methods.
     */
    private StringUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
    
    /**
     * Checks if a string is null or empty.
     * 
     * @param str the string to check
     * @return true if string is null or empty (""), false otherwise
     */
    public static boolean isNullOrEmpty(String str) {
        return str == null || str.isEmpty();
    }
    
    /**
     * Checks if a string is null, empty, or contains only whitespace.
     * 
     * @param str the string to check
     * @return true if string is null, empty, or whitespace only, false otherwise
     */
    public static boolean isNullOrBlank(String str) {
        return str == null || str.trim().isEmpty();
    }
    
    /**
     * Checks if a string is not null and not empty.
     * 
     * Opposite of isNullOrEmpty().
     * 
     * @param str the string to check
     * @return true if string has content, false otherwise
     */
    public static boolean hasContent(String str) {
        return !isNullOrEmpty(str);
    }
    
    /**
     * Capitalises the first letter of a string.
     * 
     * Examples:
     * - "john" → "John"
     * - "SMITH" → "Smith"
     * - "mARY" → "Mary"
     * 
     * @param str the string to capitalise
     * @return string with first letter capitalised and rest lowercase
     */
    public static String capitalise(String str) {
        if (isNullOrEmpty(str)) {
            return str;
        }
        
        String trimmed = str.trim();
        if (trimmed.length() == 1) {
            return trimmed.toUpperCase();
        }
        
        return trimmed.substring(0, 1).toUpperCase() + 
               trimmed.substring(1).toLowerCase();
    }
    
    /**
     * Capitalises the first letter of each word in a string.
     * 
     * Words are separated by spaces.
     * 
     * Examples:
     * - "john smith" → "John Smith"
     * - "large predator cage" → "Large Predator Cage"
     * 
     * @param str the string to capitalise
     * @return string with each word capitalised
     */
    public static String capitaliseWords(String str) {
        if (isNullOrEmpty(str)) {
            return str;
        }
        
        String[] words = str.trim().split("\\s+");
        StringBuilder result = new StringBuilder();
        
        for (int wordIdx = 0; wordIdx < words.length; wordIdx++) {
            if (wordIdx > 0) {
                result.append(" ");
            }
            result.append(capitalise(words[wordIdx]));
        }
        
        return result.toString();
    }
    
    /**
     * Trims whitespace from both ends of a string.
     * 
     * Safe version that handles null input.
     * 
     * @param str the string to trim
     * @return trimmed string, or null if input was null
     */
    public static String safeTrim(String str) {
        return str == null ? null : str.trim();
    }
    
    /**
     * Removes all whitespace from a string.
     * 
     * Removes spaces, tabs, newlines, etc.
     * 
     * @param str the string to process
     * @return string with all whitespace removed
     */
    public static String removeWhitespace(String str) {
        if (isNullOrEmpty(str)) {
            return str;
        }
        return str.replaceAll("\\s+", "");
    }
    
    /**
     * Sanitises a string for use in file names.
     * 
     * Removes or replaces characters that are invalid in file names:
     * - Replaces spaces with underscores
     * - Removes special characters: / \ : * ? " < > |
     * 
     * @param str the string to sanitise
     * @return sanitised string safe for file names
     */
    public static String sanitiseForFileName(String str) {
        if (isNullOrEmpty(str)) {
            return str;
        }
        
        return str.trim()
                  .replaceAll("\\s+", "_")              // Replace spaces with underscores
                  .replaceAll("[/\\\\:*?\"<>|]", "");   // Remove invalid characters
    }
    
    /**
     * Truncates a string to a maximum length.
     * 
     * If string is longer than maxLength, truncates and adds "...".
     * 
     * @param str the string to truncate
     * @param maxLength maximum length (including "..." if truncated)
     * @return truncated string
     */
    public static String truncate(String str, int maxLength) {
        if (isNullOrEmpty(str) || str.length() <= maxLength) {
            return str;
        }
        
        if (maxLength <= 3) {
            return str.substring(0, maxLength);
        }
        
        return str.substring(0, maxLength - 3) + "...";
    }
    
    /**
     * Pads a string to a specific length with spaces on the right.
     * 
     * Useful for creating aligned console output.
     * 
     * @param str the string to pad
     * @param length the desired total length
     * @return padded string
     */
    public static String padRight(String str, int length) {
        if (str == null) {
            str = "";
        }
        
        if (str.length() >= length) {
            return str;
        }
        
        return str + " ".repeat(length - str.length());
    }
    
    /**
     * Pads a string to a specific length with spaces on the left.
     * 
     * Useful for right-aligning numbers in console output.
     * 
     * @param str the string to pad
     * @param length the desired total length
     * @return padded string
     */
    public static String padLeft(String str, int length) {
        if (str == null) {
            str = "";
        }
        
        if (str.length() >= length) {
            return str;
        }
        
        return " ".repeat(length - str.length()) + str;
    }
    
    /**
     * Validates that a string matches a regular expression pattern.
     * 
     * @param str the string to validate
     * @param regex the regular expression pattern
     * @return true if string matches pattern, false otherwise
     */
    public static boolean matches(String str, String regex) {
        if (isNullOrEmpty(str) || isNullOrEmpty(regex)) {
            return false;
        }
        return str.matches(regex);
    }
    
    /**
     * Validates a UK phone number format.
     * 
     * Accepts formats:
     * - 07123456789 (mobile)
     * - 01412345678 (landline)
     * - +447123456789 (international mobile)
     * - +441412345678 (international landline)
     * 
     * @param phoneNumber the phone number to validate
     * @return true if valid UK phone number format, false otherwise
     */
    public static boolean isValidUKPhoneNumber(String phoneNumber) {
        if (isNullOrBlank(phoneNumber)) {
            return false;
        }
        
        String cleaned = phoneNumber.trim().replaceAll("\\s+", "");
        
        // UK mobile: 07xxx xxxxxx or +447xxx xxxxxx
        // UK landline: 01xx xxx xxxx or +441xx xxx xxxx
        String ukPhoneRegex = "^(0[17]\\d{9}|\\+44[17]\\d{9})$";
        
        return matches(cleaned, ukPhoneRegex);
    }
    
    /**
     * Converts a string to title case (first letter of each word capitalised).
     * 
     * This is an alias for capitaliseWords() for clearer semantics.
     * 
     * @param str the string to convert
     * @return string in title case
     */
    public static String toTitleCase(String str) {
        return capitaliseWords(str);
    }
    
    /**
     * Checks if a string contains only letters (no numbers or special characters).
     * 
     * @param str the string to check
     * @return true if string contains only letters, false otherwise
     */
    public static boolean isAlphabetic(String str) {
        if (isNullOrEmpty(str)) {
            return false;
        }
        return str.matches("[a-zA-Z]+");
    }
    
    /**
     * Checks if a string contains only digits.
     * 
     * @param str the string to check
     * @return true if string contains only digits, false otherwise
     */
    public static boolean isNumeric(String str) {
        if (isNullOrEmpty(str)) {
            return false;
        }
        return str.matches("\\d+");
    }
    
    /**
     * Checks if a string contains only alphanumeric characters.
     * 
     * @param str the string to check
     * @return true if string contains only letters and numbers, false otherwise
     */
    public static boolean isAlphanumeric(String str) {
        if (isNullOrEmpty(str)) {
            return false;
        }
        return str.matches("[a-zA-Z0-9]+");
    }
}
