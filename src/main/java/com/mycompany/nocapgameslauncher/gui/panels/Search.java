package com.mycompany.nocapgameslauncher.gui.panels;

import com.mycompany.nocapgameslauncher.game_manager.Game;
import com.mycompany.nocapgameslauncher.game_manager.GameManager;
import com.mycompany.nocapgameslauncher.gui.components.GameCardCreator;
import com.mycompany.nocapgameslauncher.gui.mainFrame;
import com.mycompany.nocapgameslauncher.gui.utilities.FontManager;
import com.mycompany.nocapgameslauncher.gui.utilities.LightModeToggle;
import com.mycompany.nocapgameslauncher.gui.utilities.ThemePanel;
import com.mycompany.nocapgameslauncher.resourceHandling.resourceLoader;
import com.mycompany.nocapgameslauncher.resourceHandling.NameFormatting;
import java.io.File;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

import static com.mycompany.nocapgameslauncher.gui.components.GameCardCreator.CARD_WIDTH;

public class Search extends ThemePanel {
    private final mainFrame frame;
    private ThemePanel cardsPanel;
    private ArrayList<JPanel> gameCardsList;
    private List<Game> allGames;
    private JLabel titleLabel;
    private JLabel resultsLabel;
    private static final int CARD_GAP = 20;
    private String currentQuery = "";

    @SuppressWarnings("OverridableMethodCallInConstructor")
    public Search(mainFrame frame) {
        super(new BorderLayout());
        this.frame = frame;
        loadAllGames();
        createContentView();
        updateTheme();
    }

    private void loadAllGames() {
        GameManager gameManager = GameManager.getInstance();
        allGames = gameManager.getAllGames();
    }

    private void createContentView() {
        try {
            setBorder(new EmptyBorder(20, 20, 20, 20));

            titleLabel = new JLabel("Search Results");
            titleLabel.setFont(new Font("Arial", Font.BOLD, 40));
            titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            resultsLabel = new JLabel("Enter a search term to find games");
            resultsLabel.setFont(new Font("Arial", Font.PLAIN, 16));
            resultsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            ThemePanel topPanel = new ThemePanel(new BorderLayout());
            topPanel.setOpaque(false);
            topPanel.add(titleLabel, BorderLayout.NORTH);
            topPanel.add(resultsLabel, BorderLayout.SOUTH);
            topPanel.setBorder(new EmptyBorder(0, 0, 10, 0));
            
            add(topPanel, BorderLayout.NORTH);

            cardsPanel = new ThemePanel(new GridLayout(0, 4, CARD_GAP, CARD_GAP));
            cardsPanel.setBorder(new EmptyBorder(20, 0, 0, 0));

            gameCardsList = new ArrayList<>();

            JScrollPane scrollPane = new JScrollPane(cardsPanel);
            scrollPane.setBorder(null);
            scrollPane.getVerticalScrollBar().setUnitIncrement(16);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

            add(scrollPane, BorderLayout.CENTER);
            
            // Add component listener to dynamically adjust columns
            addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    updateGridColumns();
                }
            });
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Failed to build content: " + e.getClass().getSimpleName() + " - " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public void performSearch(String query, String scope) {
        currentQuery = query.trim().toLowerCase();
        
        // Clear existing cards
        cardsPanel.removeAll();
        gameCardsList.clear();
        
        if (currentQuery.isEmpty()) {
            resultsLabel.setText("Enter a search term to find games");
            cardsPanel.revalidate();
            cardsPanel.repaint();
            return;
        }
        
        // Filter games based on search query and scope
        List<Game> matchingGames = allGames.stream()
            .filter(game -> game.getTitle().toLowerCase().contains(currentQuery) || 
                          game.getDescription().toLowerCase().contains(currentQuery))
            .toList();
        
        // Update UI based on search results
        if (matchingGames.isEmpty()) {
            resultsLabel.setText("No games found matching '" + query + "'");
        } else {
            resultsLabel.setText("Found " + matchingGames.size() + " games matching '" + query + "'");
            
            for (Game game : matchingGames) {
                String formattedTitle = NameFormatting.formatGameName(game.getTitle());
                String imagePath = resourceLoader.RESOURCE_DIRECTORY + "ImageResources/" + 
                    formattedTitle.toLowerCase().replace(" ", "_") + ".jpg";
                File imageFile = new File(imagePath);
                ImageIcon gameIcon;
                
                if (imageFile.exists()) {
                    gameIcon = new ImageIcon(imagePath);
                } else {
                    gameIcon = new ImageIcon(resourceLoader.RESOURCE_DIRECTORY + resourceLoader.PROXYIMAGE);
                }
                
                JPanel card = GameCardCreator.createGameCard(
                    formattedTitle,
                    game.getDescription(),
                    gameIcon,
                    game.getID(),
                    () -> frame.showGameDetail(game.getTitle(), game.getID())
                );
                
                // Add card to the panel
                gameCardsList.add(card);
                cardsPanel.add(card);
                
                // Load actual image in background
                loadGameImageAsync(game, card);
            }
        }
        
        // Update the UI
        cardsPanel.revalidate();
        cardsPanel.repaint();
    }
    
    private void loadGameImageAsync(Game game, JPanel card) {
        new Thread(() -> {
            try {
                // Simulate loading
                Thread.sleep(500);
                
                // Load actual image
                ImageIcon actualIcon = resourceLoader.loadIcon(game.getImageUrl());
                if (actualIcon != null) {
                    // Find and update the image label in the card
                    Component[] components = card.getComponents();
                    for (Component comp : components) {
                        if (comp instanceof JLabel label) {
                            if (label.getIcon() != null) {
                                // This is the image label
                                SwingUtilities.invokeLater(() -> {
                                    Image scaled = actualIcon.getImage().getScaledInstance(
                                        180, 180, Image.SCALE_SMOOTH);
                                    label.setIcon(new ImageIcon(scaled));
                                    label.repaint();
                                });
                                break;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Error loading game image: " + e.getMessage());
            }
        }, "ImageLoader-" + game.getTitle()).start();
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
        if (resultsLabel != null) {
            resultsLabel.setForeground(LightModeToggle.getTextColor());
        }
        if (cardsPanel != null) {
            cardsPanel.updateTheme();
        }
    }
}
