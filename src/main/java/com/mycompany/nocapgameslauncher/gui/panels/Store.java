package com.mycompany.nocapgameslauncher.gui.panels;

import com.mycompany.nocapgameslauncher.gui.mainFrame;
import com.mycompany.nocapgameslauncher.gui.components.GameCardCreator;
import com.mycompany.nocapgameslauncher.gui.resourceHandling.resourceLoader;
import com.mycompany.nocapgameslauncher.gui.utilities.FontManager;
import com.mycompany.nocapgameslauncher.gui.utilities.LightModeToggle;
import com.mycompany.nocapgameslauncher.gui.utilities.ThemePanel;

import static com.mycompany.nocapgameslauncher.gui.components.GameCardCreator.CARD_WIDTH;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.*;
import java.io.*;

import javax.swing.border.EmptyBorder;

import org.json.JSONArray;
import org.json.JSONObject;

import com.mycompany.nocapgameslauncher.gui.resourceHandling.NameFormatting;

public class Store extends ThemePanel {
    @SuppressWarnings("unused") private final mainFrame frame;
    private ThemePanel cardsPanel;
    private ArrayList<JPanel> gameCardsList;
    private JLabel titleLabel;
    private static final int CARD_GAP = 20;

    @SuppressWarnings("OverridableMethodCallInConstructor")
    public Store(mainFrame frame) {
        super(new BorderLayout());
        this.frame = frame;
        createContentView();
        updateTheme();
    }

