package com.mycompany.nocapgameslauncher.gui.panels;

import com.mycompany.nocapgameslauncher.gui.mainFrame;
import com.mycompany.nocapgameslauncher.gui.utilities.*;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.io.*;
import javax.swing.border.EmptyBorder;

public class GameDetail extends ThemePanel {

    private final mainFrame frame;
    private final JLabel gameTitleLabel;
    private final JTextArea gameDescriptionArea;
    private final ThemeButton playButton;

    private final Map<String, String> gameDescriptions;

    public GameDetail(mainFrame frame) {
        super(new BorderLayout(20, 20));
        this.frame = frame;
        setBorder(new EmptyBorder(20, 20, 20, 20));

        gameTitleLabel = new JLabel("Game Title");
        FontManager.setFont(gameTitleLabel, Font.BOLD, 36);
        gameTitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(gameTitleLabel, BorderLayout.NORTH);

        gameDescriptionArea = new JTextArea("Game Description");
        FontManager.setFont(gameDescriptionArea, Font.PLAIN, 18);
        gameDescriptionArea.setLineWrap(true);
        gameDescriptionArea.setWrapStyleWord(true);
        gameDescriptionArea.setEditable(false);
        gameDescriptionArea.setOpaque(true); // Make it opaque
        JScrollPane scrollPane = new JScrollPane(gameDescriptionArea);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        add(scrollPane, BorderLayout.CENTER);

        playButton = new ThemeButton("Play Game", false, false, null, true);
        FontManager.setFont(playButton, Font.BOLD, 24);
        playButton.setPreferredSize(new Dimension(200, 50));
        playButton.setBackground(LightModeToggle.GREEN); // Steam Green
        playButton.setForeground(Color.WHITE); // White text for contrast
        playButton.setOpaque(true); // Ensure background is painted
        playButton.setBorderPainted(false); // Remove border
        playButton.addActionListener(_ -> {
            JOptionPane.showMessageDialog(this, "Launching " + gameTitleLabel.getText() + "...");
        });
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setOpaque(false);
        buttonPanel.add(playButton);
        add(buttonPanel, BorderLayout.SOUTH);

        gameDescriptions = loadGameDescriptions();
    }

    public void setGame(String gameTitle) {
        gameTitleLabel.setText(gameTitle);
        String description = gameDescriptions.getOrDefault(gameTitle, "No description available for this game.");
        gameDescriptionArea.setText(description);
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
            
            // Simple JSON parsing
            String content = jsonContent.toString();
            int startIndex = content.indexOf('[') + 1;
            int endIndex = content.lastIndexOf(']');
            
            if (startIndex > 0 && endIndex > startIndex) {
                String gamesArray = content.substring(startIndex, endIndex);
                // Split by "gameName" to find each game entry
                String[] gameEntries = gamesArray.split("\\{\\\"gameName\\\"\\s*:");

                for (int i = 1; i < gameEntries.length; i++) {
                    try {
                        String gameName = null;
                        String gameDescription = "No description available.";
                        
                        // Extract game name
                        int nameStart = gameEntries[i].indexOf('"') + 1;
                        int nameEnd = gameEntries[i].indexOf('"', nameStart);
                        if (nameStart > 0 && nameEnd > nameStart) {
                            gameName = gameEntries[i].substring(nameStart, nameEnd);
                        }
                        
                        // Extract game description
                        int descStart = gameEntries[i].indexOf("gameDescription\":") + 17;
                        int descEnd = gameEntries[i].indexOf('"', descStart);
                        if (descStart > 16 && descEnd > descStart) {
                            gameDescription = gameEntries[i].substring(descStart, descEnd);
                        }
                        
                        if (gameName != null) {
                            descriptions.put(gameName, gameDescription);
                        }
                    } catch (Exception e) {
                        System.err.println("Error parsing game entry: " + e.getMessage());
                    }
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
        gameTitleLabel.setForeground(LightModeToggle.getTextColor());
        gameDescriptionArea.setForeground(LightModeToggle.getTextColor());
        gameDescriptionArea.setBackground(LightModeToggle.getBackgroundColor());
    }
}
