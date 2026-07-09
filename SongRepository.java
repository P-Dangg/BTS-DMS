/* Name  : Phuong Dang
Course  : CEN3024C-31032
Date    : 07/01/2026
Manages the in memory list of songs and keeps it synced with a text file
 */

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class SongRepository {

    private static final String DATA_FILE = "songs.txt";
    private final List<Song> songs = new ArrayList<>();

    // Load from file
    public void loadFromFile() {
        File file = new File(DATA_FILE);
        if (!file.exists()) {
            System.out.println("No data file. Starting empty.");
            return;
        }

        int loaded = 0, skipped = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split("\\|", -1);
                if (parts.length != 6) { skipped++; continue; }

                try {
                    String title = parts[0].trim();
                    String date = parts[1].trim();
                    String city = parts[2].trim();
                    int plays = Integer.parseInt(parts[3].trim());
                    boolean isUnit = parseYesNo(parts[4].trim());
                    int year = Integer.parseInt(parts[5].trim());

                    if (title.isEmpty() || city.isEmpty() || !InputValidator.isValidDate(date) || plays < 0) {
                        skipped++; continue;
                    }

                    // Performance date must be in 2026 (Arirang Tour)
                    if (!isValidPerformanceDate(date)) {
                        skipped++; continue;
                    }

                    // Release year must be 2013-2026 (BTS debut to current)
                    if (year < 2013 || year > 2026) {
                        skipped++; continue;
                    }

                    if (isDuplicate(title, date, city, -1)) {
                        skipped++; continue;
                    }

                    songs.add(new Song(title, date, city, plays, isUnit, year));
                    loaded++;
                } catch (NumberFormatException e) {
                    skipped++;
                }
            }
            System.out.println("✅ Loaded " + loaded + " songs" + (skipped > 0 ? " (" + skipped + " skipped)" : ""));
        } catch (IOException e) {
            System.out.println("❌ Could not read file.");
        }
    }

    // Save to file
    public void saveToFile() {
        try (FileWriter writer = new FileWriter(DATA_FILE)) {
            for (Song s : songs) {
                writer.write(s.getSongTitle() + "|" +
                        s.getPerformedDate() + "|" +
                        s.getCity() + "|" +
                        s.getTimesPlayed() + "|" +
                        booleanToYesNo(s.isUnitSong()) + "|" +
                        s.getReleaseYear());
                writer.write(System.lineSeparator());
            }
            System.out.println("💾 Saved to " + DATA_FILE);
        } catch (IOException e) {
            System.out.println("❌ Error saving.");
        }
    }

    // Yes/No helpers
    private boolean parseYesNo(String input) {
        return input.trim().toLowerCase().matches("yes|y|true");
    }

    private String booleanToYesNo(boolean value) {
        return value ? "yes" : "no";
    }

    // Check if performance date is in 2026
    private boolean isValidPerformanceDate(String date) {
        try {
            java.time.LocalDate localDate = java.time.LocalDate.parse(date, java.time.format.DateTimeFormatter.ofPattern("MM/dd/yyyy"));
            return localDate.getYear() == 2026;
        } catch (java.time.format.DateTimeParseException e) {
            return false;
        }
    }

    // CRUD
    public boolean addSong(Song song) {
        if (isDuplicate(song.getSongTitle(), song.getPerformedDate(), song.getCity(), -1)) {
            return false;
        }
        songs.add(song);
        saveToFile();
        return true;
    }

    public List<Song> getAll() { return songs; }
    public Song get(int index) {
        if (index >= 0 && index < songs.size()) {
            return songs.get(index);
        }
        return null;
    }
    public int size() { return songs.size(); }

    public Song delete(int index) {
        if (index >= 0 && index < songs.size()) {
            Song removed = songs.remove(index);
            saveToFile();
            return removed;
        }
        return null;
    }

    public void persistAfterUpdate() { saveToFile(); }

    // Duplicate check
    public boolean isDuplicate(String title, String date, String city, int excludeIndex) {
        for (int i = 0; i < songs.size(); i++) {
            if (i == excludeIndex) continue;
            Song s = songs.get(i);
            if (s.getSongTitle().equalsIgnoreCase(title) &&
                    s.getPerformedDate().equals(date) &&
                    s.getCity().equalsIgnoreCase(city)) {
                return true;
            }
        }
        return false;
    }

    // Import from file
    public ImportResult importFromFile(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return new ImportResult(0, 0, "File not found");
        }

        int added = 0, skipped = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split("\\|", -1);
                if (parts.length != 6) { skipped++; continue; }

                try {
                    String title = parts[0].trim();
                    String date = parts[1].trim();
                    String city = parts[2].trim();
                    int plays = Integer.parseInt(parts[3].trim());
                    boolean isUnit = parseYesNo(parts[4].trim());
                    int year = Integer.parseInt(parts[5].trim());

                    if (title.isEmpty() || city.isEmpty() || !InputValidator.isValidDate(date) || plays < 0) {
                        skipped++; continue;
                    }

                    // Performance date must be in 2026 (Arirang Tour)
                    if (!isValidPerformanceDate(date)) {
                        skipped++; continue;
                    }

                    // Release year must be 2013-2026 (BTS debut to current)
                    if (year < 2013 || year > 2026) {
                        skipped++; continue;
                    }

                    if (isDuplicate(title, date, city, -1)) {
                        skipped++; continue;
                    }

                    songs.add(new Song(title, date, city, plays, isUnit, year));
                    added++;
                } catch (NumberFormatException e) {
                    skipped++;
                }
            }
            if (added > 0) saveToFile();
        } catch (IOException e) {
            return new ImportResult(added, skipped, "Error reading");
        }
        return new ImportResult(added, skipped, null);
    }

    public static class ImportResult {
        public final int added, skipped;
        public final String errorMessage;
        public ImportResult(int added, int skipped, String errorMessage) {
            this.added = added;
            this.skipped = skipped;
            this.errorMessage = errorMessage;
        }
    }
}