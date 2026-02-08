package com.conservation.ui;

import com.conservation.model.Animal;
import com.conservation.model.Animal.Category;
import com.conservation.model.Keeper;
import com.conservation.model.Cage;
import com.conservation.util.DateUtils;

import java.util.Collection;
import java.util.List;

/**
 * Utility class for formatting console output in a consistent, readable manner.
 * Provides methods for displaying entities, tables, messages, and UI elements.
 * 
 * <p>All output follows a consistent visual style with:
 * <ul>
 *   <li>Header bars using '=' characters</li>
 *   <li>Separator lines using '-' characters</li>
 *   <li>Consistent column widths for tabular data</li>
 *   <li>Success (✓), Error (✗), and Warning (⚠) symbols</li>
 * </ul>
 * 
 * @author Clyde Conservation Development Team
 * @version 1.0
 */
public final class DisplayFormatter {

    // ============================================================
    // Constants for Formatting
    // ============================================================
    
    /** Standard width for console output lines */
    private static final int LINE_WIDTH = 60;
    
    /** Character used for header bars */
    private static final char HEADER_CHAR = '=';
    
    /** Character used for separator lines */
    private static final char SEPARATOR_CHAR = '-';
    
    /** Symbol for success messages */
    private static final String SUCCESS_SYMBOL = "✓";
    
    /** Symbol for error messages */
    private static final String ERROR_SYMBOL = "✗";
    
    /** Symbol for warning messages */
    private static final String WARNING_SYMBOL = "⚠";
    
    // Column widths for tables
    private static final int COL_ID = 6;
    private static final int COL_NAME = 15;
    private static final int COL_TYPE = 12;
    private static final int COL_CATEGORY = 10;
    private static final int COL_STATUS = 12;

    // ============================================================
    // Private Constructor (Utility Class)
    // ============================================================
    
    private DisplayFormatter() {
        // Prevent instantiation of utility class
    }

    // ============================================================
    // Header and Separator Methods
    // ============================================================
    
    /**
     * Prints a header bar line of '=' characters.
     */
    public static void printHeaderBar() {
        System.out.println(repeatChar(HEADER_CHAR, LINE_WIDTH));
    }
    
    /**
     * Prints a separator line of '-' characters.
     */
    public static void printSeparator() {
        System.out.println(repeatChar(SEPARATOR_CHAR, LINE_WIDTH));
    }
    
    /**
     * Prints a centred title with header bars above and below.
     * 
     * @param title the title text to display
     */
    public static void printTitle(String title) {
        printHeaderBar();
        printCentred(title);
        printHeaderBar();
    }
    
    /**
     * Prints text centred within the standard line width.
     * 
     * @param text the text to centre
     */
    public static void printCentred(String text) {
        int padding = (LINE_WIDTH - text.length()) / 2;
        System.out.println(repeatChar(' ', padding) + text);
    }
    
    /**
     * Prints an empty line.
     */
    public static void printBlankLine() {
        System.out.println();
    }

    // ============================================================
    // Message Display Methods
    // ============================================================
    
    /**
     * Prints a success message with the success symbol.
     * 
     * @param message the success message to display
     */
    public static void printSuccess(String message) {
        System.out.println();
        System.out.println(SUCCESS_SYMBOL + " Success!");
        System.out.println();
        System.out.println(message);
    }
    
    /**
     * Prints an error message with the error symbol.
     * 
     * @param message the error message to display
     */
    public static void printError(String message) {
        System.out.println();
        System.out.println(ERROR_SYMBOL + " Error!");
        System.out.println();
        System.out.println(message);
    }
    
    /**
     * Prints a warning message with the warning symbol.
     * 
     * @param message the warning message to display
     */
    public static void printWarning(String message) {
        System.out.println();
        System.out.println(WARNING_SYMBOL + " Warning!");
        System.out.println();
        System.out.println(message);
    }
    
    /**
     * Prints an informational message.
     * 
     * @param message the message to display
     */
    public static void printInfo(String message) {
        System.out.println(message);
    }
    
    /**
     * Prints a labelled value pair.
     * 
     * @param label the label (field name)
     * @param value the value to display
     */
    public static void printLabelledValue(String label, String value) {
        System.out.printf("  %-20s %s%n", label + ":", value);
    }
    
