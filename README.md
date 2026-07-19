Phuong Dang - CEN3024C-31032

# BTS Surprise Song Tracker MDS

A Java Data Management System for tracking BTS surprise songs on the ARIRANG World Tour. Users can log performances, update play counts, and calculate the Borahae rarity score.

Updated: connect to the database SQLite

## Features
- Add songs manually or import from `.txt` file
- View all songs with Borahae scores
- Edit/update song details (title, date, city, plays, unit song, year)
- Delete songs with confirmation
- Borahae Meter displays rarity with 💜 hearts
  
## Formula
Borahae Score = (2026 - Year) + (15 - Plays)

## Run
java -jar "BTS DMS.jar"

## Menu
1. Add Song
2. View Songs
3. Edit Song
4. Delete Song
5. Borahae Meter
0. Exit

## File Format (`songs.txt`)
```
Title|MM/dd/yyyy|City|Times Played|Unit Song (yes/no)|Release Year

## Validation
- ✅ Empty inputs handled
- ✅ Date format: MM/dd/yyyy
- ✅ Performance year must be 2026 (Ariang Tour)
- ✅ Release year: 2013-2026
- ✅ Play count: non-negative
- ✅ Unit song: yes/no
- ✅ Duplicate detection


