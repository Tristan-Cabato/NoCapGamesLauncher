package com.mycompany.nocapgameslauncher.gui.panels;

import com.mycompany.nocapgameslauncher.game_manager.Game;
import com.mycompany.nocapgameslauncher.game_manager.GameManager;
import com.mycompany.nocapgameslauncher.gui.components.GameCardCreator;
import com.mycompany.nocapgameslauncher.gui.mainFrame;
import com.mycompany.nocapgameslauncher.gui.utilities.FontManager;
import com.mycompany.nocapgameslauncher.gui.utilities.LightModeToggle;
import com.mycompany.nocapgameslauncher.gui.utilities.ThemePanel;
import com.mycompany.nocapgameslauncher.resourceHandling.resourceLoader;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.border.EmptyBorder;

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
            
            // Initialize scrollPane
            scrollPane = new JScrollPane();

            cardsPanel = new ThemePanel(new GridLayout(0, 3, CARD_GAP, CARD_GAP));
            cardsPanel.setBorder(new EmptyBorder(20, 0, 0, 0));
            gameCardsList = new ArrayList<>();
            
            // Load games using GameManager facade
            try {
                GameManager gameManager = GameManager.getInstance();
                Collection<Game> games = gameManager.getAllGames();

                if (games.isEmpty()) {
                    JLabel errorLabel = new JLabel("No games found. Could not load store data.");
                    errorLabel.setForeground(LightModeToggle.getTextColor());
                    add(errorLabel, BorderLayout.CENTER);
                    return;
                } else {
                    System.out.println("Successfully loaded " + games.size() + " games");
                    
                    for (Game game : games) {
                        ImageIcon proxyIcon = new ImageIcon(resourceLoader.RESOURCE_DIRECTORY + resourceLoader.PROXYIMAGE);
                        JPanel card = GameCardCreator.createGameCard(
                            game.getTitle(),
                            game.getDescription(),
                            proxyIcon,
                            game.getID(),
                            () -> frame.showGameDetail(game.getTitle(), game.getID())
                        );
                        
                        // Get the image label from the card
                        JLabel imageLabel = (JLabel) ((BorderLayout)card.getLayout()).getLayoutComponent(BorderLayout.CENTER);
                        
                        // Load actual image in background
                        new Thread(() -> {
                            try {
                                Thread.sleep(1200); // 1.2 seconds | Simulate loading
                                ImageIcon actualIcon = resourceLoader.loadIcon(game.getImageUrl());
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
                        }, "ImageLoader-" + game.getTitle()).start();
                        
                        gameCardsList.add(card);
                        cardsPanel.add(card);
                    }
                }

                scrollPane.setViewportView(cardsPanel);
                scrollPane.setBorder(BorderFactory.createEmptyBorder());
                scrollPane.getVerticalScrollBar().setUnitIncrement(16);
                scrollPane.getViewport().setOpaque(false);
                add(scrollPane, BorderLayout.CENTER);
        } catch (Exception e) {
            System.err.println("Error initializing Store panel: " + e.getMessage());
            e.printStackTrace();
            add(new JLabel("Error loading store content: " + e.getMessage()), BorderLayout.CENTER);
        }
    } catch (Exception e) { System.out.println("Error"); }
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
            scrollPane.revalidate();
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
                card.removeAll();
            }
        }
    }
}