    /**
     * Prints a labelled value pair with integer value.
     * 
     * @param label the label (field name)
     * @param value the integer value to display
     */
    public static void printLabelledValue(String label, int value) {
        printLabelledValue(label, String.valueOf(value));
    }

    // ============================================================
    // Animal Display Methods
    // ============================================================
    
    /**
     * Displays detailed information for a single animal.
     * 
     * @param animal the animal to display
     */
    public static void displayAnimal(Animal animal) {
        if (animal == null) {
            printError("Animal not found.");
            return;
        }
        
        printBlankLine();
        printLabelledValue("Animal ID", animal.getAnimalId());
        printLabelledValue("Name", animal.getName());
        printLabelledValue("Type", animal.getType());
        printLabelledValue("Category", animal.getCategory().toString());
        printLabelledValue("Date of Birth", DateUtils.formatForUI(animal.getDateOfBirth()));
        printLabelledValue("Date Acquired", DateUtils.formatForUI(animal.getDateOfAcquisition()));
        printLabelledValue("Sex", animal.getSex().toString());
    }
    
    /**
     * Displays a table of animals with column headers.
     * 
     * @param animals the collection of animals to display
     */
    public static void displayAnimalTable(Collection<Animal> animals) {
        if (animals == null || animals.isEmpty()) {
            printInfo("No animals found.");
            return;
        }
        
        printBlankLine();
        printAnimalTableHeader();
        printSeparator();
        
        for (Animal animal : animals) {
            printAnimalTableRow(animal);
        }
        
        printSeparator();
        printInfo("Total: " + animals.size() + " animal(s)");
    }
    
    /**
     * Prints the header row for animal tables.
     */
    private static void printAnimalTableHeader() {
        System.out.printf("%-" + COL_ID + "s %-" + COL_NAME + "s %-" + COL_TYPE + "s %-" + COL_CATEGORY + "s%n",
            "ID", "Name", "Type", "Category");
    }
    
    /**
     * Prints a single row in the animal table.
     * 
     * @param animal the animal to display
     */
    private static void printAnimalTableRow(Animal animal) {
        System.out.printf("%-" + COL_ID + "d %-" + COL_NAME + "s %-" + COL_TYPE + "s %-" + COL_CATEGORY + "s%n",
            animal.getAnimalId(),
            truncate(animal.getName(), COL_NAME),
            truncate(animal.getType(), COL_TYPE),
            animal.getCategory().toString());
    }
    
    /**
     * Displays animals grouped by category (PREDATOR/PREY).
     * 
     * @param animals the collection of animals to display
     */
    public static void displayAnimalsByCategory(Collection<Animal> animals) {
        if (animals == null || animals.isEmpty()) {
            printInfo("No animals found.");
            return;
        }
        
        // Display predators
        printBlankLine();
        printInfo("=== PREDATORS ===");
        long predatorCount = animals.stream()
            .filter(animal -> animal.getCategory() == Category.PREDATOR)
            .peek(DisplayFormatter::printAnimalTableRow)
            .count();
        
        if (predatorCount == 0) {
            printInfo("  No predators found.");
        }
        
        // Display prey
        printBlankLine();
        printInfo("=== PREY ===");
        long preyCount = animals.stream()
            .filter(animal -> animal.getCategory() == Category.PREY)
            .peek(DisplayFormatter::printAnimalTableRow)
            .count();
        
        if (preyCount == 0) {
            printInfo("  No prey animals found.");
        }
    }

    // ============================================================
    // Keeper Display Methods
    // ============================================================
    
    /**
     * Displays detailed information for a single keeper.
     * 
     * @param keeper the keeper to display
     */
    public static void displayKeeper(Keeper keeper) {
        if (keeper == null) {
            printError("Keeper not found.");
            return;
        }
        
        printBlankLine();
        printLabelledValue("Keeper ID", keeper.getKeeperId());
        printLabelledValue("Name", keeper.getFirstName() + " " + keeper.getSurname());
        printLabelledValue("Position", keeper.getPosition().toString());
        printLabelledValue("Address", keeper.getAddress());
        printLabelledValue("Contact", keeper.getContactNumber());
        
        List<Integer> allocatedCages = keeper.getAllocatedCageIds();
        String cagesInfo = allocatedCages.isEmpty() 
            ? "None" 
            : allocatedCages.toString() + " (" + allocatedCages.size() + "/4)";
        printLabelledValue("Allocated Cages", cagesInfo);
    }
    
