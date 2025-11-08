package com.mycompany.nocapgameslauncher.database;

import com.mycompany.nocapgameslauncher.userManager.UserGameData;

import java.sql.*;

import java.io.*;
import java.util.ArrayList;

import com.mycompany.nocapgameslauncher.userManager.UserRepository;

public class DatabaseHandler {
    private static DatabaseHandler instance;
    private static String currentUser;
    private static final String DB_URL = "jdbc:mysql://localhost:3306/nocapserver?allowPublicKeyRetrieval=true&useSSL=false";
    public final String DB_USER = "Admin";
    private static final String DB_PASS = "nocap";
    
    private DatabaseHandler() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found. Add connector dependency.");
        }
    }

    // Getters

    public static DatabaseHandler getInstance() {
        if (instance == null) {
            instance = new DatabaseHandler();
        } return instance;
    }

    public static String getCurrentUser() {
        return currentUser;
    }
    
    public static void setCurrentUser(String username) {
        currentUser = username;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }

    public int getUserId(String username) throws SQLException {
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
    
    // Login
    public boolean login(String username, String password) throws SQLException, IOException {
        // First get the actual username from database (case-sensitive)
        String getUsernameSql = "SELECT username FROM users WHERE username = ? AND password = ?";
        try (Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(getUsernameSql)) {
            
            stmt.setString(1, username);
            stmt.setString(2, password);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String actualUsername = rs.getString("username");
                    
                    // Only create user data file if the username case matches exactly
                    if (!actualUsername.equals(username)) {
                        return false;
                    }
                    
                    // Skip all user data operations for Admin user
                    if (!actualUsername.equals(DB_USER)) {
                        UserRepository userRepo = new UserRepository();
                        UserGameData userData = userRepo.loadUser(actualUsername);
                        if (userData == null) {
                            userData = new UserGameData(actualUsername);
                            userData.setUserID(getUserId(actualUsername));
                            userData.setUsername(actualUsername);
                            userData.setPassword(password);
                            userRepo.saveUser(userData);
                        } else {
                            userData.setUserID(getUserId(actualUsername));
                            userData.setUsername(actualUsername);
                            userData.setPassword(password);
                            userRepo.saveUser(userData);
                        }
                    }
                    
                    // Set the actual username from database
                    setCurrentUser(actualUsername);
                    return true;
                }
                return false;
            }
        }
    }

    public boolean register(String username, String password) throws SQLException {
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

   public void initializeDatabase() {
        // First try to connect to the database
        try (Connection conn = getConnection()) {
            // If we get here, the database exists
            System.out.println("Connected to existing database");
            initializeTables(conn);
        } catch (SQLException e) {
            // If database doesn't exist, create it
            if (e.getMessage().contains("Unknown database")) {
                try (Connection rootConn = DriverManager.getConnection(
                        "jdbc:mysql://localhost:3306/?allowPublicKeyRetrieval=true&useSSL=false", 
                        DB_USER, 
                        DB_PASS);
                    Statement stmt = rootConn.createStatement()) {
                    
                    // Create the database
                    stmt.execute("CREATE DATABASE nocapserver");
                    System.out.println("Created database nocapserver");
                    
                    // Now connect to the new database and create tables
                    try (Connection newDbConn = getConnection()) {
                        initializeTables(newDbConn);
                    }
                } catch (SQLException ex) {
                    System.err.println("Error creating database: " + ex.getMessage());
                    ex.printStackTrace();
                }
            } else {
                System.err.println("Database error: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void initializeTables(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                "userID INT AUTO_INCREMENT PRIMARY KEY, " +
                "username VARCHAR(50) UNIQUE NOT NULL, " +
                "password VARCHAR(255) NOT NULL" +
            ")");
            
            // Insert default admin user if it doesn't exist
            stmt.executeUpdate("INSERT IGNORE INTO users (username, password) VALUES ('Admin', 'nocap')");
            System.out.println("Database tables initialized successfully");
        }
    }

    public void updateGamePlayData(int userId, String gameId, long additionalPlayTime) throws SQLException {
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

    public ArrayList<String> getAllUsers() {
        String sql = "SELECT username FROM users";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            ArrayList<String> users = new ArrayList<>();
            while (rs.next()) {
                users.add(rs.getString("username"));
            }
            return users;
        } catch (SQLException e) {
            return null;
        }
    }
}
