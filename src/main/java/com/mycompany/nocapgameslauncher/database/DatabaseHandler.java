package com.mycompany.nocapgameslauncher.database;

import com.mycompany.nocapgameslauncher.gui.resourceHandling.resourceLoader;
import com.mycompany.nocapgameslauncher.gui.userManager.UserGameData;

import java.sql.*;

import com.google.gson.*;

import java.io.*;
import java.nio.file.*;

import com.mycompany.nocapgameslauncher.gui.userManager.UserRepository;

public class DatabaseHandler {
    private static String currentUser;
    
    public static String getCurrentUser() {
        return currentUser;
    }
    
    public static void setCurrentUser(String username) {
        currentUser = username;
    }
    private static final Gson gson = new Gson();
    private static final String USERS_DIR = resourceLoader.RESOURCE_DIRECTORY + "Users/";
    private static final String DB_URL = "jdbc:mysql://localhost:3306/nocapserver";
    private static final String DB_USER = "Admin";
    private static final String DB_PASS = "nocap";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found. Add connector dependency.");
        }
    }

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }

    public static boolean register(String username, String password) throws SQLException {
    String insertSql = "INSERT INTO users (username, password) " +
                      "SELECT ?, ? FROM DUAL " +
                      "WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = ?)";

    try (Connection conn = getConnection();
         PreparedStatement pstmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
        
        pstmt.setString(1, username);
        pstmt.setString(2, password);
        pstmt.setString(3, username);  // For the NOT EXISTS check
        
        int affectedRows = pstmt.executeUpdate();
        return affectedRows > 0;  // Returns true if a new user was inserted
    }
}

    public static void initializeDatabase() {
        String[] initQueries = {
            "CREATE DATABASE IF NOT EXISTS nocapserver",
            "USE nocapserver",
            "CREATE TABLE IF NOT EXISTS users (" +
                "userID INT AUTO_INCREMENT PRIMARY KEY, " +
                "username VARCHAR(50) UNIQUE NOT NULL, " +
                "password VARCHAR(255) NOT NULL" +
            ")",
            "INSERT IGNORE INTO users (username, password) VALUES ('Admin', 'nocap')"
        };

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            for (String query : initQueries) {
                stmt.execute(query);
            }
            System.out.println("Database initialized successfully");
        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
        }
    }
    
        public static boolean login(String username, String password) throws SQLException, IOException {
        String sql = "SELECT userID FROM users WHERE username = ? AND password = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password);
            try (ResultSet rs = stmt.executeQuery()) {
                boolean loginSuccess = rs.next();
                if (loginSuccess && !username.equals("Admin")) {
                    // Skip user data management for Admin user
                    // For all other users, manage their data file
                    UserRepository userRepo = new UserRepository();
                    UserGameData userData = userRepo.loadUser(username);
                    
                    // If user data doesn't exist or needs to be refreshed
                    if (userData == null) {
                        userData = new UserGameData(username);
                    }
                    
                    // Update user data with latest info
                    userData.setUserID(getUserId(username));
                    userData.setUsername(username);
                    userData.setPassword(password); // Store the password (for testing only)
                    
                    // Save the user data (will overwrite existing file)
                    userRepo.saveUser(userData);
                }
                return loginSuccess;
            }
        }
    }
    
    public static int getUserId(String username) throws SQLException {
        String sql = "SELECT userID FROM users WHERE username = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("userID");
                }
            }
        }
        return -1; // User not found
    }
    
    public static UserGameData getUserGames(String username) {
        String filename = USERS_DIR + username + ".json";
        try {
            String json = new String(Files.readAllBytes(Paths.get(filename)));
            UserGameData data = gson.fromJson(json, UserGameData.class);
            if (data == null) {
                throw new IllegalStateException("Failed to parse user data for: " + username);
            }
            return data;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read user data for: " + username, e);
        }
    }
    
    public static boolean addGameToUser(String username, int gameId) {
        try {
            UserGameData userData = getUserGames(username);
            if (userData == null) return false;
            
            userData.addGame(gameId);
            saveUserData(userData);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    private static void saveUserData(UserGameData userData) {
        String filename = USERS_DIR + userData.getUsername() + ".json";
        try (Writer writer = new FileWriter(filename)) {
            gson.toJson(userData, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void updateGamePlayData(int userId, String gameId, long additionalPlayTime) throws SQLException {
        String updateSql = "UPDATE user_games SET lastPlayed = NOW(), playTime = playTime + ? " +
                         "WHERE userId = ? AND gameId = ?";
        String insertSql = "INSERT INTO user_games (userId, gameId, lastPlayed, playTime) " +
                         "SELECT ?, ?, NOW(), ? FROM DUAL " +
                         "WHERE NOT EXISTS (SELECT 1 FROM user_games WHERE userId = ? AND gameId = ?)";
        
        try (Connection conn = getConnection()) {
            // First try to update existing record
            try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
                stmt.setLong(1, additionalPlayTime);
                stmt.setInt(2, userId);
                stmt.setString(3, gameId);
                int updated = stmt.executeUpdate();
                
                // If no rows were updated, insert new record
                if (updated == 0) {
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                        insertStmt.setInt(1, userId);
                        insertStmt.setString(2, gameId);
                        insertStmt.setLong(3, additionalPlayTime);
                        insertStmt.setInt(4, userId);
                        insertStmt.setString(5, gameId);
                        insertStmt.executeUpdate();
                    }
                }
            }
        }
    }
}