    private void createContentView() {
        try {
            setBorder(new EmptyBorder(20, 20, 20, 20));
            titleLabel = new JLabel("Game Store");
            FontManager.setFont(titleLabel, Font.BOLD, 40);
            add(titleLabel, BorderLayout.NORTH);

            cardsPanel = new ThemePanel(new GridLayout(0, 3, CARD_GAP, CARD_GAP));
            cardsPanel.setBorder(new EmptyBorder(20, 0, 0, 0));
            gameCardsList = new ArrayList<>();
            
            // Load games using the existing utility method
            ArrayList<String> gameTitles = NameFormatting.getGameTitlesFromJson();
            
            if (gameTitles.isEmpty()) {
                JLabel errorLabel = new JLabel("No games found. Could not load store data.");
                errorLabel.setForeground(Color.RED);
                errorLabel.setHorizontalAlignment(SwingConstants.CENTER);
                cardsPanel.add(errorLabel);
            } else {
                System.out.println("Successfully loaded " + gameTitles.size() + " games");
                
                for (String title : gameTitles) {
                    String description = "No description available for this game";
                    String iconPath = "ImageResources/" + title.toLowerCase().replace(" ", "_") + ".jpg";
                    ImageIcon gameIcon = new ImageIcon(resourceLoader.RESOURCE_DIRECTORY + resourceLoader.PROXYIMAGE);
                    
                    JPanel card = GameCardCreator.createGameCard(
                        title,
                        description,
                        gameIcon,
                        () -> frame.showGameDetail(title)
                    );
                    
                    // Get the image label from the card
                    JLabel imageLabel = (JLabel) ((BorderLayout)card.getLayout()).getLayoutComponent(BorderLayout.CENTER);
                    
                    // Load actual image in background
                    new Thread(() -> {
                        try {
                            Thread.sleep(1200); // 1.2 seconds | Simulate loading
                            ImageIcon actualIcon = resourceLoader.loadIcon(iconPath);
                            if (actualIcon != null) {
                                SwingUtilities.invokeLater(() -> {
                                    // Update the image label with the actual icon
                                    Image scaled = actualIcon.getImage().getScaledInstance(180, 180, Image.SCALE_SMOOTH);
                                    imageLabel.setIcon(new ImageIcon(scaled));
                                });
                            }
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }, "ImageLoader-" + title).start();
                    
                    gameCardsList.add(card);
                    cardsPanel.add(card);
                }
            }

            JScrollPane scrollPane = new JScrollPane(cardsPanel);
            scrollPane.setBorder(BorderFactory.createEmptyBorder());
            scrollPane.getVerticalScrollBar().setUnitIncrement(16);
            scrollPane.setOpaque(false);
            scrollPane.getViewport().setOpaque(false);
            add(scrollPane, BorderLayout.CENTER);
            
        } catch (Exception e) {
            System.err.println("Error initializing Store panel: " + e.getMessage());
            e.printStackTrace();
            add(new JLabel("Error loading store content: " + e.getMessage()), BorderLayout.CENTER);
        }
    }
    
    private void updateGridColumns() {
        if (cardsPanel == null || gameCardsList.isEmpty()) {
            return;
        }
        
        int availableWidth = getWidth() - 40; // Subtract left and right padding
        if (availableWidth <= 0) {
            return;
        }
        
        // Calculate optimal number of columns based on available width
        int columns = Math.max(1, availableWidth / (CARD_WIDTH + CARD_GAP));
        
        // Update the grid layout if columns changed
        GridLayout layout = (GridLayout) cardsPanel.getLayout();
        if (layout.getColumns() != columns) {
            cardsPanel.setLayout(new GridLayout(0, columns, CARD_GAP, CARD_GAP));
            cardsPanel.revalidate();
            cardsPanel.repaint();
        }
    }

    private static class GameData {
        int gameID;
        String gameName;
        String imageURL;
        String gameURL;
        String gameDescription;
    }
    
    private ArrayList<GameData> loadGamesFromFile(String filename) {
        ArrayList<GameData> games = new ArrayList<>();
        try (InputStream is = getClass().getResourceAsStream(filename)) {
            if (is == null) {
                System.err.println("Could not find resource: " + filename);
                return games;
            }
            
            // Read the entire file content
            String content = new String(is.readAllBytes(), "UTF-8");
            
            // Parse the JSON array
            int startArray = content.indexOf('[');
            int endArray = content.lastIndexOf(']');
            
            if (startArray >= 0 && endArray > startArray) {
                String arrayContent = content.substring(startArray + 1, endArray);
                
                // Split by "gameID" to get individual game entries
                String[] entries = arrayContent.split("(?=\"gameID\")");
                
                for (String entry : entries) {
                    try {
                        GameData game = new GameData();
                        
                        // Extract gameID
                        int idStart = entry.indexOf("gameID") + 7; // "gameID":
                        int idEnd = entry.indexOf(',', idStart);
                        if (idEnd == -1) idEnd = entry.indexOf('}', idStart);
                        if (idStart > 6 && idEnd > idStart) {
                            String idStr = entry.substring(idStart, idEnd).trim();
                            if (idStr.endsWith("}")) {
                                idStr = idStr.substring(0, idStr.length() - 1);
                            }
                            game.gameID = Integer.parseInt(idStr);
                        }
                        
                        // Extract gameName
                        int nameStart = entry.indexOf("gameName\":\"") + 12;
                        int nameEnd = entry.indexOf("\"", nameStart);
                        if (nameStart > 11 && nameEnd > nameStart) {
                            game.gameName = entry.substring(nameStart, nameEnd);
                        }
                        
                        // Extract imageURL
                        int imgStart = entry.indexOf("imageURL\":\"") + 12;
                        int imgEnd = entry.indexOf("\"", imgStart);
                        if (imgStart > 11 && imgEnd > imgStart) {
                            game.imageURL = entry.substring(imgStart, imgEnd);
                            // Remove src/main/resources/ prefix if it exists
                            if (game.imageURL.startsWith("src/main/resources/")) {
                                game.imageURL = game.imageURL.substring("src/main/resources/".length());
                            }
                        }
                        
                        // Extract gameURL
                        int urlStart = entry.indexOf("gameURL\":\"") + 10;
                        int urlEnd = entry.indexOf("\"", urlStart);
                        if (urlStart > 9 && urlEnd > urlStart) {
                            game.gameURL = entry.substring(urlStart, urlEnd);
                        }
                        
                        // Extract gameDescription
                        int descStart = entry.indexOf("gameDescription\":\"") + 18;
                        int descEnd = entry.indexOf("\"", descStart);
                        if (descStart > 17 && descEnd > descStart) {
                            game.gameDescription = entry.substring(descStart, descEnd);
                        }
                        
                        if (game.gameName != null) {
                            games.add(game);
                        }
                    } catch (Exception e) {
                        System.err.println("Error parsing game entry: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading games from " + filename + ": " + e.getMessage());
            e.printStackTrace();
        }
        return games;
    }

    @Override
    public void updateTheme() {
        super.updateTheme();
        if (titleLabel != null) {
            titleLabel.setForeground(LightModeToggle.getTextColor());
        }
        if (cardsPanel != null) {
            cardsPanel.updateTheme();
        }
    }
}
