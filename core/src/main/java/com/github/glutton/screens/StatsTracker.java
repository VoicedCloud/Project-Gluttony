package com.github.glutton.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

public class StatsTracker {

    private static final Preferences stats =
            Gdx.app.getPreferences("ProjectGluttonyStats");

    // Current playthrough statistics
    private static int wavesSurvived = 0;
    private static int monstersKilled = 0;
    private static int cashEarned = 0;

    // Highest overall records
    private static int highestWaves =
            stats.getInteger("highestWaves", 0);

    private static int highestKills =
            stats.getInteger("highestKills", 0);

    private static int highestCash =
            stats.getInteger("highestCash", 0);

    // Most recent playthrough
    private static int recentWaves =
            stats.getInteger("recentWaves", 0);

    private static int recentKills =
            stats.getInteger("recentKills", 0);

    private static int recentCash =
            stats.getInteger("recentCash", 0);

    // Start a new playthrough
    public static void startNewRun() {
        wavesSurvived = 0;
        monstersKilled = 0;
        cashEarned = 0;

        System.out.println("New playthrough started!");
    }

    // Record a successfully completed wave
    public static void waveSurvived() {
        wavesSurvived++;
    }

    // Record a monster kill
    public static void monsterKilled() {
        monstersKilled++;
    }

    // Record cash earned from monster rewards
    public static void earnCash(int amount) {
        if (amount > 0) {
            cashEarned += amount;
        }
    }

    // Save statistics when the playthrough ends
    public static void endRun() {

        // Update highest records independently
        highestWaves = Math.max(
                highestWaves, wavesSurvived);

        highestKills = Math.max(
                highestKills, monstersKilled);

        highestCash = Math.max(
                highestCash, cashEarned);

        // Update most recent playthrough
        recentWaves = wavesSurvived;
        recentKills = monstersKilled;
        recentCash = cashEarned;

        // Save highest overall records
        stats.putInteger("highestWaves", highestWaves);
        stats.putInteger("highestKills", highestKills);
        stats.putInteger("highestCash", highestCash);

        // Save most recent statistics
        stats.putInteger("recentWaves", recentWaves);
        stats.putInteger("recentKills", recentKills);
        stats.putInteger("recentCash", recentCash);

        stats.flush();

        printStats();
    }

    // Print statistics in the NetBeans Output window
    public static void printStats() {

        System.out.println();
        System.out.println("========== PROJECT-GLUTTONY STATS ==========");

        System.out.println();
        System.out.println("HIGHEST OVERALL STATS");
        System.out.println("Waves Survived : " + highestWaves);
        System.out.println("Monsters Killed: " + highestKills);
        System.out.println("Cash Earned    : " + highestCash);

        System.out.println();
        System.out.println("MOST RECENT PLAYTHROUGH");
        System.out.println("Waves Survived : " + recentWaves);
        System.out.println("Monsters Killed: " + recentKills);
        System.out.println("Cash Earned    : " + recentCash);

        System.out.println("============================================");
        System.out.println();
    }
}