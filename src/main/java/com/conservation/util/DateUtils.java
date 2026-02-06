package com.conservation.util;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Utility class for date formatting and manipulation.
 * 
 * Handles conversion between:
 * - UI format: DD-MM-YYYY (human-readable, European format)
 * - Storage format: YYYY-MM-DD (ISO 8601, sortable, database-standard)
 * 
 * Also provides date validation and age calculation utilities.
 */
public class DateUtils {
    
    // UI format for user-facing displays
    private static final DateTimeFormatter UI_FORMAT = 
        DateTimeFormatter.ofPattern("dd-MM-yyyy");
    
    // Storage format for XML/database persistence
    private static final DateTimeFormatter STORAGE_FORMAT = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    /**
     * Private constructor to prevent instantiation.
     * This is a utility class with only static methods.
     */
    private DateUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
    
    /**
     * Parses a date string from UI format (DD-MM-YYYY) to LocalDate.
     * 
     * Example: "15-05-2020" → LocalDate(2020-05-15)
     * 
     * @param dateString date string in DD-MM-YYYY format
     * @return LocalDate object
     * @throws IllegalArgumentException if date string is null, empty, or invalid
     */
    public static LocalDate parseFromUI(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            throw new IllegalArgumentException("Date string cannot be null or empty");
        }
        
        try {
            return LocalDate.parse(dateString.trim(), UI_FORMAT);
        } catch (DateTimeParseException parseException) {
            throw new IllegalArgumentException(
                "Invalid date format. Expected DD-MM-YYYY, got: " + dateString,
                parseException
            );
        }
    }
    
    /**
     * Parses a date string from storage format (YYYY-MM-DD) to LocalDate.
     * 
     * Example: "2020-05-15" → LocalDate(2020-05-15)
     * 
     * @param dateString date string in YYYY-MM-DD format
     * @return LocalDate object
     * @throws IllegalArgumentException if date string is null, empty, or invalid
     */
    public static LocalDate parseFromStorage(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            throw new IllegalArgumentException("Date string cannot be null or empty");
        }
        
        try {
            return LocalDate.parse(dateString.trim(), STORAGE_FORMAT);
        } catch (DateTimeParseException parseException) {
            throw new IllegalArgumentException(
                "Invalid date format. Expected YYYY-MM-DD, got: " + dateString,
                parseException
            );
        }
    }
    
    /**
     * Formats a LocalDate to UI format (DD-MM-YYYY).
     * 
     * Example: LocalDate(2020-05-15) → "15-05-2020"
     * 
     * @param date the date to format
     * @return formatted date string in DD-MM-YYYY format
     * @throws IllegalArgumentException if date is null
     */
    public static String formatForUI(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        return date.format(UI_FORMAT);
    }
    
    /**
     * Formats a LocalDate to storage format (YYYY-MM-DD).
     * 
     * Example: LocalDate(2020-05-15) → "2020-05-15"
     * 
     * @param date the date to format
     * @return formatted date string in YYYY-MM-DD format
     * @throws IllegalArgumentException if date is null
     */
    public static String formatForStorage(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        return date.format(STORAGE_FORMAT);
    }
    
    /**
     * Calculates age in years from a birth date to today.
     * 
     * Example: birthDate = 2020-05-15, today = 2025-02-04 → age = 4 years
     * 
     * @param birthDate the date of birth
     * @return age in complete years
     * @throws IllegalArgumentException if birthDate is null or in the future
     */
    public static int calculateAge(LocalDate birthDate) {
        if (birthDate == null) {
            throw new IllegalArgumentException("Birth date cannot be null");
        }
        if (birthDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Birth date cannot be in the future");
        }
        
        return Period.between(birthDate, LocalDate.now()).getYears();
    }
    
    /**
     * Calculates age in years from a birth date to a specific end date.
     * 
     * Useful for calculating age at a specific point in time.
     * 
     * @param birthDate the date of birth
     * @param endDate the date to calculate age at
     * @return age in complete years at the end date
     * @throws IllegalArgumentException if dates are null or birthDate is after endDate
     */
    public static int calculateAge(LocalDate birthDate, LocalDate endDate) {
        if (birthDate == null) {
            throw new IllegalArgumentException("Birth date cannot be null");
        }
        if (endDate == null) {
            throw new IllegalArgumentException("End date cannot be null");
        }
        if (birthDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Birth date cannot be after end date");
        }
        
        return Period.between(birthDate, endDate).getYears();
    }
    
    /**
     * Validates that a date is not in the future.
     * 
     * Useful for date of birth, date of acquisition, etc.
     * 
     * @param date the date to validate
     * @return true if date is today or in the past, false if in the future
     */
    public static boolean isNotFuture(LocalDate date) {
        if (date == null) {
            return false;
        }
        return !date.isAfter(LocalDate.now());
    }
    
    /**
     * Validates that dateAfter is after dateBefore.
     * 
     * Useful for validating date ranges (e.g., acquisition date after birth date).
     * 
     * @param dateBefore the earlier date
     * @param dateAfter the later date
     * @return true if dateAfter is after dateBefore, false otherwise
     */
    public static boolean isAfter(LocalDate dateBefore, LocalDate dateAfter) {
        if (dateBefore == null || dateAfter == null) {
            return false;
        }
        return dateAfter.isAfter(dateBefore);
    }
    
    /**
     * Validates a date string in UI format (DD-MM-YYYY).
     * 
     * @param dateString the date string to validate
     * @return true if valid, false if invalid
     */
    public static boolean isValidUIFormat(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return false;
        }
        
        try {
            LocalDate.parse(dateString.trim(), UI_FORMAT);
            return true;
        } catch (DateTimeParseException parseException) {
            return false;
        }
    }
    
    /**
     * Validates a date string in storage format (YYYY-MM-DD).
     * 
     * @param dateString the date string to validate
     * @return true if valid, false if invalid
     */
    public static boolean isValidStorageFormat(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return false;
        }
        
        try {
            LocalDate.parse(dateString.trim(), STORAGE_FORMAT);
            return true;
        } catch (DateTimeParseException parseException) {
            return false;
        }
    }
    
    /**
     * Gets today's date formatted for UI display.
     * 
     * @return today's date in DD-MM-YYYY format
     */
    public static String getTodayForUI() {
        return formatForUI(LocalDate.now());
    }
    
    /**
     * Gets today's date formatted for storage.
     * 
     * @return today's date in YYYY-MM-DD format
     */
    public static String getTodayForStorage() {
        return formatForStorage(LocalDate.now());
    }
}
