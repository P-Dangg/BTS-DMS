/* Name  : Phuong Dang
Course  : CEN3024C-31032
Date    : 07/01/2026
BTS Surprise Song Data Management System
 */

import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;

public class Main {

    private final SongRepository repository = new SongRepository();
    private final Scanner scanner = new Scanner(System.in);
    private final InputValidator input = new InputValidator(scanner);

    public static void main(String[] args) {
        new Main().run();
    }

    private void run() {
        repository.loadFromFile();
        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();

            // Check if input is empty
            if (choice.isEmpty()) {
                System.out.println("❌ Please enter a choice.");
                continue;
            }

            switch (choice) {
                case "1": addSong(); break;
                case "2": viewSongs(); break;
                case "3": editSong(); break;
                case "4": deleteSong(); break;
                case "5": showBorahaeMeter(); break;
                case "0":
                    running = false;
                    System.out.println("\n💜 Saranghae! Goodbye! 💜");
                    break;
                default:
                    System.out.println("❌ Invalid choice. Please enter 0-5.");
                    break;
            }
        }
    }

    private void printMenu() {
        System.out.println("\n===== BTS Surprise Song Tracker =====");
        System.out.println("1. Add Song");
        System.out.println("2. View Songs");
        System.out.println("3. Edit Song");
        System.out.println("4. Delete Song");
        System.out.println("5. Borahae Meter");
        System.out.println("0. Exit");
        System.out.print("Enter choice: ");
    }

    // ADD
    private void addSong() {
        System.out.println("\n1. Enter manually");
        System.out.println("2. Import from file");
        System.out.print("Choose: ");
        String choice = input.readLine();

        if (choice.equals("2")) {
            importFromFile();
        } else if (choice.equals("1") || choice.isEmpty()) {
            addManually();
        } else {
            System.out.println("❌ Invalid choice. Please enter 1 or 2.");
        }
    }

    private void addManually() {
        String title = input.readNonEmptyString("Song Title: ");

        // Check if this song already exists
        Song existing = findSongByTitle(title);

        if (existing != null) {
            System.out.println("'" + title + "' already exists in the database!");
            System.out.println("   Current total plays: " + existing.getTimesPlayed());
            System.out.print("   Add as another stop? (y/n): ");
            String confirm = input.readLine();

            if (confirm.equalsIgnoreCase("y") || confirm.equalsIgnoreCase("yes")) {
                String date = input.readValidDate("New Date (MM/dd/yyyy): ");
                String city = input.readNonEmptyString("New City: ");

                int newTotal = existing.getTimesPlayed() + 1;

                Song newSong = new Song(
                        title,
                        date,
                        city,
                        newTotal,
                        existing.isUnitSong(),
                        existing.getReleaseYear()
                );
                repository.addSong(newSong);

                existing.setTimesPlayed(newTotal);
                repository.persistAfterUpdate();

                System.out.println("\n✅ New stop added for '" + title + "'!");
                System.out.println("   New total plays: " + newTotal);
                System.out.println("   Both entries now show: " + newTotal + " plays");
                return;
            } else {
                System.out.println("❌ Cancelled.");
                return;
            }
        }

        // Normal add (new song)
        String date = input.readValidDate("Performed Date (MM/dd/yyyy): ");
        String city = input.readNonEmptyString("City: ");
        int plays = input.readNonNegativeInt("Times Played: ");
        boolean isUnit = input.readYesNo("Unit Song?");
        int year = input.readValidYear("Release Year: ");

        Song song = new Song(title, date, city, plays, isUnit, year);
        if (repository.addSong(song)) {
            System.out.println("✅ Song added!");
        } else {
            System.out.println("❌ Duplicate! (same title + date + city)");
        }
    }

    private Song findSongByTitle(String title) {
        for (Song s : repository.getAll()) {
            if (s.getSongTitle().equalsIgnoreCase(title)) {
                return s;
            }
        }
        return null;
    }

    private void importFromFile() {
        System.out.print("File path: ");
        String path = input.readLine();

        // Check if path is empty
        if (path.isEmpty()) {
            System.out.println("❌ File path cannot be empty.");
            return;
        }

        SongRepository.ImportResult result = repository.importFromFile(path);
        if (result.errorMessage != null) {
            System.out.println("❌ " + result.errorMessage);
        } else {
            System.out.println("✅ Added: " + result.added + " | Skipped: " + result.skipped);
        }
    }

    // VIEW
    private void viewSongs() {
        if (repository.size() == 0) {
            System.out.println("\nNo songs yet.");
            return;
        }
        System.out.println("\n-- All Songs --");
        for (int i = 0; i < repository.size(); i++) {
            System.out.println((i + 1) + ". " + repository.get(i));
        }
        System.out.println("\nTotal: " + repository.size() + " songs");
    }

    // EDIT
    private void editSong() {
        if (repository.size() == 0) {
            System.out.println("No songs to edit.");
            return;
        }

        viewSongs();
        int index = input.readValidIndex("Enter song number to edit: ", repository.size());
        if (index == -1) {
            System.out.println("❌ Edit cancelled.");
            return;
        }

        Song song = repository.get(index);
        System.out.println("\n--- Editing: " + song.getSongTitle() + " ---");
        System.out.println("(Press ENTER to keep current value)");

        // Title
        String title = input.readLine("Title [" + song.getSongTitle() + "]: ");
        if (!title.isEmpty()) {
            // Check if new title would create duplicate
            Song existing = findSongByTitle(title);
            if (existing != null && existing != song) {
                System.out.println("❌ '" + title + "' already exists in the database.");
            } else {
                song.setSongTitle(title);
            }
        }

        // Date - must be in 2026
        while (true) {
            String date = input.readLine("Date [" + song.getPerformedDate() + "]: ");
            if (date.isEmpty()) break; // Keep current
            if (InputValidator.isValidDate(date)) {
                try {
                    java.time.LocalDate localDate = java.time.LocalDate.parse(date, java.time.format.DateTimeFormatter.ofPattern("MM/dd/yyyy"));
                    if (localDate.getYear() == 2026) {
                        song.setPerformedDate(date);
                        break;
                    } else {
                        System.out.println("❌ Performance date must be in 2026 (Arirang Tour Year).");
                    }
                } catch (java.time.format.DateTimeParseException e) {
                    System.out.println("❌ Invalid date. Use MM/dd/yyyy");
                }
            } else {
                System.out.println("❌ Invalid date. Use MM/dd/yyyy");
            }
        }

        // City
        String city = input.readLine("City [" + song.getCity() + "]: ");
        if (!city.isEmpty()) song.setCity(city);

        // Times Played - loops until valid
        while (true) {
            String plays = input.readLine("Times Played [" + song.getTimesPlayed() + "]: ");
            if (plays.isEmpty()) break; // Keep current
            try {
                int p = Integer.parseInt(plays);
                if (p >= 0) {
                    song.setTimesPlayed(p);
                    break;
                }
                System.out.println("❌ Cannot be negative.");
            } catch (NumberFormatException e) {
                System.out.println("❌ Enter a number.");
            }
        }

        // Unit Song - loops until valid
        while (true) {
            String unit = input.readLine("Unit Song? (yes/no) [" + (song.isUnitSong() ? "yes" : "no") + "]: ");
            if (unit.isEmpty()) break; // Keep current
            if (unit.equalsIgnoreCase("yes") || unit.equalsIgnoreCase("y")) {
                song.setUnitSong(true);
                break;
            } else if (unit.equalsIgnoreCase("no") || unit.equalsIgnoreCase("n")) {
                song.setUnitSong(false);
                break;
            }
            System.out.println("❌ Enter 'yes' or 'no'.");
        }

        // Release Year - must be 2023-2026
        while (true) {
            String year = input.readLine("Release Year [" + song.getReleaseYear() + "]: ");
            if (year.isEmpty()) break;
            try {
                int y = Integer.parseInt(year);
                if (y >= 2013 && y <= 2026) {
                    song.setReleaseYear(y);
                    break;
                }
                System.out.println("❌ Enter 2013-2026 (BTS debut year to current year). ");
            } catch (NumberFormatException e) {
                System.out.println("❌ Enter a year.");
            }
        }

        repository.persistAfterUpdate();
        System.out.println("✅ Song updated!");
    }

    // DELETE
    private void deleteSong() {
        if (repository.size() == 0) {
            System.out.println("No songs to delete.");
            return;
        }

        viewSongs();
        int index = input.readValidIndex("Enter song number to delete: ", repository.size());
        if (index == -1) {
            System.out.println("❌ Delete cancelled.");
            return;
        }

        Song song = repository.get(index);
        String title = song.getSongTitle();

        // Loop until user gives valid input
        while (true) {
            System.out.print("Delete '" + title + "'? (y/n): ");
            String confirm = input.readLine();

            if (confirm.isEmpty()) {
                System.out.println("❌ Please enter yes or no.");
                continue;
            }

            if (confirm.equalsIgnoreCase("y") || confirm.equalsIgnoreCase("yes")) {
                // Find all entries with the same title (before deleting)
                List<Song> sameSongs = new ArrayList<>();
                for (Song s : repository.getAll()) {
                    if (s.getSongTitle().equalsIgnoreCase(title)) {
                        sameSongs.add(s);
                    }
                }

                // Delete the selected song
                repository.delete(index);

                // Update play counts if there are other entries with the same title
                if (sameSongs.size() > 1) {
                    int remainingStops = sameSongs.size() - 1;
                    for (Song s : repository.getAll()) {
                        if (s.getSongTitle().equalsIgnoreCase(title)) {
                            s.setTimesPlayed(remainingStops);
                        }
                    }
                    repository.persistAfterUpdate();
                    System.out.println("✅ Deleted! Remaining entries for '" + title + "' now show: " + remainingStops + " plays");
                } else {
                    System.out.println("✅ Deleted!");
                }
                break; // Exit the loop after successful deletion

            } else if (confirm.equalsIgnoreCase("n") || confirm.equalsIgnoreCase("no")) {
                System.out.println("❌ Deletion cancelled.");
                break; // Exit the loop

            } else {
                System.out.println("❌ Please enter yes or no.");
                // Loop continues
            }
        }
    }

    // BORAHAE
    private void showBorahaeMeter() {
        if (repository.size() == 0) {
            System.out.println("No songs yet.");
            return;
        }

        System.out.println("\n--- Borahae Meter ---");
        for (int i = 0; i < repository.size(); i++) {
            Song s = repository.get(i);
            System.out.println((i + 1) + ". " + s.getSongTitle() + " (" + s.getCity() + ")");
        }

        int index = input.readValidIndex("Select song: ", repository.size());
        if (index == -1) {
            System.out.println("❌ Selection cancelled.");
            return;
        }

        Song song = repository.get(index);
        int score = (int) BorahaeCalculator.calculate(song);
        int heartsCount = Math.min(Math.max(score, 0), 25);

        // Build hearts with 5 per row
        StringBuilder heartsDisplay = new StringBuilder();
        int heartsPerRow = 5;
        for (int i = 0; i < heartsCount; i++) {
            heartsDisplay.append("💜");
            if ((i + 1) % heartsPerRow == 0 && i < heartsCount - 1) {
                heartsDisplay.append("\n");
            }
        }


        System.out.println("\n+----------------------------------------------+");
        System.out.println("|            💜 BORAHAE METER 💜              |");
        System.out.println("+----------------------------------------------+");
        System.out.printf("   Song         : %-30s \n", song.getSongTitle());
        System.out.printf("   City         : %-30s \n", song.getCity());
        System.out.printf("   Release Year : %-30d \n", song.getReleaseYear());
        System.out.printf("   Times Played : %-30d \n", song.getTimesPlayed());
        System.out.printf("   Borahae Score: %-30d \n", score);
        System.out.println("+----------------------------------------------+");

        // Display hearts centered
        String heartStr = heartsDisplay.toString();
        if (heartStr.isEmpty()) {
            System.out.println("      No hearts yet");
        } else {
            String[] rows = heartStr.split("\n");
            for (String row : rows) {
                int padding = (44 - row.length()) / 2;
                System.out.println(" ".repeat(Math.max(0, padding)) + row);
            }
        }
        System.out.println("+----------------------------------------------+");
    }
}