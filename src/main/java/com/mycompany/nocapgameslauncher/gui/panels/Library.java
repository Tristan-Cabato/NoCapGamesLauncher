package com.mycompany.nocapgameslauncher.gui.panels;

import com.mycompany.nocapgameslauncher.gui.mainFrame;
import com.mycompany.nocapgameslauncher.gui.components.GameCardCreator;
import com.mycompany.nocapgameslauncher.gui.resourceManager.resourceLoader;

import static com.mycompany.nocapgameslauncher.gui.components.GameCardCreator.CARD_WIDTH;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.*;

import com.mycompany.nocapgameslauncher.gui.utilities.*;

public class Library extends ThemePanel {
    @SuppressWarnings("unused") private final mainFrame frame; // Placeholder suppression for now
    private ThemePanel cardsPanel;
    private ArrayList<JPanel> gameCardsList;
    private JLabel titleLabel;
    private static final int CARD_GAP = 20;

    @SuppressWarnings("OverridableMethodCallInConstructor") // Placeholder suppression for now
    public Library(mainFrame frame) {
        super(new BorderLayout());
        this.frame = frame;
        createContentView();
        updateTheme();
    }

    @SuppressWarnings("BusyWait") // Placeholder suppression for now
    private void createContentView() {
        try {
            setBorder(new EmptyBorder(20, 20, 20, 20));
            titleLabel = new JLabel("My Library");
            FontManager.setFont(titleLabel, Font.BOLD, 40);
            add(titleLabel, BorderLayout.NORTH);

            cardsPanel = new ThemePanel(new GridLayout(0, 4, CARD_GAP, CARD_GAP));
            cardsPanel.setBorder(new EmptyBorder(20, 0, 0, 0));

            gameCardsList = new ArrayList<>();
            
            ArrayList<String> gameTitles = resourceLoader.loadGamesFromFile("/library_games.txt");
            ArrayList<String> gameDescriptions = resourceLoader.loadGameDescriptionsFromFile("/gamedesc.txt");

            if (gameTitles.isEmpty()) {
                JLabel noGamesLabel = new JLabel("No games available.");
                noGamesLabel.setForeground(Color.WHITE);
                FontManager.setFont(noGamesLabel, Font.PLAIN, 20);
                cardsPanel.add(noGamesLabel);
            } else {
                for (int i = 0; i < gameTitles.size(); i++) {
                    String title = gameTitles.get(i);
                    String description = (i < gameDescriptions.size()) ? gameDescriptions.get(i) : "No description available.";
                    String iconPath = "ImageResources/" + title.toLowerCase().replace(" ", "_") + ".jpg";
                    ImageIcon gameIcon = resourceLoader.loadIcon(resourceLoader.PROXYIMAGE);
                    
                    JPanel card = GameCardCreator.createGameCard(title, description, gameIcon, () -> frame.showGameDetail(title));
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
                }
            }

            for (JPanel card : gameCardsList) {
                cardsPanel.add(card);
            }

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
