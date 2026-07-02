/* Name  : Phuong Dang
Course  : CEN3024C-31032
Date    : 07/01/2026
Input validation to prevent crash
 */

import java.time.LocalDate;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

public class InputValidator {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private final Scanner scanner;

    public InputValidator(Scanner scanner) {
        this.scanner = scanner;
    }

    // Non-empty string
    public String readNonEmptyString(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (!input.isEmpty()) return input;
            System.out.println("❌ Cannot be empty.");
        }
    }

    // Valid date (MM/dd/yyyy)
    public static boolean isValidDate(String date) {
        try {
            LocalDate.parse(date, DATE_FORMAT);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    public String readValidDate(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (isValidDate(input)) return input;
            System.out.println("❌ Use MM/dd/yyyy (e.g., 06/12/2026)");
        }
    }

    // Non-negative integer
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

    // Yes/No
    public boolean readYesNo(String prompt) {
        while (true) {
            System.out.print(prompt + " (yes/no): ");
            String input = scanner.nextLine().trim().toLowerCase();
            if (input.equals("yes") || input.equals("y")) return true;
            if (input.equals("no") || input.equals("n")) return false;
            System.out.println("❌ Enter yes or no.");
        }
    }

    // Valid year (2013 - current)
    public int readValidYear(String prompt) {
        int currentYear = Year.now().getValue();
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                int year = Integer.parseInt(input);
                if (year >= 2013 && year <= currentYear) return year;
                System.out.println("❌ Year must be 2013-" + currentYear);
            } catch (NumberFormatException e) {
                System.out.println("❌ Enter a year.");
            }
        }
    }

    // Valid index for list
    public int readValidIndex(String prompt, int listSize) {
        if (listSize <= 0) {
            System.out.println("❌ List is empty.");
            return -1;
        }
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                int index = Integer.parseInt(input) - 1;
                if (index >= 0 && index < listSize) return index;
                System.out.println("❌ Enter 1-" + listSize);
            } catch (NumberFormatException e) {
                System.out.println("❌ Enter a number.");
            }
        }
    }

    // Read line (for import path)
    public String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    public String readLine() {
        return scanner.nextLine().trim();
    }
}