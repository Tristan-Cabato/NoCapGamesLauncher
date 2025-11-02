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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;

import com.mycompany.nocapgameslauncher.NoCapGamesLauncher;
import com.mycompany.nocapgameslauncher.game_manager.Game;
import com.mycompany.nocapgameslauncher.game_manager.GameManager;
import com.mycompany.nocapgameslauncher.userManager.UserMemento;

public class databaseMegaquery extends JFrame {
    private DatabaseHandler database = DatabaseHandler.getInstance();
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
        scanButton.addActionListener(_ -> scanGames());
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
    
   private void scanGames() {
        logArea.append("Starting game scan...\n");
        
        File execDir = new File("src/main/resources/Executables");
        if (!execDir.exists() || !execDir.isDirectory()) {
            logArea.append("Executables directory not found.\n");
            return;
        }

        File[] files = execDir.listFiles((dir, name) -> name.endsWith(".lnk"));
        if (files == null || files.length == 0) {
            logArea.append("No game files found in Executables directory.\n");
            return;
        }

        processedCount = 0;
        for (File file : files) {
            try {
                String fileName = file.getName();
                String baseName = fileName.substring(0, fileName.lastIndexOf('.'));
                String gameName = baseName.replace(" ", "_").toLowerCase();
                String gamePath = file.getAbsolutePath();
                String gameIconPath = "src/main/resources/ImageResources/" + baseName.toLowerCase() + ".jpg";
                String gameDescription = "This is a description";

                // Insert into database
                String sql = "INSERT INTO gameData (gameName, gameURL, imageURL, gameDescription) VALUES (?, ?, ?, ?)";
                try (Connection conn = database.getConnection();
                    PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    
                    pstmt.setString(1, gameName);
                    pstmt.setString(2, gamePath);
                    pstmt.setString(3, gameIconPath);
                    pstmt.setString(4, gameDescription);
                    
                    int affectedRows = pstmt.executeUpdate();
                    
                    if (affectedRows > 0) {
                        try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                            if (generatedKeys.next()) {
                                int generatedId = generatedKeys.getInt(1);
                                logArea.append("Added to database: " + gameName + " (ID: " + generatedId + ")\n");
                                processedCount++;
                            }
                        }
                    }
                }
            } catch (SQLException e) {
                logArea.append("Error processing " + file.getName() + ": " + e.getMessage() + "\n");
            }
        }
        logArea.append("Scan complete. Processed " + processedCount + " games.\n");
    }
            
    private void createGameMetadata(int gameId, String gameName, String gamePath, String imageUrl, String description) {
        try {
            // Use GameManager facade to create and save the game
            GameManager gameManager = GameManager.getInstance();
            
            // Get existing games, handle case where getAllGames() might be empty
            List<Game> games;
            try {
                games = new ArrayList<>(gameManager.getAllGames());
            } catch (Exception e) {
                // If we can't get games, start with an empty list
                games = new ArrayList<>();
                logArea.append("Warning: Could not load existing games, starting fresh.\n");
            }
            
            // Check if game with this ID already exists
            boolean gameExists = games.stream().anyMatch(g -> g.getID() == gameId);
            if (gameExists) {
                logArea.append("Game with ID " + gameId + " already exists. Skipping...\n");
                return;
            }
            
            // Create and add the new game
            Game newGame = gameManager.createGame(gameName, description, imageUrl, gameId, gamePath);
            if (newGame != null) {
                games.add(newGame);
                // Save all games
                gameManager.saveGames(games);
                logArea.append("Successfully added game: " + gameName + "\n");
            } else {
                logArea.append("Failed to create game: " + gameName + "\n");
            }
            
        } catch (Exception e) {
            logArea.append("Error creating game metadata: " + e.getMessage() + "\n");
            e.printStackTrace(); // Add this for more detailed error logging
        }
    }
    
    private void saveGamesToFile() {
        try {
            List<Game> games = new ArrayList<>();
            String sql = "SELECT * FROM gameData";

            try (Connection conn = database.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {

                GameManager gm = GameManager.getInstance();
                while (rs.next()) {
                    int id = rs.getInt("gameID");
                    String name = rs.getString("gameName");
                    String desc = rs.getString("gameDescription");
                    String url = rs.getString("gameURL");
                    String img = rs.getString("imageURL");

                    Game game = gm.createGame(name, desc != null ? desc : "", img != null ? img : "", id, url != null ? url : "");
                    games.add(game);
                }
            }

            // Convert to JSON array and write to file
            JSONArray jsonArray = new JSONArray();
            for (Game game : games) {
                JSONObject gameJson = new JSONObject();
                gameJson.put("gameID", game.getID());
                gameJson.put("gameName", game.getTitle());
                gameJson.put("gameURL", game.getGameUrl());
                gameJson.put("imageURL", game.getImageUrl());
                gameJson.put("gameDescription", game.getDescription());
                jsonArray.put(gameJson);
            }

            try (FileWriter file = new FileWriter("src/main/resources/store_games.json")) {
                file.write(jsonArray.toString(4));
                logArea.append("Successfully saved " + games.size() + " games to store_games.json\n");
            }

        } catch (SQLException | IOException e) {
            logArea.append("Error saving games to file: " + e.getMessage() + "\n");
            e.printStackTrace();
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

        try (Connection conn = database.getConnection()) {
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