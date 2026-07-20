/* Name  : Phuong Dang
Course  : CEN3024C-31032
Date    : 07/01/2026
Input validation to prevent crash
Add performed year must be 2026
Relase date for song must between 2013-2026
 */

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

/**
 * Handles user input parsing and validation to prevent runtime errors
 */
public class InputValidator {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private final Scanner scanner;

    /**
     * Constructs an InputValidator attached to a Scanner
     *
     * @param scanner active Scanner object for console input
     */
    public InputValidator(Scanner scanner) {
        this.scanner = scanner;
    }

    /**
     * Prompts for a non-empty string.
     *
     * @param prompt message displayed to the user
     * @return validated non-empty string
     */
    public String readNonEmptyString(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (!input.isEmpty()) return input;
            System.out.println("❌ Cannot be empty.");
        }
    }

    /**
     * Checks if a string date matches MM/dd/yyyy format
     *
     * @param date string to validate
     * @return true if valid date format, false otherwise
     */
    public static boolean isValidDate(String date) {
        try {
            LocalDate.parse(date, DATE_FORMAT);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    /**
     * Prompts for a valid performance date in the year 2026
     *
     * @param prompt message displayed to user
     * @return validated date string (MM/dd/yyyy)
     */
    public String readValidDate(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            if (!isValidDate(input)) {
                System.out.println("❌ Use MM/dd/yyyy (e.g., 06/13/2013)");
                continue;
            }

            try {
                LocalDate localDate = LocalDate.parse(input, DATE_FORMAT);
                if (localDate.getYear() == 2026) {
                    return input;
                }
                System.out.println("❌ Performance date must be in 2026 (Arirang Tour Year).");
            } catch (DateTimeParseException e) {
                System.out.println("❌ Use MM/dd/yyyy (e.g., 06/13/2013)");
            }
        }
    }

    /**
     * Reads and validates a non-negative integer from the user
     *
     * @param prompt message to display to the user
     * @return the non-negative integer entered by the user
     */
    public int readNonNegativeInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                int value = Integer.parseInt(input);
                if (value >= 0) return value;
                System.out.println("❌ Can't be negative.");
            } catch (NumberFormatException e) {
                System.out.println("❌ Enter a number.");
            }
        }
    }

    /**
     * Prompts for yes/no input
     *
     * @param prompt message displayed to user
     * @return true if yes, false if no
     */
    public boolean readYesNo(String prompt) {
        while (true) {
            System.out.print(prompt + " (yes/no): ");
            String input = scanner.nextLine().trim().toLowerCase();
            if (input.equals("yes") || input.equals("y")) return true;
            if (input.equals("no") || input.equals("n")) return false;
            System.out.println("❌ Enter yes or no.");
        }
    }

    /**
     * Prompts for a song release year between 2013 and 2026
     *
     * @param prompt message displayed to user
     * @return validated year between 2013 and 2026
     */
    public int readValidYear(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                int year = Integer.parseInt(scanner.nextLine().trim());
                if (year >= 2013 && year <= 2026) return year;
                System.out.println("❌ Enter 2013-2026 (BTS debut year to current year).");
            } catch (NumberFormatException e) {
                System.out.println("❌ Enter a number.");
            }
        }
    }

    /**
     * Prompts for a 1-based list selection index
     *
     * @param prompt message displayed to user
     * @param listSize current size of target list
     * @return 0-based index, or -1 if canceled
     */
    public int readValidIndex(String prompt, int listSize) {
        if (listSize <= 0) {
            System.out.println("❌ The list is currently empty.");
            return -1;
        }
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                System.out.println("❌ Please enter a number.");
                continue;
            }

            try {
                int index = Integer.parseInt(input) - 1;
                if (index >= 0 && index < listSize) {
                    return index;
                }
                System.out.println("❌ Please enter a number between 1 and " + listSize + ".");
            } catch (NumberFormatException e) {
                System.out.println("❌ Please enter a valid number.");
            }
        }
    }

    /**
     * Reads a line of input from the user with a prompt
     *
     * @param prompt message to display to the user
     * @return the trimmed input string
     */
    public String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    public String readLine() {
        return scanner.nextLine().trim();
    }
}