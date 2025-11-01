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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.mycompany.nocapgameslauncher.database.DatabaseHandler;
import com.mycompany.nocapgameslauncher.gui.utilities.*;

public class Library extends ThemePanel{
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
        
        // Title panel with proper theming
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        titleLabel = new JLabel("My Library");
        titleLabel.setForeground(LightModeToggle.getTextColor());
        FontManager.setFont(titleLabel, Font.BOLD, 40);
        titlePanel.add(titleLabel, BorderLayout.WEST);
        titlePanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        add(titlePanel, BorderLayout.NORTH);
        
        // Main content panel with proper theming
        ThemePanel contentPanel = new ThemePanel(new BorderLayout());
        contentPanel.setBackground(LightModeToggle.getComponentColor());
        contentPanel.setOpaque(true);
        contentPanel.setBorder(new EmptyBorder(0, 20, 20, 20));
        
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
                            gameId,
                            () -> frame.showGameDetail(title, gameId)
                        );
                        
                        JLabel imageLabel = (JLabel) ((BorderLayout)card.getLayout()).getLayoutComponent(BorderLayout.CENTER);
                        
                        // Create a proxy image that will load the actual image when needed
                        ImageIcon proxyIcon = new ImageIcon(resourceLoader.RESOURCE_DIRECTORY + resourceLoader.PROXYIMAGE);
                        imageLabel.setIcon(proxyIcon);
                        
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
                                        imageLabel.repaint();
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
            
            scrollPane = new JScrollPane(cardsPanel);
            scrollPane.setBorder(BorderFactory.createEmptyBorder());
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

    // Panel state management
    private JScrollPane scrollPane;
    private boolean isContentCreated = false;
    
    public void showPanel() {
        // Always recreate content to ensure fresh state
        if (isContentCreated) {
            removeAll();
            isContentCreated = false;
        }
        
        if (!isContentCreated) {
            createContentView();
            isContentCreated = true;
        }
        
        // Update theme before showing
        updateTheme();
        
        setVisible(true);
        if (scrollPane != null) {
            scrollPane.setVisible(true);
            scrollPane.getViewport().setOpaque(false);
            scrollPane.setOpaque(false);
            scrollPane.revalidate();
        }
        
        // Ensure proper background painting
        setOpaque(true);
        if (cardsPanel != null) {
            cardsPanel.setOpaque(true);
            cardsPanel.repaint();
        }
        
        revalidate();
        repaint();
    }
    
    public void hidePanel() {
        // Clean up resources but don't remove components yet
        cleanupHeavyResources();
        setVisible(false);
        if (scrollPane != null) {
            scrollPane.setVisible(false);
        }
    }
    
    private void cleanupHeavyResources() {
        // Clean up any heavy resources like images
        if (gameCardsList != null) {
            for (JPanel card : gameCardsList) {
                cleanupCard(card);
            }
            gameCardsList.clear();
        }
        
        // Clear the cards panel
        if (cardsPanel != null) {
            cardsPanel.removeAll();
        }
    }
    
    private void cleanupCard(JPanel card) {
        // Clean up resources for a single card
        if (card != null) {
            Component[] components = card.getComponents();
            for (Component comp : components) {
                if (comp instanceof JLabel) {
                    Icon icon = ((JLabel) comp).getIcon();
                    if (icon instanceof ImageIcon) {
                        Image img = ((ImageIcon) icon).getImage();
                        if (img != null) {
                            img.flush();
                        }
                    }
                    ((JLabel) comp).setIcon(null);
                }
            }
            card.removeAll();
        }
    }
    
    private void cleanupComponents(Container container) {
        for (Component comp : container.getComponents()) {
            // Clean up any image resources
            if (comp instanceof JLabel) {
                Icon icon = ((JLabel)comp).getIcon();
                if (icon instanceof ImageIcon) {
                    Image image = ((ImageIcon)icon).getImage();
                    if (image != null) {
                        image.flush();
                    }
                }
            }
            // Recursively clean up child components
            if (comp instanceof Container) {
                cleanupComponents((Container)comp);
            }
        }
    }
}