    /**
     * Displays a table of keepers with column headers.
     * 
     * @param keepers the collection of keepers to display
     */
    public static void displayKeeperTable(Collection<Keeper> keepers) {
        if (keepers == null || keepers.isEmpty()) {
            printInfo("No keepers found.");
            return;
        }
        
        printBlankLine();
        printKeeperTableHeader();
        printSeparator();
        
        for (Keeper keeper : keepers) {
            printKeeperTableRow(keeper);
        }
        
        printSeparator();
        printInfo("Total: " + keepers.size() + " keeper(s)");
    }
    
    /**
     * Prints the header row for keeper tables.
     */
    private static void printKeeperTableHeader() {
        System.out.printf("%-" + COL_ID + "s %-" + COL_NAME + "s %-" + COL_TYPE + "s %-" + COL_STATUS + "s%n",
            "ID", "Name", "Position", "Cages");
    }
    
    /**
     * Prints a single row in the keeper table.
     * 
     * @param keeper the keeper to display
     */
    private static void printKeeperTableRow(Keeper keeper) {
        String fullName = keeper.getFirstName() + " " + keeper.getSurname();
        String position = keeper.getPosition() == Keeper.Position.HEAD_KEEPER ? "Head" : "Assistant";
        String cagesInfo = keeper.getAllocatedCageIds().size() + "/4";
        
        System.out.printf("%-" + COL_ID + "d %-" + COL_NAME + "s %-" + COL_TYPE + "s %-" + COL_STATUS + "s%n",
            keeper.getKeeperId(),
            truncate(fullName, COL_NAME),
            position,
            cagesInfo);
    }

    // ============================================================
    // Cage Display Methods
    // ============================================================
    
    /**
     * Displays detailed information for a single cage.
     * 
     * @param cage the cage to display
     */
    public static void displayCage(Cage cage) {
        if (cage == null) {
            printError("Cage not found.");
            return;
        }
        
        printBlankLine();
        printLabelledValue("Cage ID", cage.getCageId());
        printLabelledValue("Cage Number", cage.getCageNumber());
        printLabelledValue("Description", cage.getDescription());
        printLabelledValue("Capacity", cage.getOccupancyInfo());
        printLabelledValue("Status", cage.getStatus());
        
        Integer assignedKeeper = cage.getAssignedKeeperId();
        printLabelledValue("Assigned Keeper", assignedKeeper != null ? String.valueOf(assignedKeeper) : "None");
        
        List<Integer> animalIds = cage.getCurrentAnimalIds();
        String animalsInfo = animalIds.isEmpty() ? "None" : animalIds.toString();
        printLabelledValue("Current Animals", animalsInfo);
    }
    
    /**
     * Displays a table of cages with column headers.
     * 
     * @param cages the collection of cages to display
     */
    public static void displayCageTable(Collection<Cage> cages) {
        if (cages == null || cages.isEmpty()) {
            printInfo("No cages found.");
            return;
        }
        
        printBlankLine();
        printCageTableHeader();
        printSeparator();
        
        for (Cage cage : cages) {
            printCageTableRow(cage);
        }
        
        printSeparator();
        printInfo("Total: " + cages.size() + " cage(s)");
    }
    
    /**
     * Prints the header row for cage tables.
     */
    private static void printCageTableHeader() {
        System.out.printf("%-" + COL_ID + "s %-" + COL_NAME + "s %-" + COL_TYPE + "s %-" + COL_STATUS + "s%n",
            "ID", "Number", "Capacity", "Status");
    }
    
    /**
     * Prints a single row in the cage table.
     * 
     * @param cage the cage to display
     */
    private static void printCageTableRow(Cage cage) {
        System.out.printf("%-" + COL_ID + "d %-" + COL_NAME + "s %-" + COL_TYPE + "s %-" + COL_STATUS + "s%n",
            cage.getCageId(),
            truncate(cage.getCageNumber(), COL_NAME),
            cage.getOccupancyInfo(),
            cage.getStatus());
    }
    
