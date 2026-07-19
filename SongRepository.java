/* Name  : Phuong Dang
Course  : CEN3024C-31032
Date    : 07/01/2026
Manages the in memory list of songs and keeps it synced with a SQLite database
 */

import java.io.*;
import java.nio.file.Path;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SongRepository {

    private static final String LEGACY_DATA_FILE = "songs.txt";

    //Database path
    private static final String DB_PATH = resolveDatabasePath();
    private static final String DB_URL = "jdbc:sqlite:" + DB_PATH;
    //In memory cache of all songs
    private final List<Song> songs = new ArrayList<>();

    //Constructor loads JDBC driver and initializes the database.
    public SongRepository() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.out.println("❌ SQLite JDBC driver not found on classpath.");
        }
        initializeDatabase();
    }

    //Determines where the database file should be created.
    //Places songs.db
    private static String resolveDatabasePath() {
        try {
            Path codeSource = Path.of(
                    SongRepository.class.getProtectionDomain()
                            .getCodeSource()
                            .getLocation()
                            .toURI());
            File location = codeSource.toFile();
            File dir = location.isFile() ? location.getParentFile() : location;
            if (dir == null || !dir.exists()) {
                dir = new File(System.getProperty("user.dir"));
            }
            return new File(dir, "songs.db").getAbsolutePath();
        } catch (Exception e) {
            return "songs.db";
        }
    }

    public String getDatabasePath() {
        return DB_PATH;
    }

    //Creates the songs table if it doesn't exist
    private void initializeDatabase() {
        boolean isNewDatabase = !new File(DB_PATH).exists();

        try (Connection conn = connect()) {
            if (conn == null) return;
            try (Statement st = conn.createStatement()) {
                st.execute(
                        "CREATE TABLE IF NOT EXISTS songs (" +
                                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                "song_title TEXT NOT NULL, " +
                                "performed_date TEXT NOT NULL, " +
                                "city TEXT NOT NULL, " +
                                "times_played INTEGER NOT NULL DEFAULT 0, " +
                                "is_unit_song INTEGER NOT NULL DEFAULT 0, " +
                                "release_year INTEGER NOT NULL" +
                                ")");
            }
        } catch (SQLException e) {
            System.out.println("❌ Could not initialize database: " + e.getMessage());
            return;
        }
        //Migrate old data if this is a fresh database and legacy file exists
        if (isNewDatabase && new File(LEGACY_DATA_FILE).exists()) {
            migrateLegacyTextFile();
        }
    }

    //One-time import of the old songs.txt format into the new database.
    private void migrateLegacyTextFile() {
        File file = new File(LEGACY_DATA_FILE);
        int migrated = 0, skipped = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Song parsed = parseLegacyLine(line);
                if (parsed == null) {
                    if (!line.trim().isEmpty()) skipped++;
                    continue;
                }
                if (isDuplicate(parsed.getSongTitle(), parsed.getPerformedDate(), parsed.getCity(), -1)) {
                    skipped++;
                    continue;
                }
                if (insertRow(parsed)) {
                    songs.add(parsed);
                    migrated++;
                } else {
                    skipped++;
                }
            }
        } catch (IOException e) {
            System.out.println("❌ Could not read legacy songs.txt for migration.");
            return;
        }

        if (migrated > 0) {
            System.out.println("✅ Migrated " + migrated + " songs from songs.txt into songs.db"
                    + (skipped > 0 ? " (" + skipped + " skipped)" : ""));
        }
    }

    //Parses a legacy line in format: "title|date|city|plays|yes/no|year"
    //null if the line is blank or improperly formed.
    private Song parseLegacyLine(String line) {
        line = line.trim();
        if (line.isEmpty()) return null;

        String[] parts = line.split("\\|", -1);
        if (parts.length != 6) return null;

        try {
            String title = parts[0].trim();
            String date = parts[1].trim();
            String city = parts[2].trim();
            int plays = Integer.parseInt(parts[3].trim());
            boolean isUnit = parseYesNo(parts[4].trim());
            int year = Integer.parseInt(parts[5].trim());

            if (!isRowValid(title, date, city, plays, year)) return null;
            return new Song(title, date, city, plays, isUnit, year);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    //Opens a database connection
    //Returns null on error instead of throwing
    private Connection connect() {
        try {
            return DriverManager.getConnection(DB_URL);
        } catch (SQLException e) {
            System.out.println("❌ Could not connect to database: " + e.getMessage());
            return null;
        }
    }

    //Validates a song data before database operations
    //Prevent crashing
    private boolean isRowValid(String title, String date, String city, int plays, int year) {
        if (title == null || title.isBlank()) return false;
        if (city == null || city.isBlank()) return false;
        if (date == null || !InputValidator.isValidDate(date)) return false;
        if (!isValidPerformanceDate(date)) return false;
        if (plays < 0) return false;
        if (year < 2013 || year > 2026) return false;
        return true;
    }

    //Load from database
    public void loadFromFile() {
        songs.clear();
        Connection conn = connect();
        if (conn == null) {
            System.out.println("❌ Starting with an empty list; database unavailable.");
            return;
        }

        int loaded = 0, skipped = 0;
        String query = "SELECT id, song_title, performed_date, city, times_played, is_unit_song, release_year " +
                "FROM songs ORDER BY id ASC";

        try (conn; Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String title = rs.getString("song_title");
                String date = rs.getString("performed_date");
                String city = rs.getString("city");
                int plays = rs.getInt("times_played");
                boolean isUnit = rs.getInt("is_unit_song") != 0;
                int year = rs.getInt("release_year");

                //Skip invalid or corrupted rows
                if (!isRowValid(title, date, city, plays, year)) {
                    skipped++;
                    continue;
                }
                if (isDuplicate(title, date, city, -1)) {
                    skipped++;
                    continue;
                }

                Song song = new Song(title, date, city, plays, isUnit, year);
                song.setId(id);
                songs.add(song);
                loaded++;
            }
            System.out.println("✅ Loaded " + loaded + " songs from " + DB_PATH
                    + (skipped > 0 ? " (" + skipped + " skipped)" : ""));
        } catch (SQLException e) {
            System.out.println("❌ Could not read from database: " + e.getMessage());
        }
    }

    //Inserts a new song into the database and updates its ID
    //Returns false if insertion fails or data is invalid
    private boolean insertRow(Song song) {
        if (!isRowValid(song.getSongTitle(), song.getPerformedDate(), song.getCity(),
                song.getTimesPlayed(), song.getReleaseYear())) {
            return false;
        }
        String sql = "INSERT INTO songs " +
                "(song_title, performed_date, city, times_played, is_unit_song, release_year) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = connect()) {
            if (conn == null) return false;
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, song.getSongTitle());
                ps.setString(2, song.getPerformedDate());
                ps.setString(3, song.getCity());
                ps.setInt(4, song.getTimesPlayed());
                ps.setInt(5, song.isUnitSong() ? 1 : 0);
                ps.setInt(6, song.getReleaseYear());
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) song.setId(keys.getInt(1));
                }
            }
            return true;
        } catch (SQLException e) {
            System.out.println("❌ Error saving song: " + e.getMessage());
            return false;
        }
    }

    //Updates one existing row by ID
    //Returns false if update fails or data is invalid
    private boolean updateRow(Song song) {
        if (song.getId() < 0) return false;
        if (!isRowValid(song.getSongTitle(), song.getPerformedDate(), song.getCity(),
                song.getTimesPlayed(), song.getReleaseYear())) {
            return false;
        }
        String sql = "UPDATE songs SET song_title = ?, performed_date = ?, city = ?, " +
                "times_played = ?, is_unit_song = ?, release_year = ? WHERE id = ?";
        try (Connection conn = connect()) {
            if (conn == null) return false;
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, song.getSongTitle());
                ps.setString(2, song.getPerformedDate());
                ps.setString(3, song.getCity());
                ps.setInt(4, song.getTimesPlayed());
                ps.setInt(5, song.isUnitSong() ? 1 : 0);
                ps.setInt(6, song.getReleaseYear());
                ps.setInt(7, song.getId());
                ps.executeUpdate();
            }
            return true;
        } catch (SQLException e) {
            System.out.println("❌ Error updating song: " + e.getMessage());
            return false;
        }
    }

    //Deletes one row by ID
    private boolean deleteRow(int id) {
        if (id < 0) return true; // never persisted, nothing to delete
        try (Connection conn = connect()) {
            if (conn == null) return false;
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM songs WHERE id = ?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
            return true;
        } catch (SQLException e) {
            System.out.println("❌ Error deleting song: " + e.getMessage());
            return false;
        }
    }

    //Yes/No helpers
    private boolean parseYesNo(String input) {
        return input.trim().toLowerCase().matches("yes|y|true");
    }

    //Check if performance date is in 2026
    private boolean isValidPerformanceDate(String date) {
        try {
            java.time.LocalDate localDate = java.time.LocalDate.parse(date, java.time.format.DateTimeFormatter.ofPattern("MM/dd/yyyy"));
            return localDate.getYear() == 2026;
        } catch (java.time.format.DateTimeParseException e) {
            return false;
        }
    }

    //CRUD
    public boolean addSong(Song song) {
        if (isDuplicate(song.getSongTitle(), song.getPerformedDate(), song.getCity(), -1)) {
            return false;
        }
        if (!insertRow(song)) return false;
        songs.add(song);
        return true;
    }
    //Returns all songs in the repository
    public List<Song> getAll() { return songs; }

    //Returns a song at the specified index
    public Song get(int index) {
        if (index >= 0 && index < songs.size()) {
            return songs.get(index);
        }
        return null;
    }
    //Returns the total number of songs
    public int size() { return songs.size(); }

    //Deletes a song at the specified index
    public Song delete(int index) {
        if (index >= 0 && index < songs.size()) {
            Song removed = songs.get(index);
            deleteRow(removed.getId());
            songs.remove(index);
            return removed;
        }
        return null;
    }

    //Persists all in memory changes to the database
    //Each song is updated by its row ID
    public void persistAfterUpdate() {
        for (Song s : songs) {
            if (s.getId() < 0) {
                insertRow(s); // safety net: shouldn't normally happen
            } else {
                updateRow(s);
            }
        }
    }

    //Duplicate check
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

    //Import from an external text file
    public ImportResult importFromFile(String path) {
        if (path == null || path.isBlank()) {
            return new ImportResult(0, 0, "File path cannot be empty");
        }

        File file = new File(path);
        if (!file.exists()) {
            return new ImportResult(0, 0, "File not found");
        }

        int added = 0, skipped = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                Song parsed = parseLegacyLine(line);
                if (parsed == null) {
                    skipped++;
                    continue;
                }
                if (isDuplicate(parsed.getSongTitle(), parsed.getPerformedDate(), parsed.getCity(), -1)) {
                    skipped++;
                    continue;
                }
                if (insertRow(parsed)) {
                    songs.add(parsed);
                    added++;
                } else {
                    skipped++;
                }
            }
        } catch (IOException e) {
            return new ImportResult(added, skipped, "Error reading file");
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