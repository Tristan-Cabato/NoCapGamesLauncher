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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

        int newGamesAdded = 0;
        int existingGamesSkipped = 0;
        
        try (Connection conn = database.getConnection()) {
            // Create a set of existing game names for quick lookup
            Set<String> existingGames = new HashSet<>();
            try (Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT LOWER(gameName) as gameName FROM gameData")) {
                while (rs.next()) {
                    existingGames.add(rs.getString("gameName"));
                }
            }

            // Process each file
            for (File file : files) {
                String fileName = file.getName();
                String baseName = fileName.substring(0, fileName.lastIndexOf('.'));
                String gameName = baseName.replace(" ", "_").toLowerCase();
                
                // Skip if game already exists
                if (existingGames.contains(gameName)) {
                    logArea.append("Skipped (exists): " + gameName + "\n");
                    existingGamesSkipped++;
                    continue;
                }

                // Insert new game
                String gamePath = file.getAbsolutePath();
                String gameIconPath = "src/main/resources/ImageResources/" + baseName.toLowerCase() + ".jpg";
                String gameDescription = "This is a description";

                String sql = "INSERT INTO gameData (gameName, gameURL, imageURL, gameDescription) VALUES (?, ?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    pstmt.setString(1, gameName);
                    pstmt.setString(2, gamePath);
                    pstmt.setString(3, gameIconPath);
                    pstmt.setString(4, gameDescription);
                    
                    int affectedRows = pstmt.executeUpdate();
                    if (affectedRows > 0) {
                        try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                            if (generatedKeys.next()) {
                                int generatedId = generatedKeys.getInt(1);
                                logArea.append("Added new game: " + gameName + " (ID: " + generatedId + ")\n");
                                newGamesAdded++;
                            }
                        }
                    }
                } catch (SQLException e) {
                    logArea.append("Error adding " + gameName + ": " + e.getMessage() + "\n");
                }
            }
        } catch (SQLException e) {
            logArea.append("Database error: " + e.getMessage() + "\n");
            return;
        }
        
        logArea.append(String.format("Scan complete. Added %d new games, skipped %d existing games.\n", 
            newGamesAdded, existingGamesSkipped));
    }
    
    private void saveGamesToFile() {
        try (Connection conn = database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM gameData")) {
            
            List<Game> games = new ArrayList<>();
            GameManager gm = GameManager.getInstance();
            
            while (rs.next()) {
                int id = rs.getInt("gameID");
                String name = rs.getString("gameName");
                String desc = rs.getString("gameDescription");
                String url = rs.getString("gameURL");
                String img = rs.getString("imageURL");
                
                Game game = gm.createGame(
                    name != null ? name : "", 
                    desc != null ? desc : "", 
                    img != null ? img : "", 
                    id, 
                    url != null ? url : ""
                );
                games.add(game);
            }
            
            // Save the games to the JSON file
            gm.saveGames(games);
            logArea.append("Successfully saved " + games.size() + " games from database to store_games.json\n");
            
        } catch (SQLException e) {
            logArea.append("Error saving games: " + e.getMessage() + "\n");
            e.printStackTrace();
        }
    }

    private void addMegaQueryButton(JPanel buttonPanel) {
        JButton megaQueryButton = new JButton("Run MegaQuery");
        megaQueryButton.setBackground(new Color(220, 53, 69));
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