    /**
     * Displays cage occupancy summary information.
     * 
     * @param cages the collection of cages to summarise
     */
    public static void displayCageOccupancySummary(Collection<Cage> cages) {
        if (cages == null || cages.isEmpty()) {
            printInfo("No cages found.");
            return;
        }
        
        int totalCages = cages.size();
        long emptyCages = cages.stream().filter(Cage::isEmpty).count();
        long fullCages = cages.stream().filter(Cage::isFull).count();
        long availableCages = totalCages - fullCages;
        
        int totalCapacity = cages.stream().mapToInt(Cage::getAnimalCapacity).sum();
        int totalOccupancy = cages.stream().mapToInt(cage -> cage.getCurrentAnimalIds().size()).sum();
        
        printBlankLine();
        printInfo("=== CAGE OCCUPANCY SUMMARY ===");
        printBlankLine();
        printLabelledValue("Total Cages", totalCages);
        printLabelledValue("Empty Cages", (int) emptyCages);
        printLabelledValue("Full Cages", (int) fullCages);
        printLabelledValue("Available Cages", (int) availableCages);
        printBlankLine();
        printLabelledValue("Total Capacity", totalCapacity + " animals");
        printLabelledValue("Current Occupancy", totalOccupancy + " animals");
        printLabelledValue("Available Space", (totalCapacity - totalOccupancy) + " animals");
    }

    // ============================================================
    // Menu Display Methods
    // ============================================================
    
    /**
     * Displays a menu with numbered options.
     * 
     * @param title   the menu title
     * @param options the menu options (will be numbered starting from 1)
     */
    public static void displayMenu(String title, String[] options) {
        printTitle(title);
        printBlankLine();
        
        for (int optionIndex = 0; optionIndex < options.length; optionIndex++) {
            System.out.printf("%d. %s%n", optionIndex + 1, options[optionIndex]);
        }
        
        printBlankLine();
    }
    
    /**
     * Displays a numbered list of items for selection.
     * 
     * @param title the list title
     * @param items the items to display
     */
    public static void displayNumberedList(String title, List<String> items) {
        printInfo(title);
        printBlankLine();
        
        for (int itemIndex = 0; itemIndex < items.size(); itemIndex++) {
            System.out.printf("  %d. %s%n", itemIndex + 1, items.get(itemIndex));
        }
    }

    // ============================================================
    // Prompt Display Methods
    // ============================================================
    
    /**
     * Displays an input prompt without newline.
     * 
     * @param prompt the prompt text
     */
    public static void displayPrompt(String prompt) {
        System.out.print(prompt + ": ");
    }
    
    /**
     * Displays a menu selection prompt.
     * 
     * @param minOption the minimum valid option
     * @param maxOption the maximum valid option
     */
    public static void displayMenuPrompt(int minOption, int maxOption) {
        System.out.printf("Select option (%d-%d): ", minOption, maxOption);
    }
    
    /**
     * Displays a confirmation prompt (Y/N).
     * 
     * @param message the confirmation message
     */
    public static void displayConfirmationPrompt(String message) {
        System.out.print(message + " (Y/N): ");
    }
    
    /**
     * Displays "Press ENTER to continue..." prompt.
     */
    public static void displayContinuePrompt() {
        System.out.print("Press ENTER to continue...");
    }

    // ============================================================
    // Utility Methods
    // ============================================================
    
    /**
     * Creates a string by repeating a character.
     * 
     * @param character   the character to repeat
     * @param repeatCount the number of times to repeat
     * @return the repeated string
     */
    private static String repeatChar(char character, int repeatCount) {
        StringBuilder builder = new StringBuilder(repeatCount);
        for (int idx = 0; idx < repeatCount; idx++) {
            builder.append(character);
        }
        return builder.toString();
    }
    
    /**
     * Truncates a string to a maximum length, adding "..." if truncated.
     * 
     * @param text      the text to truncate
     * @param maxLength the maximum length
     * @return the truncated string
     */
    private static String truncate(String text, int maxLength) {
        if (text == null) {
            return "";
        }
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength - 3) + "...";
    }
    
    /**
     * Pads a string to a specified width with spaces on the right.
     * 
     * @param text  the text to pad
     * @param width the desired width
     * @return the padded string
     */
    public static String padRight(String text, int width) {
        if (text == null) {
            text = "";
        }
        return String.format("%-" + width + "s", text);
    }
    
    /**
     * Pads a string to a specified width with spaces on the left.
     * 
     * @param text  the text to pad
     * @param width the desired width
     * @return the padded string
     */
    public static String padLeft(String text, int width) {
        if (text == null) {
            text = "";
        }
        return String.format("%" + width + "s", text);
    }
}
