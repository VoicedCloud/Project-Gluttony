package com.github.glutton;

import com.badlogic.gdx.utils.JsonValue;

public class SessionManager {
    private static boolean loggedIn = false;
    private static long userId = 0;
    private static String username = "Guest";
    
    public static JsonValue recentStats = null;
    public static JsonValue highestStats = null;

    public static boolean isLoggedIn() { return loggedIn; }
    public static long getUserId() { return userId; }
    public static String getUsername() { return username; }

    public static void login(long id, String name) {
        loggedIn = true;
        userId = id;
        username = name;
    }

    public static void logout() {
        loggedIn = false;
        userId = 0;
        username = "Guest";
        recentStats = null;
        highestStats = null;
    }
}