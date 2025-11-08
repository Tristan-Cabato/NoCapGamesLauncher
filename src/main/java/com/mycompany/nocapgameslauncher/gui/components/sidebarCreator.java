package com.mycompany.nocapgameslauncher.gui.components;

import com.mycompany.nocapgameslauncher.database.DatabaseHandler;
import com.mycompany.nocapgameslauncher.gui.mainFrame;
import com.mycompany.nocapgameslauncher.resourceHandling.NameFormatting;
import com.mycompany.nocapgameslauncher.resourceHandling.resourceLoader;
import com.mycompany.nocapgameslauncher.userManager.UserDataIterator;
import com.mycompany.nocapgameslauncher.gui.utilities.*;

import java.awt.*;
import java.awt.event.*;
import java.util.function.*;
import javax.swing.*;
import java.util.Map;

public class sidebarCreator {
    private static JPanel currentSidebarPanel;
    
    public static JPanel getCurrentSidebarPanel() {
        return currentSidebarPanel;
    }

            
    public static JPanel createNavigationSidebar(int width, mainFrame frame, Consumer<String> onItemClick) {
        currentSidebarPanel = new ThemePanel() {
            @Override
            public void updateTheme() {
                setBackground(LightModeToggle.getSidebarColor());
            }
        };
        currentSidebarPanel.setLayout(new BoxLayout(currentSidebarPanel, BoxLayout.Y_AXIS));
        currentSidebarPanel.setPreferredSize(new Dimension(width, Integer.MAX_VALUE));
        currentSidebarPanel.setMaximumSize(new Dimension(width, Integer.MAX_VALUE));
        currentSidebarPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 1));
        
        addItem(currentSidebarPanel, "Owned Games", "📦", onItemClick);
        
        // Add owned games list
        ThemePanel ownedGamesPanel = createOwnedGamesPanel(frame, onItemClick);
        currentSidebarPanel.add(ownedGamesPanel);
        return currentSidebarPanel;
    }
    
    private static ThemePanel createOwnedGamesPanel(mainFrame frame, Consumer<String> onItemClick) {
        ThemePanel ownedGamesPanel = new ThemePanel() {
            @Override
            public void updateTheme() {
                setBackground(LightModeToggle.getSidebarColor());
                setForeground(LightModeToggle.getTextColor());
            }
        };
        ownedGamesPanel.setLayout(new BoxLayout(ownedGamesPanel, BoxLayout.Y_AXIS));
        ownedGamesPanel.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0)); // Indent

        // Get current user's owned games using the new iterator
        String currentUser = DatabaseHandler.getCurrentUser();
        if (currentUser == null || currentUser.isEmpty()) {
            return ownedGamesPanel;
        }

        UserDataIterator gameIdIterator = new UserDataIterator(currentUser);
        
        if (gameIdIterator.getGameCount() == 0) {
            JLabel noGamesLabel = new JLabel("No games available.");
            noGamesLabel.setForeground(LightModeToggle.getTextColor());
            noGamesLabel.setFont(new Font("Arial", Font.PLAIN, 12));
            ownedGamesPanel.add(noGamesLabel);
        } else {
            while (gameIdIterator.hasNext()) {
                int gameId = gameIdIterator.next();
                Map<String, String> gameDetails = resourceLoader.getGameById(gameId);
                if (gameDetails != null) {
                    String formattedName = NameFormatting.formatGameName(gameDetails.get("gameName"));
                    addGameItem(ownedGamesPanel, formattedName, gameId, frame, onItemClick);
                }
            }
        }
        return ownedGamesPanel;
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

    private static void addGameItem(JPanel panel, String text, int gameId, mainFrame frame, @SuppressWarnings("unused") Consumer<String> onItemClick) {
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
        
        button.addActionListener(_ -> frame.showGameDetail(text, gameId));
        panel.add(button);
    }
}