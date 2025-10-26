package com.mycompany.nocapgameslauncher.database;

import java.sql.*;

public class DatabaseHandler {
    // Change these values as you wish
    private static final String DB_URL = "jdbc:mysql://localhost:3306/nocapserver";
    private static final String DB_USER = "Admin";
    private static final String DB_PASS = "nocap";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found. Add connector dependency.");
            e.printStackTrace();
        }
    }

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }

    public static boolean register(String username, String password) throws SQLException {
        String checkSql = "SELECT userID FROM users WHERE username = ?";
        String insertSql = "INSERT INTO users (username, password) VALUES (?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {

            checkStmt.setString(1, username);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next()) {
                    return false; // user exists
                }
            }

            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                insertStmt.setString(1, username);
                insertStmt.setString(2, password); // NOTE: store hashed passwords in production
                int affected = insertStmt.executeUpdate();
                return affected == 1;
            }
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
    
    public static boolean login(String username, String password) throws SQLException {
        String sql = "SELECT userID FROM users WHERE username = ? AND password = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }
}
