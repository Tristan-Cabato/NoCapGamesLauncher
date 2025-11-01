package com.mycompany.nocapgameslauncher.gui.panels;

import com.mycompany.nocapgameslauncher.gui.mainFrame;
import com.mycompany.nocapgameslauncher.gui.components.GameCardCreator;
import com.mycompany.nocapgameslauncher.gui.resourceHandling.resourceLoader;
import com.mycompany.nocapgameslauncher.gui.resourceHandling.NameFormatting;
import com.mycompany.nocapgameslauncher.gui.utilities.FontManager;
import com.mycompany.nocapgameslauncher.gui.utilities.LightModeToggle;
import com.mycompany.nocapgameslauncher.gui.utilities.ThemePanel;
import static com.mycompany.nocapgameslauncher.gui.components.GameCardCreator.CARD_WIDTH;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

import org.json.*;

public class Search extends ThemePanel {
    private final mainFrame frame;
    private ThemePanel cardsPanel;
    private ArrayList<JPanel> gameCardsList;
    private ArrayList<String> allGameTitles;
    private ArrayList<String> allGameDescriptions;
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
        allGameTitles = new ArrayList<>();
        allGameDescriptions = new ArrayList<>();
        
        // Load from library
        ArrayList<String> libraryTitles = resourceLoader.loadGamesFromFile("/library_games.txt");
        ArrayList<String> libraryDescriptions = resourceLoader.loadGameDescriptionsFromFile("/gamedesc.txt");
        
        allGameTitles.addAll(libraryTitles);
        for (int i = 0; i < libraryTitles.size(); i++) {
            if (i < libraryDescriptions.size()) {
                allGameDescriptions.add(libraryDescriptions.get(i));
            } else {
                allGameDescriptions.add("");
            }
        }
        
        // Load from store
        ArrayList<String> storeTitles = NameFormatting.getGameTitlesFromJson();
        for (String title : storeTitles) {
            if (!allGameTitles.contains(title)) {
                allGameTitles.add(title);
                allGameDescriptions.add("");
            }
        }
    }
    
    private ArrayList<String> getLibraryTitles() {
        ArrayList<String> titles = new ArrayList<>();
        File gamesFile = new File("user_data/store_games.json");
        if (gamesFile.exists()) {
            try {
                // Read the JSON file
                String json = new String(Files.readAllBytes(gamesFile.toPath()));
                // Parse the JSON array
                JSONArray games = new JSONArray(json);
                
                // Extract game titles
                for (int i = 0; i < games.length(); i++) {
                    JSONObject game = games.getJSONObject(i);
                    titles.add(game.getString("gameName"));
                }
            } catch (IOException | JSONException e) {
                System.out.println("Error loading library titles: " + e.getMessage() + "\n");
            }
        }
        return titles;
    }
    
    private ArrayList<String> getStoreTitles() {
        ArrayList<String> titles = new ArrayList<>();
        try (InputStream is = getClass().getResourceAsStream("/store_games.json");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            
            String json = reader.lines().collect(Collectors.joining());
            JSONArray gamesArray = new JSONArray(json);
            
            for (int i = 0; i < gamesArray.length(); i++) {
                JSONObject game = gamesArray.getJSONObject(i);
                String gameName = game.getString("gameName");
                titles.add(gameName);
            }
        } catch (Exception e) {
            System.err.println("Error reading store_games.json: " + e.getMessage());
        }
        return titles;
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
    
    @SuppressWarnings("BusyWait") // Placeholder suppression for now
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
        
        // Determine which games to search based on scope
        ArrayList<String> searchTitles = new ArrayList<>();
        ArrayList<String> searchDescriptions = new ArrayList<>();
        
        switch (scope) {
            case "LIBRARY" -> {
            ArrayList<String> libraryTitles = getLibraryTitles();
            ArrayList<String> libraryDescriptions = resourceLoader.loadGameDescriptionsFromFile("/gamedesc.txt");
            searchTitles.addAll(libraryTitles);
            for (int i = 0; i < libraryTitles.size(); i++) {
                if (i < libraryDescriptions.size()) {
                    searchDescriptions.add(libraryDescriptions.get(i));
                } else {
                    searchDescriptions.add("");
                }
            } titleLabel.setText("Library Search Results");
            } case "STORE" -> {
                searchTitles.addAll(getStoreTitles());
                for (String _ : searchTitles) {
                    searchDescriptions.add("");
                }
                titleLabel.setText("Store Search Results");
            } default -> {
                searchTitles.addAll(allGameTitles);
                searchDescriptions.addAll(allGameDescriptions);
                titleLabel.setText("Search Results");
            }
        }
        // Search for matching games
        int matchCount = 0;
        for (int i = 0; i < searchTitles.size(); i++) {
            String title = searchTitles.get(i);
            String description = i < searchDescriptions.size() ? searchDescriptions.get(i) : "";
            
            // Check if title or description contains the search query
            if (title.toLowerCase().contains(currentQuery) || 
                description.toLowerCase().contains(currentQuery)) {
                    String iconPath = "ImageResources/" + title.toLowerCase().replace(" ", "_") + ".jpg";
                    // Load proxy image immediately
                    ImageIcon gameIcon = resourceLoader.loadIcon(resourceLoader.PROXYIMAGE);
                    
                    // Create the card with the proxy image first
                    JPanel card = GameCardCreator.createGameCard(title, description, gameIcon, -1, () -> frame.showGameDetail(title));
                    
                    // Store a reference to the card's image label
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
                    cardsPanel.add(card);
                    matchCount++;
            }
        }
        
        // Update results label
        String scopeText = scope.equals("LIBRARY") ? " in Library" : (scope.equals("STORE") ? " in Store" : "");
        if (matchCount == 0) {
            resultsLabel.setText("No games found for \"" + query + "\"" + scopeText);
            JLabel noResultsLabel = new JLabel("Try a different search term");
            FontManager.setFont(noResultsLabel, Font.PLAIN, 18);
            noResultsLabel.setForeground(LightModeToggle.getTextColor());
            cardsPanel.add(noResultsLabel);
        } else {
            resultsLabel.setText("Found " + matchCount + " game" + (matchCount != 1 ? "s" : "") + " for \"" + query + "\"" + scopeText);
        }
        
        updateGridColumns();
        cardsPanel.revalidate();
        cardsPanel.repaint();
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
