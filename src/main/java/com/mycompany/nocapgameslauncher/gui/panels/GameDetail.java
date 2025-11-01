package com.mycompany.nocapgameslauncher.gui.panels;

import com.mycompany.nocapgameslauncher.gui.mainFrame;
import com.mycompany.nocapgameslauncher.gui.resourceHandling.resourceLoader;
import com.mycompany.nocapgameslauncher.gui.utilities.*;

import javax.swing.*;
import javax.swing.Timer;

import java.awt.*;
import java.util.*;
import java.io.*;
import javax.swing.border.EmptyBorder;
import org.json.*;

public class GameDetail extends ThemePanel {

    private final mainFrame frame;
    private final JLabel gameTitleLabel;
    private final JTextArea gameDescriptionArea;
    private final JScrollPane descriptionScrollPane;
    private final ThemeButton playButton;
    private boolean userOwned;

    private final Map<String, String> gameDescriptions;
    private int currentGameId = -1;

    public GameDetail(mainFrame frame) {
        super(new BorderLayout(30, 30));
        this.frame = frame;
        setBorder(new EmptyBorder(30, 30, 30, 30));
        checkIfOwned();

        // Create main content panel with horizontal layout
        JPanel contentPanel = new JPanel(new BorderLayout(30, 0));
        contentPanel.setOpaque(false);

        // Left side - Game Image
        JLabel gameImageLabel = new JLabel();
        gameImageLabel.setPreferredSize(new Dimension(400, 400));
        gameImageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gameImageLabel.setVerticalAlignment(SwingConstants.CENTER);
        gameImageLabel.setBackground(Color.DARK_GRAY);
        gameImageLabel.setOpaque(true);
        
        // Right side - Game Info
        JPanel infoPanel = new JPanel(new BorderLayout(10, 20));
        infoPanel.setOpaque(false);
        
        // Game Title
        gameTitleLabel = new JLabel("Game Title");
        FontManager.setFont(gameTitleLabel, Font.BOLD, 36);
        gameTitleLabel.setHorizontalAlignment(SwingConstants.LEFT);
        
        // Game Description
        gameDescriptionArea = new JTextArea("Game Description");
        FontManager.setFont(gameDescriptionArea, Font.PLAIN, 16);
        gameDescriptionArea.setLineWrap(true);
        gameDescriptionArea.setWrapStyleWord(true);
        gameDescriptionArea.setEditable(false);
        gameDescriptionArea.setMargin(new Insets(0, 0, 20, 0));
        
        // Create scroll pane for description
        descriptionScrollPane = new JScrollPane(gameDescriptionArea);
        descriptionScrollPane.setBorder(BorderFactory.createEmptyBorder());
        descriptionScrollPane.getViewport().setOpaque(false);
        descriptionScrollPane.setOpaque(false);
        
        // Add components to info panel
        infoPanel.add(gameTitleLabel, BorderLayout.NORTH);
        infoPanel.add(descriptionScrollPane, BorderLayout.CENTER);
        
        // Add image and info to content panel
        contentPanel.add(gameImageLabel, BorderLayout.WEST);
        contentPanel.add(infoPanel, BorderLayout.CENTER);
        
        // Button panel at the bottom
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        buttonPanel.setOpaque(false);
        
        if (userOwned) {
            playButton = new ThemeButton("Play Game", false, false, null, true);
        } else {
            playButton = new ThemeButton("Add to Library", false, false, null, true);
        }

        FontManager.setFont(playButton, Font.BOLD, 20);
        playButton.setPreferredSize(new Dimension(200, 50));
        playButton.setBackground(LightModeToggle.GREEN);
        playButton.setForeground(Color.WHITE);
        playButton.setOpaque(true);
        playButton.setBorderPainted(false);
        
        buttonPanel.add(playButton);
        
        // Add everything to main panel
        add(contentPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        playButton.addActionListener(_ -> {
            String gameTitle = gameTitleLabel.getText();
            String gamePath = resourceLoader.RESOURCE_DIRECTORY + "Executables/" + 
                            gameTitle + ".lnk";
            
            try {
                File file = new File(gamePath);
                if (file.exists()) {
                    Desktop.getDesktop().open(file);
                    JOptionPane optionPane = new JOptionPane(
                    "Launching " + gameTitle + "...", 
                    JOptionPane.INFORMATION_MESSAGE);
                    JDialog dialog = optionPane.createDialog("Game Launched");
                    dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                    dialog.setModal(false);
                    dialog.setVisible(true);

                    javax.swing.Timer timer = new javax.swing.Timer(2000, e -> {
                        dialog.dispose();
                    });
                    timer.setRepeats(false);
                    timer.start();
                } else {
                    JOptionPane.showMessageDialog(this, 
                        "Executable not found at: " + gamePath, 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error launching game: " + e.getMessage());
            }
        });

        gameDescriptions = loadGameDescriptions();
    }

    private void checkIfOwned() {
        if (currentGameId == -1) {
            userOwned = false;
            return;
        }
        
        try {
            // Get current user
            String currentUser = com.mycompany.nocapgameslauncher.database.DatabaseHandler.getCurrentUser();
            if (currentUser == null || currentUser.isEmpty()) {
                userOwned = false;
                return;
            }
            
            // Load user's JSON file
            String userJsonPath = resourceLoader.RESOURCE_DIRECTORY + "Users/" + currentUser + ".json";
            try (java.io.Reader reader = new java.io.FileReader(userJsonPath)) {
                com.google.gson.Gson gson = new com.google.gson.Gson();
                java.util.Map<String, Object> userData = gson.fromJson(reader, java.util.Map.class);
                
                // Get owned game IDs
                @SuppressWarnings("unchecked")
                java.util.List<Double> ownedGameIds = (java.util.List<Double>) userData.get("ownedGameIds");
                
                // Check if current game ID is in the owned games list
                if (ownedGameIds != null) {
                    userOwned = ownedGameIds.stream()
                            .anyMatch(id -> id.intValue() == currentGameId);
                } else {
                    userOwned = false;
                }
            }
        } catch (Exception e) {
            System.err.println("Error checking if game is owned: " + e.getMessage());
            userOwned = false;
        }
        
        // Update button text based on ownership
        if (playButton != null) {
            playButton.setText(userOwned ? "Play Game" : "Add to Library");
        }
    }
    
    public void setGame(String gameTitle) {
        setGame(gameTitle, (String)null);
    }
    
    public void setGame(String gameTitle, String description) {
        gameTitleLabel.setText(gameTitle);
        if (description == null || description.trim().isEmpty()) {
            description = gameDescriptions.getOrDefault(gameTitle, "No description available for this game.");
        }
        gameDescriptionArea.setText(description);
        
        // Load game image if available
        try {
            String imagePath = "ImageResources/" + gameTitle.toLowerCase().replace(" ", "_") + ".jpg";
            ImageIcon icon = resourceLoader.loadIcon(imagePath);
            if (icon != null) {
                Image img = icon.getImage();
                Image scaledImg = img.getScaledInstance(400, 400, Image.SCALE_SMOOTH);
                JLabel gameImageLabel = (JLabel) ((JPanel) getComponent(0)).getComponent(0);
                gameImageLabel.setIcon(new ImageIcon(scaledImg));
            }
        } catch (Exception e) {
            System.err.println("Error loading game image: " + e.getMessage());
        }
    }
    
    public void setGame(String gameTitle, Integer gameId) {
        // Store the game ID for ownership check
        if (gameId != null) {
            this.currentGameId = gameId;
            checkIfOwned();
        }
        setGame(gameTitle, (String)null);
    }

    private Map<String, String> loadGameDescriptions() {
        Map<String, String> descriptions = new HashMap<>();
        
        try (InputStream is = getClass().getResourceAsStream("/store_games.json");
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
            
            // Read the entire file content
            StringBuilder jsonContent = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonContent.append(line.trim());
            }
            
            // Parse JSON using JSONObject
            JSONArray gamesArray = new JSONArray(jsonContent.toString());
            
            for (int i = 0; i < gamesArray.length(); i++) {
                try {
                    JSONObject game = gamesArray.getJSONObject(i);
                    String gameName = game.optString("gameName");
                    String gameDescription = game.optString("gameDescription", "No description available.");
                    
                    if (gameName != null && !gameName.isEmpty()) {
                        descriptions.put(gameName, gameDescription);
                    }
                } catch (JSONException e) {
                    System.err.println("Error parsing game entry at index " + i + ": " + e.getMessage());
                }
            }
            
        } catch (IOException e) {
            System.err.println("Error reading game data: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error parsing game data: " + e.getMessage());
        }
        return descriptions;
    }

    @Override
    public void updateTheme() {
        super.updateTheme();
        
        // Update component colors based on theme
        Color bgColor = LightModeToggle.getComponentColor();
        Color textColor = LightModeToggle.getTextColor();
        
        // Update description area
        if (gameDescriptionArea != null) {
            gameDescriptionArea.setForeground(textColor);
            gameDescriptionArea.setBackground(bgColor);
            gameDescriptionArea.setOpaque(true);
        }
        
        // Update scroll pane
        if (descriptionScrollPane != null) {
            descriptionScrollPane.getViewport().setBackground(bgColor);
            descriptionScrollPane.setBackground(bgColor);
        }
        
        // Update title label
        if (gameTitleLabel != null) {
            gameTitleLabel.setForeground(textColor);
        }
        
        // Force repaint to apply changes
        revalidate();
        repaint();
    }
}
