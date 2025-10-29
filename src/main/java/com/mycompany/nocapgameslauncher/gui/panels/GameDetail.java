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

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setOpaque(false);
        buttonPanel.add(playButton);
        add(buttonPanel, BorderLayout.SOUTH);

        gameDescriptions = loadGameDescriptions();
    }

    public void setGame(String gameTitle) {
        setGame(gameTitle, null);
    }
    
    public void setGame(String gameTitle, String description) {
        gameTitleLabel.setText(gameTitle);
        if (description == null || description.trim().isEmpty()) {
            description = gameDescriptions.getOrDefault(gameTitle, "No description available for this game.");
        } gameDescriptionArea.setText(description);
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
        gameTitleLabel.setForeground(LightModeToggle.getTextColor());
        gameDescriptionArea.setForeground(LightModeToggle.getTextColor());
        gameDescriptionArea.setBackground(LightModeToggle.getBackgroundColor());
    }
}
