package com.github.glutton.screens;

public class UserSession {

	// Default to Guest state (ID 0)
	private static int userId = 0;
	private static String username = "Guest";
	private static boolean loggedIn = false;

	// Called when PHP authentication succeeds and returns user data
	public static void login(int id, String name) {
		userId = id;
		username = name;
		loggedIn = true;
	}

	// Clears credentials back to guest state on logout
	public static void logout() {
		userId = 0;
		username = "Guest";
		loggedIn = false;
	}

	public static int getUserId() {
		return userId;
	}

	public static String getUsername() {
		return username;
	}

	public static boolean isLoggedIn() {
		return loggedIn;
	}
}
