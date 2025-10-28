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
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.*;
import java.io.*;
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
            titleLabel.setFont(new Font("Arial", Font.BOLD, 40));
            titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            add(titleLabel, BorderLayout.NORTH);

            cardsPanel = new ThemePanel(new GridLayout(0, 4, CARD_GAP, CARD_GAP));
            cardsPanel.setBorder(new EmptyBorder(20, 0, 0, 0));

            gameCardsList = new ArrayList<>();
            
            ArrayList<String> gameTitles = loadGamesFromFile("/store_games.txt");

            if (gameTitles.isEmpty()) {
                JLabel noGamesLabel = new JLabel("No games available.");
                noGamesLabel.setForeground(Color.WHITE);
                FontManager.setFont(noGamesLabel, Font.PLAIN, 20);
                cardsPanel.add(noGamesLabel);
            } else {
                for (int i = 0; i < gameTitles.size(); i++) {
                    String title = gameTitles.get(i);
                    ImageIcon gameIcon = resourceLoader.loadIcon("ImageResources/default_game_icon.jpg"); 
                    gameCardsList.add(GameCardCreator.createGameCard(title, "", gameIcon, () -> frame.showGameDetail(title)));
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
        } catch (Throwable t) {
            JOptionPane.showMessageDialog(this,
                "Failed to build content: " + t.getClass().getSimpleName() + " - " + t.getMessage(),
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

    private ArrayList<String> loadGamesFromFile(String filename) {
        ArrayList<String> games = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(Store.class.getResourceAsStream(filename)))) {
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
