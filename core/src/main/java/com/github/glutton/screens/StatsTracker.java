package com.github.glutton.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class StatsTracker {

    private static final String HISTORY_FILE = "match_history.json";
    private static final Json json = new Json();

    // Active playthrough counters
    private static int wavesSurvived = 0;
    private static int monstersKilled = 0;
    private static int cashEarned = 0;

    public static void startNewRun() {
        wavesSurvived = 0;
        monstersKilled = 0;
        cashEarned = 0;
    }

    public static void waveSurvived() {
        wavesSurvived++;
    }

    public static void monsterKilled() {
        monstersKilled++;
    }

    public static void earnCash(int amount) {
        if (amount > 0) {
            cashEarned += amount;
        }
    }

    // Called when the run ends; appends this match to the local JSON history list
    public static void endRun() {
        Array<MatchRecord> history = loadHistory();

        MatchRecord record = new MatchRecord();
        record.userId = 0; // Guest local storage
        record.wavesSurvived = wavesSurvived;
        record.monstersKilled = monstersKilled;
        record.cashEarned = cashEarned;

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        record.timestamp = dtf.format(LocalDateTime.now());

        history.add(record);

        FileHandle file = Gdx.files.local(HISTORY_FILE);
        file.writeString(json.prettyPrint(history), false);

        System.out.println("Match recorded locally! Total stored runs: " + history.size);
    }

    public static Array<MatchRecord> loadHistory() {
        FileHandle file = Gdx.files.local(HISTORY_FILE);
        if (!file.exists()) {
            return new Array<>();
        }
        try {
            return json.fromJson(Array.class, MatchRecord.class, file.readString());
        } catch (Exception e) {
            Gdx.app.error("StatsTracker", "Failed to parse match history JSON", e);
            return new Array<>();
        }
    }

    // --- RECENT STATS (Gets the last played match) ---
    public static int getRecentWaves() {
        Array<MatchRecord> history = loadHistory();
        if (history.size == 0) return 0;
        return history.peek().wavesSurvived;
    }

    public static int getRecentMonsters() {
        Array<MatchRecord> history = loadHistory();
        if (history.size == 0) return 0;
        return history.peek().monstersKilled;
    }

    public static int getRecentCash() {
        Array<MatchRecord> history = loadHistory();
        if (history.size == 0) return 0;
        return history.peek().cashEarned;
    }

    public static int getRecentCompletion() {
        return 0; // Default completion boolean/metric
    }

    // --- HIGHEST STATS (Scans history for max values) ---
    public static int getHighestWaves() {
        Array<MatchRecord> history = loadHistory();
        int max = 0;
        for (MatchRecord r : history) {
            if (r.wavesSurvived > max) max = r.wavesSurvived;
        }
        return max;
    }

    public static int getHighestMonsters() {
        Array<MatchRecord> history = loadHistory();
        int max = 0;
        for (MatchRecord r : history) {
            if (r.monstersKilled > max) max = r.monstersKilled;
        }
        return max;
    }

    public static int getHighestCash() {
        Array<MatchRecord> history = loadHistory();
        int max = 0;
        for (MatchRecord r : history) {
            if (r.cashEarned > max) max = r.cashEarned;
        }
        return max;
    }

    public static int getHighestCompletion() {
        return 0;
    }
}