package com.mycompany.nocapgameslauncher.database;

import javax.swing.*;

import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

import com.mycompany.nocapgameslauncher.NoCapGamesLauncher;
import com.mycompany.nocapgameslauncher.gui.panels.LoginForm;

public class databaseMegaquery extends JFrame {
    private DatabaseHandler dbHandler = new DatabaseHandler();
    private JTextArea logArea;
    private JScrollPane scrollPane;
    private int processedCount = 0;
    private JSONArray gamesArray = new JSONArray();
    
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
        JButton scanButton = new JButton("Scan Games");
        scanButton.addActionListener(_ -> scanAndProcessGames());
        buttonPanel.add(scanButton);

        JButton createButton = new JButton("Save to File");
        createButton.addActionListener(_ -> saveGamesToFile());
        buttonPanel.add(createButton);
        
        JButton clearButton = new JButton("Clear Log");
        clearButton.addActionListener(_ -> logArea.setText(""));
        buttonPanel.add(clearButton);

        JButton backButton = new JButton("Back");
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                NoCapGamesLauncher.main(null);
            }
        });
        buttonPanel.add(backButton);
        
        // Add the button panel to the frame
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
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
        
        gamesArray = new JSONArray(); // Reset the array for new scan
        String url = "jdbc:mysql://localhost:3306/nocapserver?useSSL=false&allowPublicKeyRetrieval=true";
        String user = "Admin";
        String password = "nocap";
        
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            String clearQuery = "TRUNCATE TABLE gameData";
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(clearQuery);
                logArea.append("Rebooting table\n");
            }
            
            String insertQuery = "INSERT INTO gameData (gameName, gameURL, imageURL, gameDescription) VALUES (?, ?, ?, ?)";
            
            for (File file : lnkFiles) {
                try {
                    String fileName = file.getName();
                    String gameName = fileName.substring(0, fileName.lastIndexOf('.')).replace(" ", "_").toLowerCase();
                    String gamePath = file.getAbsolutePath();
                    String gameIconPath = "src/main/resources/ImageResources/" + fileName.toLowerCase().replace(".lnk", ".jpg");
                    String gameDescription = "This is a description";
                    
                    // Insert into database
                    try (PreparedStatement pstmt = conn.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {
                        pstmt.setString(1, gameName);
                        pstmt.setString(2, gamePath);
                        pstmt.setString(3, gameIconPath);
                        pstmt.setString(4, gameDescription);
                        
                        int affectedRows = pstmt.executeUpdate();
                        
                        if (affectedRows > 0) {
                            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                                if (generatedKeys.next()) {
                                    int generatedId = generatedKeys.getInt(1);
                                    createGameMetadata(generatedId, gameName, gamePath, gameIconPath, gameDescription);
                                    logArea.append("Processed: " + fileName + " -> " + gameName + "\n");
                                    processedCount++;
                                }
                            }
                        }
                    }
                } catch (SQLException | JSONException e) {
                    logArea.append("Error processing " + file.getName() + ": " + e.getMessage() + "\n");
                }
            }
                    
        } catch (SQLException e) {
            logArea.append("Database error: " + e.getMessage() + "\n");
        }
    }
            
    private void createGameMetadata(int gameId, String gameName, String gamePath, String imageUrl, String description) {
        try {
            JSONObject game = new JSONObject();
            game.put("gameID", gameId);
            game.put("gameName", gameName);
            game.put("gameURL", gamePath);
            game.put("imageURL", imageUrl);
            game.put("gameDescription", description);
            gamesArray.put(game);
        } catch (JSONException e) {
            logArea.append("Error creating game metadata: " + e.getMessage() + "\n");
        }
    }
    
    private void saveGamesToFile() {
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
    }


    private void addMegaQueryButton(JPanel buttonPanel) {
        JButton megaQueryButton = new JButton("Run MegaQuery");
        megaQueryButton.setBackground(new Color(70, 130, 180));
        megaQueryButton.setForeground(Color.WHITE);
        megaQueryButton.setFocusPainted(false);
        megaQueryButton.addActionListener(_ -> runMegaQuery());
        buttonPanel.add(megaQueryButton);
    }

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