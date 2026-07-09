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

    // Valid date - 2026 (Arirang tour year)
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

    // Valid year - must be between 2013 and 2026 (BTS debut to current)
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

    // Valid index for list
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

    // Read line
    public String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    public String readLine() {
        return scanner.nextLine().trim();
    }
}