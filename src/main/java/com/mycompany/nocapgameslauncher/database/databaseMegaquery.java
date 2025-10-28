package com.mycompany.nocapgameslauncher.database;

import javax.swing.*;

import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.json.JSONException;

public class databaseMegaquery extends JFrame {
    private DatabaseHandler dbHandler = new DatabaseHandler();
    private JTextArea logArea;
    private JScrollPane scrollPane;
    
    public databaseMegaquery() {
        initializeFrame();
        setupUI();
    }
    
    private void initializeFrame() {
        setTitle("No Cap Games - Database Mega Query");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);
        setLocationRelativeTo(null);
        setResizable(true);
        getContentPane().setBackground(new Color(32, 32, 32));
    }
    
    private void setupUI() {
        setLayout(new BorderLayout());

        // Title label
        JLabel titleLabel = new JLabel("Game Data Scanner", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        add(titleLabel, BorderLayout.NORTH);

        // Log area for output
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setBackground(new Color(50, 50, 50));
        logArea.setForeground(Color.WHITE);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        scrollPane = new JScrollPane(logArea);
        add(scrollPane, BorderLayout.CENTER);
        
        // Button panel at the bottom
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(32, 32, 32));
        
        // Add the MegaQuery button
        addMegaQueryButton(buttonPanel);
        
        // Add other buttons (scan, clear) if needed
        JButton scanButton = new JButton("Scan and Process Games");
        scanButton.addActionListener(_ -> scanAndProcessGames());
        buttonPanel.add(scanButton);
        
        JButton clearButton = new JButton("Clear Log");
        clearButton.addActionListener(_ -> logArea.setText(""));
        buttonPanel.add(clearButton);
        
        // Add the button panel to the frame
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    // In databaseMegaquery.java, update the scanAndProcessGames method
    private void scanAndProcessGames() {
    logArea.append("Starting game scan...\n");
    
    File execDir = new File("src/main/resources/Executables");
    if (!execDir.exists() || !execDir.isDirectory()) {
        logArea.append("Error: Executables directory not found at " + execDir.getAbsolutePath() + "\n");
        return;
    }
    
    File[] lnkFiles = execDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".lnk"));
    if (lnkFiles == null || lnkFiles.length == 0) {
        logArea.append("No .lnk files found in Executables directory.\n");
        return;
    }
    
    JSONArray gamesArray = new JSONArray();
    int processedCount = 0;
    
    // Database connection URL - use the same one as in DatabaseHandler
    String url = "jdbc:mysql://localhost:3306/nocapserver?useSSL=false&allowPublicKeyRetrieval=true";
    String user = "Admin";
    String password = "nocap";
    
    try (Connection conn = DriverManager.getConnection(url, user, password)) {
        for (File file : lnkFiles) {
            try {
                String fileName = file.getName();

                String gameName = fileName.substring(0, fileName.lastIndexOf('.')).replace(" ", "_").toLowerCase();
                String gamePath = file.getAbsolutePath();
                String gameIconPath = "src/main/resources/ImageResources/" + fileName.toLowerCase().replace(".lnk", ".jpg");
                String gameDescription = "This is a description";
                
                // Insert into database
                int generatedId = -1;
                String insertQuery = "INSERT INTO gameData (gameName, gameURL, imageURL, gameDescription) " +
                                "SELECT ?, ?, ?, ? FROM DUAL " +
                                "WHERE NOT EXISTS (SELECT 1 FROM gameData WHERE gameName = ?)";

                try (PreparedStatement pstmt = conn.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {
                    pstmt.setString(1, gameName);
                    pstmt.setString(2, gamePath);
                    pstmt.setString(3, gameIconPath);
                    pstmt.setString(4, gameDescription);
                    pstmt.setString(5, gameName); // This is for the WHERE NOT EXISTS check
                    int affectedRows = pstmt.executeUpdate();

                    if (affectedRows > 0) {
                        try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                            if (generatedKeys.next()) {
                                generatedId = generatedKeys.getInt(1);
                            }
                        }
                    }
                }
                
                // Add to JSON array
                JSONObject game = new JSONObject();
                game.put("gameId", generatedId);
                game.put("gameName", gameName);
                game.put("executablePath", gamePath);
                game.put("gameIconPath", gameIconPath);
                game.put("gameDescription", gameDescription);
                gamesArray.put(game);
                
                logArea.append("Processed: " + fileName + " -> " + gameName + "\n");
                processedCount++;
                
            } catch (SQLException | JSONException e) {
                logArea.append("Error processing " + file.getName() + ": " + e.getMessage() + "\n");
            }
        }
        
        // Save to JSON file
        try {
            Files.createDirectories(Paths.get("src/main/resources/"));
            try (FileWriter writer = new FileWriter("src/main/resources/store_games.json")) {
                writer.write(gamesArray.toString(4));
                logArea.append("Saved " + processedCount + " games to store_games.json\n");
            }
        } catch (IOException e) {
            logArea.append("Error saving games to file: " + e.getMessage() + "\n");
        }
        
        logArea.append("Processing complete. " + processedCount + " games processed.\n");
        
    } catch (SQLException e) {
        logArea.append("Database error: " + e.getMessage() + "\n");
        }
    }

    private void addMegaQueryButton(JPanel buttonPanel) {
        JButton megaQueryButton = new JButton("Run MegaQuery");
        megaQueryButton.setBackground(new Color(70, 130, 180));
        megaQueryButton.setForeground(Color.WHITE);
        megaQueryButton.setFocusPainted(false);
        megaQueryButton.addActionListener(_ -> runMegaQuery());
        buttonPanel.add(megaQueryButton);
    }

// Add this method to your databaseMegaquery class
private void runMegaQuery() {
    String[] queries = {
        "CREATE DATABASE IF NOT EXISTS NoCapServer",
        "USE NoCapServer",
        "CREATE TABLE IF NOT EXISTS gameData (" +
            "gameID INT AUTO_INCREMENT PRIMARY KEY, " +
            "gameName VARCHAR(100) NOT NULL, " +
            "gameURL TEXT, " +
            "imageURL TEXT, " +
            "gameDescription TEXT" +
        ")"
    }; // Game Description can be modified, it has a default value, just ALTER it

    String url = "jdbc:mysql://localhost:3306/mysql?useSSL=false&allowPublicKeyRetrieval=true";
    try (Connection conn = DriverManager.getConnection(url, "Admin", "nocap")) {
        for (String query : queries) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(query);
                logArea.append("Executed: " + query.split("\\s+")[0] + "...\n");
            } catch (SQLException ex) {
                logArea.append("Error executing query: " + ex.getMessage() + "\n");
                return;
            }
        }
        logArea.append("MegaQuery executed successfully!\n");
    } catch (SQLException ex) {
        logArea.append("Database connection failed: " + ex.getMessage() + "\n");
    }
    }
}