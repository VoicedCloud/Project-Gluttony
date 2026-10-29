package com.github.glutton.screens;

// LibGDX classes for file handling and storing lists of data
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;

// Java classes for getting the current date and time
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class StatsTracker {

    // Name of the JSON file where match history will be saved
    private static final String HISTORY_FILE = "match_history.json";

    // JSON object used to convert match records into JSON format and read them back
    private static final Json json = new Json();

    // ==========================================
    // ACTIVE PLAYTHROUGH COUNTERS
    // These variables track statistics for the current game run.
    // ==========================================

    // Number of waves survived during the current run
    private static int wavesSurvived = 0;

    // Total number of monsters killed during the current run
    private static int monstersKilled = 0;

    // Total amount of cash earned during the current run
    private static int cashEarned = 0;

    // Resets all current-run statistics when a new game starts
    public static void startNewRun() {
        wavesSurvived = 0;
        monstersKilled = 0;
        cashEarned = 0;
    }

    // Adds 1 to the wave counter when a wave is successfully survived
    public static void waveSurvived() {
        wavesSurvived++;
    }

    // Adds 1 to the monster kill counter when a monster is killed
    public static void monsterKilled() {
        monstersKilled++;
    }

    // Adds the earned amount to the cash counter.
    // Negative amounts and zero are ignored.
    public static void earnCash(int amount) {
        if (amount > 0) {
            cashEarned += amount;
        }
    }

    // ==========================================
    // END RUN AND SAVE MATCH HISTORY
    // ==========================================

    // Called when the game run ends.
    // Saves the current run's statistics as a new record
    // in the local JSON match history file.
    public static void endRun() {

        // Load previously saved match records
        Array<MatchRecord> history = loadHistory();

        // Create a record for the current game run
        MatchRecord record = new MatchRecord();

        // Use userId 0 to identify a guest/local match
        record.userId = 0;

        // Store the statistics from the current run
        record.wavesSurvived = wavesSurvived;
        record.monstersKilled = monstersKilled;
        record.cashEarned = cashEarned;

        // Get the current date and time and format it for storage
        // Example: 2026-10-30 18:30
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        record.timestamp = dtf.format(LocalDateTime.now());

        // Add the new match record to the existing history
        history.add(record);

        // Access the local file where match history is stored
        FileHandle file = Gdx.files.local(HISTORY_FILE);

        // Save the updated history as formatted JSON.
        // false means the file will be overwritten with the updated list.
        file.writeString(json.prettyPrint(history), false);

        // Print a message showing that the match was saved
        System.out.println("Match recorded locally! Total stored runs: " + history.size);
    }

    // ==========================================
    // LOAD SAVED MATCH HISTORY
    // ==========================================

    // Reads the match history from the JSON file.
    // If the file does not exist or cannot be read correctly,
    // an empty list is returned.
    public static Array<MatchRecord> loadHistory() {

        // Get the local match history file
        FileHandle file = Gdx.files.local(HISTORY_FILE);

        // If no history file exists yet, return an empty list
        if (!file.exists()) {
            return new Array<>();
        }

        try {
            // Convert the JSON file contents into a list of MatchRecord objects
            return json.fromJson(Array.class, MatchRecord.class, file.readString());
        } catch (Exception e) {

            // Log an error if the JSON data cannot be parsed
            Gdx.app.error("StatsTracker", "Failed to parse match history JSON", e);

            // Return an empty list if loading fails
            return new Array<>();
        }
    }

    // ==========================================
    // RECENT STATS
    // Gets the statistics from the most recently saved match.
    // ==========================================

    // Returns the number of waves survived in the latest saved match
    public static int getRecentWaves() {
        Array<MatchRecord> history = loadHistory();

        // Return 0 if no match records exist
        if (history.size == 0) return 0;

        // peek() returns the last record in the list
        return history.peek().wavesSurvived;
    }

    // Returns the number of monsters killed in the latest saved match
    public static int getRecentMonsters() {
        Array<MatchRecord> history = loadHistory();

        // Return 0 if there are no saved matches
        if (history.size == 0) return 0;

        return history.peek().monstersKilled;
    }

    // Returns the amount of cash earned in the latest saved match
    public static int getRecentCash() {
        Array<MatchRecord> history = loadHistory();

        // Return 0 if there are no saved matches
        if (history.size == 0) return 0;

        return history.peek().cashEarned;
    }

    // Returns the completion value for the latest match.
    // Currently, this is a placeholder that always returns 0.
    public static int getRecentCompletion() {
        return 0;
    }

    // ==========================================
    // HIGHEST STATS
    // Finds the highest recorded value across all saved matches.
    // ==========================================

    // Returns the highest number of waves survived in any saved match
    public static int getHighestWaves() {

        // Load all saved match records
        Array<MatchRecord> history = loadHistory();

        // Start the highest value at 0
        int max = 0;

        // Check each match record
        for (MatchRecord r : history) {

            // Update max if this match has a higher wave count
            if (r.wavesSurvived > max) {
                max = r.wavesSurvived;
            }
        }

        return max;
    }

    // Returns the highest number of monsters killed in any saved match
    public static int getHighestMonsters() {
        Array<MatchRecord> history = loadHistory();
        int max = 0;

        // Compare the monster kill count of every saved match
        for (MatchRecord r : history) {
            if (r.monstersKilled > max) {
                max = r.monstersKilled;
            }
        }

        return max;
    }

    // Returns the highest amount of cash earned in any saved match
    public static int getHighestCash() {
        Array<MatchRecord> history = loadHistory();
        int max = 0;

        // Compare the cash earned in every saved match
        for (MatchRecord r : history) {
            if (r.cashEarned > max) {
                max = r.cashEarned;
            }
        }

        return max;
    }

    // Returns the highest completion value across saved matches.
    // Currently, this is a placeholder that always returns 0.
    public static int getHighestCompletion() {
        return 0;
        }
}