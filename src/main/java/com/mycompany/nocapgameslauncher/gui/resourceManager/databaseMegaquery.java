package com.mycompany.nocapgameslauncher.gui.resourceManager;

import com.mycompany.nocapgameslauncher.database.DatabaseHandler;

import javax.swing.*;

import java.awt.*;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

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
        scanButton.addActionListener(e -> scanAndProcessGames());
        buttonPanel.add(scanButton);
        
        JButton clearButton = new JButton("Clear Log");
        clearButton.addActionListener(e -> logArea.setText(""));
        buttonPanel.add(clearButton);
        
        // Add the button panel to the frame
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    // In databaseMegaquery.java, update the scanAndProcessGames method
    private void scanAndProcessGames() {
        logArea.append("Starting game scan...\n");
        
        // Use the correct path for the Executables directory
        File execDir = new File("src/main/resources/Executables");
        if (!execDir.exists() || !execDir.isDirectory()) {
            logArea.append("Error: Executables directory not found at " + execDir.getAbsolutePath() + "\n");
            logArea.append("Please ensure the 'Executables' folder exists in src/main/resources/ alongside ImageResources.\n");
            return;
        }
        
        // Filter for .lnk files
        File[] lnkFiles = execDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".lnk"));
        if (lnkFiles == null || lnkFiles.length == 0) {
            logArea.append("No .lnk files found in Executables directory.\n");
            return;
        }
        
        logArea.append("Found " + lnkFiles.length + " .lnk files in " + execDir.getAbsolutePath() + ". Processing...\n");
        
        int processedCount = 0;
        for (File file : lnkFiles) {
            try {
                int gameID = processedCount + 1;
                String fileName = file.getName();
                String gameName = fileName.substring(0, fileName.lastIndexOf('.'));
                gameName = gameName.replace(" ", "_").toLowerCase();
                String filePath = file.getAbsolutePath();
                
                String query = "INSERT INTO gameData (gameID, gameName, gameURL) VALUES (?, ?, ?)";
                String url = "jdbc:mysql://localhost:3306/NoCapServer?useSSL=false&allowPublicKeyRetrieval=true";
                try (Connection conn = DriverManager.getConnection(url, "Admin", "nocap");
                    PreparedStatement stmt = conn.prepareStatement(query)) {
                    
                    stmt.setInt(1, gameID);
                    stmt.setString(2, gameName);
                    stmt.setString(3, filePath);
                    
                    stmt.executeUpdate();
                }
                
                logArea.append("Processed: " + fileName + " -> " + gameName + "\n");
                processedCount++;
            } catch (Exception e) {
                logArea.append("Error processing " + file.getName() + ": " + e.getMessage() + "\n");
            }
        } logArea.append("Processing complete. " + processedCount + " games processed.\n");
    }

    private void addMegaQueryButton(JPanel buttonPanel) {
        JButton megaQueryButton = new JButton("Run MegaQuery");
        megaQueryButton.setBackground(new Color(70, 130, 180));
        megaQueryButton.setForeground(Color.WHITE);
        megaQueryButton.setFocusPainted(false);
        megaQueryButton.addActionListener(e -> runMegaQuery());
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
            "gameURL TEXT" +
        ")",
        "CREATE TABLE IF NOT EXISTS ownedGames (" +
            "userID INT, " +
            "gameID INT, " +
            "PRIMARY KEY (userID, gameID), " +
            "FOREIGN KEY (userID) REFERENCES users(userID), " +
            "FOREIGN KEY (gameID) REFERENCES gameData(gameID)" +
        ")",
    };

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