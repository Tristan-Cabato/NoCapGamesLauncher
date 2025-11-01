package com.mycompany.nocapgameslauncher.gui.panels;

import com.mycompany.nocapgameslauncher.gui.mainFrame;
import com.mycompany.nocapgameslauncher.gui.resourceHandling.resourceLoader;
import com.mycompany.nocapgameslauncher.gui.utilities.*;

import javax.swing.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.awt.AlphaComposite;
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
    private final JLabel gameImageLabel;
    private boolean userOwned;
    private static final ImageIcon DEFAULT_GAME_ICON;

    private final Map<String, String> gameDescriptions;
    private int currentGameId = -1;

    // Initialize static default icon
    static {
        ImageIcon icon = resourceLoader.loadIcon("ImageResources/default_game_icon.jpg");
        if (icon != null) {
            Image img = icon.getImage();
            DEFAULT_GAME_ICON = new ImageIcon(img.getScaledInstance(400, 400, Image.SCALE_SMOOTH));
        } else {
            // Create a simple placeholder if default icon is not found
            BufferedImage img = new BufferedImage(400, 400, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = img.createGraphics();
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.fillRect(0, 0, 400, 400);
            g2d.setColor(Color.DARK_GRAY);
            g2d.setFont(new Font("Arial", Font.BOLD, 24));
            g2d.drawString("No Image", 150, 200);
            g2d.dispose();
            DEFAULT_GAME_ICON = new ImageIcon(img);
        }
    }
    
    public GameDetail(mainFrame frame) {
        super(new BorderLayout(30, 30));
        this.frame = frame;
        setBorder(new EmptyBorder(30, 30, 30, 30));

        // Create main content panel with horizontal layout
        JPanel contentPanel = new JPanel(new BorderLayout(30, 0));
        contentPanel.setOpaque(false);

        // Left side - Game Image
        gameImageLabel = new JLabel();
        gameImageLabel.setPreferredSize(new Dimension(400, 400));
        gameImageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gameImageLabel.setVerticalAlignment(SwingConstants.CENTER);
        gameImageLabel.setBackground(new Color(0, 0, 0, 0)); // Transparent background
        gameImageLabel.setOpaque(false);
        
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
        System.out.println("=== Starting checkIfOwned() ===");
        System.out.println("Current game ID: " + currentGameId);
        
        try {
            String currentUser = com.mycompany.nocapgameslauncher.database.DatabaseHandler.getCurrentUser();
            System.out.println("Current user: " + currentUser);
            
            if (currentUser == null || currentUser.trim().isEmpty()) {
                System.out.println("No current user found, cannot check ownership");
                userOwned = false;
                return;
            }
            
            String userJsonPath = resourceLoader.RESOURCE_DIRECTORY + "Users/" + currentUser + ".json";
            System.out.println("Looking for user file at: " + userJsonPath);
            
            File userFile = new File(userJsonPath);
            
            if (!userFile.exists()) {
                System.out.println("User JSON file not found: " + userJsonPath);
                userOwned = false;
                return;
            }
            
            try (java.io.Reader reader = new java.io.FileReader(userFile)) {
                com.google.gson.Gson gson = new com.google.gson.Gson();
                java.util.Map<String, Object> userData = gson.fromJson(reader, java.util.Map.class);
                
                if (userData == null) {
                    System.out.println("Failed to parse user data from JSON");
                    userOwned = false;
                    return;
                }
                
                // Get owned game IDs
                @SuppressWarnings("unchecked")
                java.util.List<Double> ownedGameIds = (java.util.List<Double>) userData.get("ownedGameIds");
                
                // Check if current game ID is in the owned games list
                if (ownedGameIds != null) {
                    System.out.println("User's owned game IDs: " + ownedGameIds);
                    userOwned = ownedGameIds.stream()
                            .anyMatch(id -> {
                                boolean match = (id != null && id.intValue() == currentGameId);
                                System.out.println("Checking if " + id + " == " + currentGameId + ": " + match);
                                return match;
                            });
                    System.out.println("Final ownership for game " + currentGameId + ": " + userOwned);
                } else {
                    System.out.println("No owned games found for user");
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
        
        // Show loading icon first
        gameImageLabel.setIcon(new ImageIcon(resourceLoader.RESOURCE_DIRECTORY + resourceLoader.PROXYIMAGE));
        
        // Load game image in background
        new Thread(() -> {
            try {
                // Simulate loading time (similar to Store and Library)
                Thread.sleep(600);
                
                String imagePath = "ImageResources/" + gameTitle.toLowerCase().replace(" ", "_") + ".jpg";
                ImageIcon actualIcon = resourceLoader.loadIcon(imagePath);
                
                if (actualIcon != null) {
                    // Scale the image to fit the label
                    Image scaled = actualIcon.getImage().getScaledInstance(
                        gameImageLabel.getWidth(), 
                        gameImageLabel.getHeight(), 
                        Image.SCALE_SMOOTH
                    );
                    
                    // Update the UI on the Event Dispatch Thread
                    SwingUtilities.invokeLater(() -> {
                        gameImageLabel.setIcon(new ImageIcon(scaled));
                        gameImageLabel.repaint();
                    });
                } else {
                    // If no image found, use the default icon
                    SwingUtilities.invokeLater(() -> {
                        gameImageLabel.setIcon(DEFAULT_GAME_ICON);
                    });
                }
            } catch (Exception e) {
                System.err.println("Error loading game image: " + e.getMessage());
                try {
                    Thread.sleep(100); // Simulate loading time even on error
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
                SwingUtilities.invokeLater(() -> {
                    gameImageLabel.setIcon(DEFAULT_GAME_ICON);
                });
            }
        }, "GameDetail-ImageLoader-" + gameTitle).start();
    }
    
    public void setGame(String gameTitle, Integer gameId) {
        if (gameTitle == null) return;
        
        // Store the game ID for ownership check
        int newGameId = (gameId != null) ? gameId : -1;
        boolean gameChanged = (this.currentGameId != newGameId);
        this.currentGameId = newGameId;
        
        System.out.println("Setting game: " + gameTitle + " with ID: " + this.currentGameId);
        
        // Update the game details first
        setGame(gameTitle, (String)null);
        
        // Then check ownership if needed
        if (gameChanged || playButton == null) {
            SwingUtilities.invokeLater(() -> {
                checkIfOwned();
                // Ensure the button text is updated
                if (playButton != null) {
                    playButton.setText(userOwned ? "Play Game" : "Add to Library");
                    playButton.revalidate();
                    playButton.repaint();
                }
            });
        }
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
