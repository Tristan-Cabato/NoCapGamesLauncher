package com.mycompany.nocapgameslauncher.gui.components;

import com.mycompany.nocapgameslauncher.database.DatabaseHandler;
import com.mycompany.nocapgameslauncher.gui.mainFrame;
import com.mycompany.nocapgameslauncher.gui.resourceHandling.NameFormatting;
import com.mycompany.nocapgameslauncher.gui.resourceHandling.resourceLoader;
import com.mycompany.nocapgameslauncher.gui.utilities.*;

import java.awt.*;
import java.awt.event.*;
import java.util.function.*;
import javax.swing.*;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

public class sidebarCreator {
            
    public static JPanel createNavigationSidebar(int width, mainFrame frame, Consumer<String> onItemClick) {
        ThemePanel panel = new ThemePanel() {
            @Override
            public void updateTheme() {
                setBackground(LightModeToggle.getSidebarColor());
            }
        };
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setPreferredSize(new Dimension(width, Integer.MAX_VALUE));
        panel.setMaximumSize(new Dimension(width, Integer.MAX_VALUE));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 1));
        

        addItem(panel, "Owned Games", "📦", onItemClick);
        
        // Add owned games list
        ThemePanel ownedGamesPanel = new ThemePanel() {
            @Override
            public void updateTheme() {
                setBackground(LightModeToggle.getSidebarColor());
                setForeground(LightModeToggle.getTextColor());
            }
        };
        ownedGamesPanel.setLayout(new BoxLayout(ownedGamesPanel, BoxLayout.Y_AXIS));
        ownedGamesPanel.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0)); // Indent

        // Get current user's owned games
        String currentUser = DatabaseHandler.getCurrentUser();
        if (currentUser == null || currentUser.isEmpty()) {
            return panel;
        }
            String userJsonPath = resourceLoader.RESOURCE_DIRECTORY + "Users/" + currentUser + ".json";
            try (java.io.Reader reader = new java.io.FileReader(userJsonPath)) {
            com.google.gson.Gson gson = new com.google.gson.Gson();
            java.util.Map<String, Object> userData = gson.fromJson(reader, java.util.Map.class);
            @SuppressWarnings("unchecked")
            java.util.List<Double> ownedGameIdsDouble = (java.util.List<Double>) userData.get("ownedGameIds");
            
            // Convert Double IDs to Integer
            java.util.List<Integer> ownedGameIds = new java.util.ArrayList<>();
            if (ownedGameIdsDouble != null) {
                for (Double id : ownedGameIdsDouble) {
                    ownedGameIds.add(id.intValue());
                }
            }
            
            if (ownedGameIds == null || ownedGameIds.isEmpty()) {
                JLabel noGamesLabel = new JLabel("No games available.");
                noGamesLabel.setForeground(LightModeToggle.getTextColor());
                noGamesLabel.setFont(new Font("Arial", Font.PLAIN, 12));
                ownedGamesPanel.add(noGamesLabel);
            } else {
                for (Integer gameId : ownedGameIds) {
                    java.util.Map<String, String> gameDetails = resourceLoader.getGameById(gameId);
                    if (gameDetails != null) {
                        // Use the same name formatting as the store
                        String formattedName = NameFormatting.formatGameName(gameDetails.get("gameName"));
                        addGameItem(ownedGamesPanel, formattedName, frame, onItemClick);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading user data: " + e.getMessage());
            e.printStackTrace();
        }
        panel.add(ownedGamesPanel);
        return panel;
    }
    
    private static void addItem(JPanel panel, String text, String icon, Consumer<String> onItemClick) {
        ThemeButton button = new ThemeButton(text + " " + icon);
        button.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        FontManager.fixIcon(button, Integer.MAX_VALUE, 40);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(LightModeToggle.getComponentColor());
                button.setOpaque(true);
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(LightModeToggle.getComponentColor());
                button.setOpaque(false);
            }
        });
        
        button.addActionListener(_ -> {
            onItemClick.accept(text.toUpperCase());
        });
        
        panel.add(button);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
    }

    private static void addGameItem(JPanel panel, String text, mainFrame frame, @SuppressWarnings("unused") Consumer<String> onItemClick) {
        ThemeButton button = new ThemeButton(text, false, true, LightModeToggle.getTextColor());
        FontManager.fixIcon(button, Integer.MAX_VALUE, 40);
        button.setHorizontalAlignment(SwingConstants.LEFT); // Size messed up lol, I'll figure it out in prefinals
        
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setForeground(LightModeToggle.getAccentColor());
            } @Override
            public void mouseExited(MouseEvent e) {
                button.setForeground(LightModeToggle.getTextColor());
            }
        });
        
        button.addActionListener(_ -> frame.showGameDetail(text));
        panel.add(button);
    }

    private static ArrayList<String> loadGamesFromFile(String filename) {
        ArrayList<String> games = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(sidebarCreator.class.getResourceAsStream(filename)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    games.add(line.trim());
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading game list from " + filename + ": " + e.getMessage());
        }
        return games;
    }

    private static void ensureUsersDirectoryExists() {
        java.io.File usersDir = new java.io.File("src/main/resources/users");
        if (!usersDir.exists()) {
            usersDir.mkdirs();
        }
    }
}