package com.mycompany.nocapgameslauncher.gui.panels;

import com.mycompany.nocapgameslauncher.gui.mainFrame;
import com.mycompany.nocapgameslauncher.resourceHandling.resourceLoader;
import com.mycompany.nocapgameslauncher.userManager.UserGameData;
import com.mycompany.nocapgameslauncher.userManager.UserGameManager;
import com.mycompany.nocapgameslauncher.gui.utilities.*;
import com.mycompany.nocapgameslauncher.gui.components.sidebarCreator;
import com.mycompany.nocapgameslauncher.game_manager.GameManager;
import com.mycompany.nocapgameslauncher.database.DatabaseHandler;
import com.mycompany.nocapgameslauncher.game_manager.Game;

import javax.swing.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.io.*;
import java.text.SimpleDateFormat;

import javax.swing.border.EmptyBorder;
import org.json.*;

public class GameDetail extends ThemePanel {
    private final mainFrame frame;
    private final JLabel gameTitleLabel;
    private final JTextArea gameDescriptionArea;
    private final JScrollPane descriptionScrollPane;
    private final ThemeButton playButton;
    private final ThemeButton removeButton;
    private final ThemeButton statsButton;
    private final JLabel gameImageLabel;
    private boolean userOwned;
    private static final ImageIcon DEFAULT_GAME_ICON;

    private Game currentGame;
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

        playButton = new ThemeButton("Play Games", false, false, null, true);

        FontManager.setFont(playButton, Font.BOLD, 20);
        playButton.setPreferredSize(new Dimension(200, 50));
        playButton.setBackground(LightModeToggle.GREEN);
        playButton.setForeground(Color.WHITE);
        playButton.setOpaque(true);
        playButton.setBorderPainted(false);
        
        buttonPanel.add(playButton);

        // Stats
        statsButton = new ThemeButton("View Stats", false, false, null, true);
        FontManager.setFont(statsButton, Font.BOLD, 16);
        statsButton.addActionListener(_ -> showGameStats());
        buttonPanel.add(statsButton);

        // Remove button
        removeButton = new ThemeButton("Remove from Library", false, false, null, true);
        FontManager.setFont(removeButton, Font.BOLD, 16);
        removeButton.setBackground(Color.RED);
        removeButton.setForeground(Color.WHITE);
        removeButton.setOpaque(true);
        removeButton.setBorderPainted(false);
        removeButton.addActionListener(_ -> removeFromLibrary());
        removeButton.setVisible(false); // Initially hidden
        buttonPanel.add(removeButton);
        
        // Add everything to main panel
        add(contentPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // Single dynamic action listener that checks ownership state when clicked
        playButton.addActionListener(_ -> {
            if (userOwned) {
                launchGame();
            } else {
                addToLibrary();
            }
        });

        gameDescriptions = loadGameDescriptions();
        updateButtonStates();
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
        if (gameTitle == null || gameId == null) return;
        
        // Get the game instance from the manager
        this.currentGame = GameManager.getInstance().getGameById(gameId);
        if (this.currentGame == null) return;
        
        // Update the game ID
        this.currentGameId = currentGame.getID();
        
        // Get the game description
        String description = null;
        Map<String, String> gameDetails = resourceLoader.getGameById(gameId);
        if (gameDetails != null) {
            description = gameDetails.get("gameDescription");
        }
        
        // Update the UI
        setGame(gameTitle, description);
        
        // Check ownership
        checkIfOwned();
    }

    private Map<String, String> loadGameDescriptions() {
        Map<String, String> descriptions = new HashMap<>();
        try (InputStream inputStream = getClass().getResourceAsStream("/data/games.json")) {
            if (inputStream != null) {
                return GameManager.getInstance().getGameDescriptions(inputStream);
            }
        } catch (IOException e) {
            System.err.println("Error loading game descriptions: " + e.getMessage());
        }
        return descriptions;
    }

