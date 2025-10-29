package com.mycompany.nocapgameslauncher.gui.panels;

import com.mycompany.nocapgameslauncher.gui.mainFrame;
import com.mycompany.nocapgameslauncher.gui.components.GameCardCreator;
import com.mycompany.nocapgameslauncher.gui.resourceHandling.NameFormatting;
import com.mycompany.nocapgameslauncher.gui.resourceHandling.resourceLoader;


import static com.mycompany.nocapgameslauncher.gui.components.GameCardCreator.CARD_WIDTH;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import java.awt.*;
import java.io.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.mycompany.nocapgameslauncher.database.DatabaseHandler;
import com.mycompany.nocapgameslauncher.gui.utilities.*;

public class Library extends ThemePanel {
    private final mainFrame frame;
    private ThemePanel cardsPanel;
    private List<JPanel> gameCardsList;
    private JLabel titleLabel;
    private static final int CARD_GAP = 20;

    public Library(mainFrame frame) {
        super(new BorderLayout());
        this.frame = Objects.requireNonNull(frame, "Frame cannot be null");
        createContentView();
        updateTheme();
    }
    
    public void refreshLibrary() {
        removeAll();
        createContentView();
        revalidate();
        repaint();
    }

    @SuppressWarnings("BusyWait") // Placeholder suppression for now
    private void createContentView() {
        removeAll();
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Title
        titleLabel = new JLabel("My Library");
        FontManager.setFont(titleLabel, Font.BOLD, 40);
        add(titleLabel, BorderLayout.NORTH);
        
        // Main content panel
        ThemePanel contentPanel = new ThemePanel(new BorderLayout());
        contentPanel.setBorder(new EmptyBorder(20, 0, 0, 0));
        
        // Cards panel with grid layout
        cardsPanel = new ThemePanel(new GridLayout(0, 3, 20, 20));
        
        // Get current user's owned games
        String currentUser = DatabaseHandler.getCurrentUser();
        if (currentUser == null || currentUser.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No user is currently logged in.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String userJsonPath = resourceLoader.RESOURCE_DIRECTORY + "Users/" + currentUser + ".json";
        
        try (Reader reader = new FileReader(userJsonPath)) {
            com.google.gson.Gson gson = new com.google.gson.Gson();
            Map<String, Object> userData = gson.fromJson(reader, Map.class);
            @SuppressWarnings("unchecked")
            List<Double> ownedGameIdsDouble = (List<Double>) userData.get("ownedGameIds");
            
            if (ownedGameIdsDouble == null || ownedGameIdsDouble.isEmpty()) {
                JLabel noGamesLabel = new JLabel("No games in your library.");
                noGamesLabel.setForeground(LightModeToggle.getTextColor());
                FontManager.setFont(noGamesLabel, Font.PLAIN, 20);
                cardsPanel.add(noGamesLabel);
            } else {
                gameCardsList = new ArrayList<>();
                for (Double gameIdDouble : ownedGameIdsDouble) {
                    int gameId = gameIdDouble.intValue();
                    Map<String, String> gameDetails = resourceLoader.getGameById(gameId);
                    if (gameDetails != null) {
                        String title = NameFormatting.formatGameName(gameDetails.get("gameName"));
                        String description = gameDetails.get("description");
                        String iconPath = "ImageResources/" + title.toLowerCase().replace(" ", "_") + ".jpg";
                        ImageIcon gameIcon = new ImageIcon(resourceLoader.RESOURCE_DIRECTORY + resourceLoader.PROXYIMAGE);
                        
                        JPanel card = GameCardCreator.createGameCard(
                            title,
                            description,
                            gameIcon,
                            () -> frame.showGameDetail(title, description)
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
                        
                        cardsPanel.add(card);
                        gameCardsList.add(card);
                    }
                }
            }
            
            JScrollPane scrollPane = new JScrollPane(cardsPanel);
            scrollPane.setBorder(null);
            scrollPane.getVerticalScrollBar().setUnitIncrement(16);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            scrollPane.getViewport().setBackground(LightModeToggle.getComponentColor());
            
            contentPanel.add(scrollPane, BorderLayout.CENTER);
            add(contentPanel, BorderLayout.CENTER);
            
            revalidate();
            repaint();
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Failed to build content: " + e.getClass().getSimpleName() + " - " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
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
