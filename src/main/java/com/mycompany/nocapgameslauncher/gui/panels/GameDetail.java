package com.mycompany.nocapgameslauncher.gui.panels;

import com.mycompany.nocapgameslauncher.gui.mainFrame;
import com.mycompany.nocapgameslauncher.resourceHandling.resourceLoader;
import com.mycompany.nocapgameslauncher.userManager.UserGameData;
import com.mycompany.nocapgameslauncher.userManager.UserGameManager;
import com.mycompany.nocapgameslauncher.gui.utilities.*;
import com.mycompany.nocapgameslauncher.gui.components.sidebarCreator;
import com.mycompany.nocapgameslauncher.game_manager.GameManager;

import javax.swing.*;

import java.awt.*;
import java.awt.image.BufferedImage;
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
    private final ThemeButton removeButton;
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

        playButton = new ThemeButton("Play Games", false, false, null, true);

        FontManager.setFont(playButton, Font.BOLD, 20);
        playButton.setPreferredSize(new Dimension(200, 50));
        playButton.setBackground(LightModeToggle.GREEN);
        playButton.setForeground(Color.WHITE);
        playButton.setOpaque(true);
        playButton.setBorderPainted(false);
        
        buttonPanel.add(playButton);

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
        if (gameTitle == null) return;
        
        // Store the game ID for ownership check
        int newGameId = (gameId != null) ? gameId : -1;
        boolean gameChanged = (this.currentGameId != newGameId);
        this.currentGameId = newGameId;
        
        System.out.println("Setting game: " + gameTitle + " with ID: " + this.currentGameId);
        
        // Try to get description from game data if we have an ID
        String description = null;
        if (this.currentGameId > 0) {
            Map<String, String> gameDetails = resourceLoader.getGameById(this.currentGameId);
            if (gameDetails != null) {
                description = gameDetails.get("gameDescription");
                System.out.println("Found game details: " + gameDetails);
            }
        }
        
        // Update the game details with the found description or null
        setGame(gameTitle, description);
        
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
            UserGameData userGameData = UserGameData.loadForUser(
                com.mycompany.nocapgameslauncher.database.DatabaseHandler.getCurrentUser()
            );
            userGameData.addGame(currentGameId);
            userOwned = true;
            updateButtonStates();
        } catch (Exception e) {
            System.err.println("Error adding game to library: " + e.getMessage());
            JOptionPane.showMessageDialog(
                this,
                "Failed to add game to library: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void launchGame() {
        try {
            // Get the game details from JSON to retrieve the gameURL
            Map<String, String> gameDetails = resourceLoader.getGameById(currentGameId);
            
            if (gameDetails == null) {
                JOptionPane.showMessageDialog(this, 
                    "Game details not found", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            String gameUrl = gameDetails.get("gameURL");
            if (gameUrl == null || gameUrl.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "Game executable path not found", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            File file = new File(gameUrl);
            if (file.exists()) {
                Desktop.getDesktop().open(file);
                
                JOptionPane optionPane = new JOptionPane(
                    "Launching " + gameTitleLabel.getText() + "...", 
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
                    "Executable not found at: " + gameUrl, 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, 
                "Error launching game: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void removeFromLibrary() {
        try {
            UserGameData userGameData = UserGameData.loadForUser(
                com.mycompany.nocapgameslauncher.database.DatabaseHandler.getCurrentUser()
            );
            userGameData.removeGame(currentGameId);
            userOwned = false;
            updateButtonStates();
            
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
        if (playButton != null) playButton.setText(userOwned ? "Play Game" : "Add to Library");
        if (removeButton != null) {
            if (userOwned) removeButton.setVisible(true);
            else removeButton.setVisible(false);
        }
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