    private void addToLibrary() {
        try {
            String currentUser = DatabaseHandler.getCurrentUser();
            if (currentUser == null) {
                JOptionPane.showMessageDialog(this, "User not logged in", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Add the game to the user's library
            UserGameData userGameData = UserGameData.loadForUser(currentUser);
            userGameData.addGame(currentGameId);

            userOwned = true;
            updateButtonStates();
            
            // Request sidebar refresh in the main frame
            if (frame != null) {
                frame.refreshSidebar();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                this,
                "Failed to add game to library: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void launchGame() {
        if (currentGame == null) return;
        
        // Update play stats
        currentGame.incrementPlayCount();
        
        // Run game launch in a separate thread
        new Thread(() -> {
            try {
                // Get the game details from JSON to retrieve the gameURL
                Map<String, String> gameDetails = resourceLoader.getGameById(currentGameId);
                
                if (gameDetails == null) {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(
                            this, 
                            "Game details not found", 
                            "Error", 
                            JOptionPane.ERROR_MESSAGE
                        );
                    });
                    return;
                }
                
                String gameUrl = gameDetails.get("gameURL");
                if (gameUrl == null || gameUrl.isEmpty()) {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(
                            this, 
                            "Game executable path not found", 
                            "Error", 
                            JOptionPane.ERROR_MESSAGE
                        );
                    });
                    return;
                }
                
                File file = new File(gameUrl);
                if (file.exists()) {
                    // Launch the game
                    Desktop.getDesktop().open(file);
                    
                    // Show success message
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane optionPane = new JOptionPane(
                            "Launching " + currentGame.getTitle() + "...", 
                            JOptionPane.INFORMATION_MESSAGE
                        );
                        JDialog dialog = optionPane.createDialog("Game Launched");
                        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                        dialog.setModal(false);
                        dialog.setVisible(true);

                        // Auto-close after 0.8 seconds
                        new javax.swing.Timer(800, e -> {
                            dialog.dispose();
                            ((javax.swing.Timer)e.getSource()).stop();
                        }).start();
                    });
                } else {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(
                            this, 
                            "Executable not found at: " + gameUrl, 
                            "Error", 
                            JOptionPane.ERROR_MESSAGE
                        );
                    });
                }
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(
                        this, 
                        "Error launching game: " + e.getMessage(), 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE
                    );
                });
            }
        }).start();
    }

    private void removeFromLibrary() {
        try {
            UserGameData userGameData = UserGameData.loadForUser(
                com.mycompany.nocapgameslauncher.database.DatabaseHandler.getCurrentUser()
            );
            userGameData.removeGame(currentGameId);
            userOwned = false;
            updateButtonStates();
            
            // Request sidebar refresh in the main frame
            if (frame != null) {
                frame.refreshSidebar();
            }
            
            // Force a UI update on the Event Dispatch Thread
            SwingUtilities.invokeLater(() -> {
                // Force a repaint of the entire frame
                if (getTopLevelAncestor() != null) {
                    getTopLevelAncestor().revalidate();
                    getTopLevelAncestor().repaint();
                }
            });
        } catch (Exception e) {
            System.err.println("Error removing game from library: " + e.getMessage());
            JOptionPane.showMessageDialog(
                this,
                "Failed to remove game from library: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void checkIfOwned() {
        try {
            UserGameData userGameData = UserGameData.loadForUser(
                com.mycompany.nocapgameslauncher.database.DatabaseHandler.getCurrentUser()
            );
            userOwned = userGameData.ownsGame(currentGameId);
        } catch (Exception e) {
            System.err.println("Error checking game ownership: " + e.getMessage());
            userOwned = false;
        }
        updateButtonStates();
    }

    private void updateButtonStates() {
        if (playButton != null) {
            playButton.setText(userOwned ? "Play Game" : "Add to Library");
        }
        if (removeButton != null) {
            removeButton.setVisible(userOwned);
        }
        if (statsButton != null) {
            statsButton.setVisible(userOwned);
        }
    }

    // In GameDetail.java, update the showGameStats method
    private void showGameStats() {
        if (currentGame == null) {
            JOptionPane.showMessageDialog(
                this,
                "No game selected",
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
            return;
        }
        
        // Format the last played time
        String lastPlayed = "Never";
        if (currentGame.getLastPlayed() > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            lastPlayed = sdf.format(new Date(currentGame.getLastPlayed()));
        }
        
        // Format the stats message
        String stats = String.format(
            "<html><body style='width: 300px;'>" +
            "<h2>Game Statistics</h2>" +
            "<p><b>Title:</b> %s</p>" +
            "<p><b>Times Played:</b> %d</p>" +
            "<p><b>Last Played:</b> %s</p>" +
            "</body></html>",
            currentGame.getTitle(),
            currentGame.getPlayCount(),
            lastPlayed
        );
        
        // Show the stats dialog
        JOptionPane.showMessageDialog(
            this,
            stats,
            "Game Statistics - " + currentGame.getTitle(),
            JOptionPane.INFORMATION_MESSAGE
        );
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
