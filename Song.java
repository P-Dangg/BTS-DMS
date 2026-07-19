/*Name  : Phuong Dang
Course  : CEN3024C-31032
Date    : 07/01/2026
Data model representing a single BTS surprise song performance record.
 */

public class Song {
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
    public int getId() { return id; }
    public String getSongTitle() { return songTitle; }
    public String getPerformedDate() { return performedDate; }
    public String getCity() { return city; }
    public int getTimesPlayed() { return timesPlayed; }
    public boolean isUnitSong() { return isUnitSong; }
    public int getReleaseYear() { return releaseYear; }

    // Setters
    public void setId(int id) {this.id = id;}
    public void setSongTitle(String songTitle) { this.songTitle = songTitle; }
    public void setPerformedDate(String performedDate) { this.performedDate = performedDate; }
    public void setCity(String city) { this.city = city; }
    public void setTimesPlayed(int timesPlayed) { this.timesPlayed = timesPlayed; }
    public void setUnitSong(boolean unitSong) { isUnitSong = unitSong; }
    public void setReleaseYear(int releaseYear) { this.releaseYear = releaseYear; }

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