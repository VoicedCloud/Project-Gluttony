
package com.github.glutton.screens;

public class UserSession {

    // ==========================================
    // USER SESSION VARIABLES
    // These variables store the currently active user's information.
    // ==========================================

    // Stores the user's ID.
    // ID 0 means the player is using a Guest account.
    private static int userId = 0;

    // Stores the current username.
    // The default username is "Guest".
    private static String username = "Guest";

    // Tracks whether a user has successfully logged in.
    // false means Guest mode or no authenticated user.
    private static boolean loggedIn = false;

    // ==========================================
    // LOGIN
    // ==========================================

    // Called after PHP authentication succeeds.
    // Updates the session using the user's ID and username
    // returned by the authentication system.
    public static void login(int id, String name) {
        userId = id;
        username = name;
        loggedIn = true;
    }

    // ==========================================
    // LOGOUT
    // ==========================================

    // Clears the current user's session and returns to Guest mode.
    public static void logout() {
        userId = 0;
        username = "Guest";
        loggedIn = false;
    }

    // ==========================================
    // GETTERS
    // These methods allow other classes to read session information.
    // ==========================================

    // Returns the ID of the currently active user.
    // Returns 0 when using Guest mode.
    public static int getUserId() {
        return userId;
    }

    // Returns the username of the currently active user.
    public static String getUsername() {
        return username;
    }

    // Returns true if a user has logged in successfully.
    // Returns false in Guest mode.
    public static boolean isLoggedIn() {
        return loggedIn;
    }
}
