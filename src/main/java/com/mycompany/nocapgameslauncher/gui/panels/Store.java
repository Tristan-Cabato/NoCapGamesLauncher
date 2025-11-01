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
import java.util.*;

import javax.swing.border.EmptyBorder;

import com.mycompany.nocapgameslauncher.gui.resourceHandling.NameFormatting;

public class Store extends ThemePanel {
    @SuppressWarnings("unused") private final mainFrame frame;
    private ThemePanel cardsPanel;
    private ArrayList<JPanel> gameCardsList;
    private JLabel titleLabel;
    private static final int CARD_GAP = 20;
    private static final Map<String, ImageIcon> imageCache = new HashMap<>();

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
                
                for (int i = 0; i < gameTitles.size(); i++) {
                    String title = gameTitles.get(i);
                    String description = "No description available for this game";
                    String iconPath = "ImageResources/" + title.toLowerCase().replace(" ", "_") + ".jpg";
                    int gameId = i + 1;
                    
                    // Create the card with a proxy icon
                    ImageIcon proxyIcon = new ImageIcon(resourceLoader.RESOURCE_DIRECTORY + resourceLoader.PROXYIMAGE);
                    JPanel card = GameCardCreator.createGameCard(
                        title,
                        description,
                        proxyIcon,
                        gameId,
                        () -> frame.showGameDetail(title, gameId)
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
                                    imageLabel.repaint();
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

            scrollPane = new JScrollPane(cardsPanel);
            scrollPane.setBorder(BorderFactory.createEmptyBorder());
            scrollPane.getVerticalScrollBar().setUnitIncrement(16);
            add(scrollPane, BorderLayout.CENTER);
            scrollPane.getViewport().setOpaque(false);
            
        } catch (Exception e) {
            System.err.println("Error initializing Store panel: " + e.getMessage());
            e.printStackTrace();
            add(new JLabel("Error loading store content: " + e.getMessage()), BorderLayout.CENTER);
        }
    }

    private static class GameData {
        int gameID;
        String gameName;
        String imageURL;
        String gameURL;
        String gameDescription;
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
