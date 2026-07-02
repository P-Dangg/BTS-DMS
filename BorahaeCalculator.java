/*Name  : Phuong Dang
Course  : CEN3024C-31032
Date    : 07/01/2026
Custom feature: calculates the rarity score for a song
Formula: (Current Year - Release Year) + (15 - Play Count)
Higher score = older / rarer song that hasn't been played much = more special.
 */


import java.time.Year;

public class BorahaeCalculator {
    public static double calculate(Song song) {
        int currentYear = Year.now().getValue();
        return (currentYear - song.getReleaseYear()) + (15 - song.getTimesPlayed());
    }
}