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
            switch (choice) {
                case "1": addSong(); break;
                case "2": viewSongs(); break;
                case "3": editSong(); break;
                case "4": deleteSong(); break;
                case "5": showBorahaeMeter(); break;
                case "0": running = false; break;
                default: System.out.println("Invalid choice.");
            }
        }
        System.out.println("Saranghae! Goodbye. 💜");
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
        } else {
            addManually();
        }
    }

    private void addManually() {
        String title = input.readNonEmptyString("Song Title: ");

        // Check if this song already exists
        Song existing = findSongByTitle(title);

        if (existing != null) {
            System.out.println( title + "' already exists in the database!");
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
        String date = input.readValidDate("Date (MM/dd/yyyy): ");
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
            System.out.println("\n📭 No songs yet.");
            return;
        }
        System.out.println("\n-- All Songs --");
        for (int i = 0; i < repository.size(); i++) {
            System.out.println((i + 1) + ". " + repository.get(i));
        }
        System.out.println("\n📊 Total: " + repository.size() + " songs");
    }

    // EDIT
    private void editSong() {
        if (repository.size() == 0) {
            System.out.println("📭 No songs to edit.");
            return;
        }

        viewSongs();
        int index = input.readValidIndex("Enter song number: ", repository.size());
        if (index == -1) return;

        Song song = repository.get(index);
        System.out.println("\n--- Editing: " + song.getSongTitle() + " ---");
        System.out.println("(Press ENTER to keep current value)");

        // Title
        String title = input.readLine("Title [" + song.getSongTitle() + "]: ");
        if (!title.isEmpty()) song.setSongTitle(title);

        // Date - loops until valid
        while (true) {
            String date = input.readLine("Date [" + song.getPerformedDate() + "]: ");
            if (date.isEmpty()) break; // Keep current
            if (InputValidator.isValidDate(date)) {
                song.setPerformedDate(date);
                break;
            }
            System.out.println("❌ Invalid date. Use MM/dd/yyyy");
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

        // Release Year - loops until valid
        int currentYear = java.time.Year.now().getValue();
        while (true) {
            String year = input.readLine("Release Year [" + song.getReleaseYear() + "]: ");
            if (year.isEmpty()) break; // Keep current
            try {
                int y = Integer.parseInt(year);
                if (y >= 2013 && y <= currentYear) {
                    song.setReleaseYear(y);
                    break;
                }
                System.out.println("❌ Year must be 2013-" + currentYear);
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
            System.out.println("📭 No songs to delete.");
            return;
        }

        viewSongs();
        int index = input.readValidIndex("Enter song number: ", repository.size());
        if (index == -1) return;

        Song song = repository.get(index);
        String title = song.getSongTitle();

        System.out.print("Delete '" + title + "'? (y/n): ");
        String confirm = input.readLine();

        if (confirm.equalsIgnoreCase("y")) {
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
        } else {
            System.out.println("❌ Cancelled.");
        }
    }

    // BORAHAE
    private void showBorahaeMeter() {
        if (repository.size() == 0) {
            System.out.println("📭 No songs yet.");
            return;
        }

        System.out.println("\n--- Borahae Meter ---");
        for (int i = 0; i < repository.size(); i++) {
            Song s = repository.get(i);
            System.out.println((i + 1) + ". " + s.getSongTitle() + " (" + s.getCity() + ")");
        }

        int index = input.readValidIndex("Select song: ", repository.size());
        if (index == -1) return;

        Song song = repository.get(index);
        int score = (int) BorahaeCalculator.calculate(song);
        int heartsCount = Math.min(Math.max(score, 0), 25);

        StringBuilder hearts = new StringBuilder();
        for (int i = 0; i < heartsCount; i++) hearts.append("💜");

        System.out.println("\n+----------------------------------------------+");
        System.out.println("|            💜 BORAHAE METER 💜             |");
        System.out.println("+----------------------------------------------+");
        System.out.printf("| Song:          %-30s |\n", song.getSongTitle());
        System.out.printf("| City:          %-30s |\n", song.getCity());
        System.out.printf("| Release Year:  %-30d |\n", song.getReleaseYear());
        System.out.printf("| Times Played:  %-30d |\n", song.getTimesPlayed());
        System.out.println("|                                              |");
        System.out.printf("| Borahae Score: %-30d |\n", score);
        System.out.printf("| %-44s |\n", hearts.toString());
        System.out.println("+----------------------------------------------+");
    }
}