/*Name  : Phuong Dang
Course  : CEN3024C-31032
Date    : 07/01/2026
Data model representing a single BTS surprise song performance record.
 */

/**
 * Data model representing a single BTS surprise song performance record
 */
public class Song {

    /**
     * Constructs a new Song instance with performance details
     *
     * @param songTitle    the title of the song
     * @param performedDate the date the song was performed (MM/dd/yyyy)
     * @param city          the city where the song was performed
     * @param timesPlayed   the total number of times the song has been played
     * @param isUnitSong    true if the song is a unit song, false otherwise
     * @param releaseYear   the year the song was officially released
     */
    private int id = -1;
    private String songTitle;
    private String performedDate;
    private String city;
    private int timesPlayed;
    private boolean isUnitSong;
    private int releaseYear;

    public Song(String songTitle, String performedDate, String city,
                int timesPlayed, boolean isUnitSong, int releaseYear) {
        this.id = -1;
        this.songTitle = songTitle;
        this.performedDate = performedDate;
        this.city = city;
        this.timesPlayed = timesPlayed;
        this.isUnitSong = isUnitSong;
        this.releaseYear = releaseYear;
    }

    // Getters
    /** @return the database ID of the song */
    public int getId() { return id; }
    /** @return the title of the song */
    public String getSongTitle() { return songTitle; }
    /** @return the date the song was performed */
    public String getPerformedDate() { return performedDate; }
    /** @return the city of performance */
    public String getCity() { return city; }
    /** @return total times the song has been played */
    public int getTimesPlayed() { return timesPlayed; }
    /** @return true if it is a unit song, false if group song */
    public boolean isUnitSong() { return isUnitSong; }
    /** @return the release year of the song */
    public int getReleaseYear() { return releaseYear; }

    // Setters
    /** @param id the database ID to set */
    public void setId(int id) {this.id = id;}
    /** @param songTitle the title to set */
    public void setSongTitle(String songTitle) { this.songTitle = songTitle; }
    /** @param performedDate the performance date to set */
    public void setPerformedDate(String performedDate) { this.performedDate = performedDate; }
    /** @param city the city to set */
    public void setCity(String city) { this.city = city; }
    /** @param timesPlayed the play count to set */
    public void setTimesPlayed(int timesPlayed) { this.timesPlayed = timesPlayed; }
    /** @param unitSong set true if unit song, false otherwise */
    public void setUnitSong(boolean unitSong) { isUnitSong = unitSong; }
    /** @param releaseYear the release year to set */
    public void setReleaseYear(int releaseYear) { this.releaseYear = releaseYear; }

    /**
     * Returns a formatted String representation of the song
     *
     * @return song details formatted as a string
     */
    @Override
    public String toString() {
        return String.format(
                "Title: %-25s | Date: %-10s | City: %-12s | Plays: %-3d | Unit: %-4s | Year: %-4d | Score: %.1f",
                songTitle, performedDate, city, timesPlayed,
                isUnitSong ? "yes" : "no", releaseYear,
                BorahaeCalculator.calculate(this)
        );
    }
}