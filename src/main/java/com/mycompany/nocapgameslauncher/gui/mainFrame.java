package com.mycompany.nocapgameslauncher.gui;

import com.mycompany.nocapgameslauncher.gui.components.sidebarCreator;
import com.mycompany.nocapgameslauncher.gui.components.HeaderCreator;
import com.mycompany.nocapgameslauncher.gui.panels.*;
import com.mycompany.nocapgameslauncher.gui.utilities.ThemeManager;
import com.mycompany.nocapgameslauncher.gui.userManager.SessionIterator;
import javax.swing.*;
import java.awt.*;

public class mainFrame extends JFrame {
    private CardLayout cardLayout;
    private Library libraryPanel;
    private Store storePanel;
    private boolean storeInitialized = false;
    private JPanel mainPanel;
    private JPanel sidebarPanel;
    private GameDetail gameDetailPanel;
    private Search searchPanel;
    private Friends friendsPanel;
    private Profile profilePanel;
    
    private JPanel currentPanel;
    private String currentPanelName = "LIBRARY";

    public Library getLibraryPanel() {
        return libraryPanel;
    }

    public mainFrame() {
        initializeFrame();
        setupUI();
        ThemeManager.updateTheme(); // Apply initial theme
    }
    
    public mainFrame(String startPanel) {
        initializeFrame();
        setupUI();
        ThemeManager.updateTheme(); // Apply initial theme
        // Navigate to the saved panel
        if (startPanel != null && !startPanel.isEmpty() && !startPanel.equals("LOGIN")) {
            showCard(startPanel);
        }
    }

    private void initializeFrame() {
        setTitle("No Cap Games Launcher");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(screenSize.width * 3/4, screenSize.height * 3/4);
        setLocationRelativeTo(null);
    }

    private void setupUI() {
        sidebarPanel = sidebarCreator.createNavigationSidebar(250, this, this::showCard);
        
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        
        // Initialize only the library panel by default
        libraryPanel = new Library(this);
        gameDetailPanel = new GameDetail(this);
        searchPanel = new Search(this);
        friendsPanel = new Friends(this);
        profilePanel = new Profile(this);

        // Add all panels to the card layout
        mainPanel.add(libraryPanel, "LIBRARY");
        // Store panel will be added when first accessed
        mainPanel.add(new JPanel(), "STORE"); // Placeholder
        mainPanel.add(friendsPanel, "FRIENDS");
        mainPanel.add(profilePanel, "PROFILE");
        mainPanel.add(gameDetailPanel, "GAME_DETAIL");
        mainPanel.add(searchPanel, "SEARCH");

        getContentPane().add(HeaderCreator.createHeader(this, sidebarPanel), BorderLayout.NORTH);
        getContentPane().add(sidebarPanel, BorderLayout.WEST);
        getContentPane().add(mainPanel, BorderLayout.CENTER);
        
        showCard("LIBRARY");
    }

    public void showCard(String cardName) {
        // Hide current panel
        if (currentPanel != null) {
            if (currentPanel instanceof Library) {
                ((Library) currentPanel).hidePanel();
            } else if (currentPanel instanceof Store) {
                ((Store) currentPanel).hidePanel();
            } else if (currentPanel == gameDetailPanel) {
                gameDetailPanel.setVisible(false);
            }
        }
        
        // Show new panel
        switch (cardName) {
            case "LIBRARY" -> {
                libraryPanel.showPanel();
                currentPanel = libraryPanel;
            } case "STORE" -> {
                if (!storeInitialized) {
                    storePanel = new Store(this);
                    mainPanel.add(storePanel, "STORE");
                    storeInitialized = true;
                }
                storePanel.showPanel();
                currentPanel = storePanel;
            } case "FRIENDS" -> {
                currentPanel = friendsPanel;
            } case "PROFILE" -> {
                currentPanel = profilePanel;
            } case "GAME_DETAIL" -> {
                if (gameDetailPanel.getParent() == null) {
                    mainPanel.add(gameDetailPanel, "GAME_DETAIL");
                }
                gameDetailPanel.setVisible(true);
                currentPanel = gameDetailPanel;
            } case "SEARCH" -> {
                searchPanel.setVisible(true);
                currentPanel = searchPanel;
            } default -> { return; }
        }
        
        // Update the current panel name and save to session
        currentPanelName = cardName;
        updateSessionPanel(cardName);
        
        cardLayout.show(mainPanel, cardName);
    }
    
    private void updateSessionPanel(String panelName) {
        // Update the session's last panel
        var currentMemento = SessionIterator.getCurrentMemento();
        if (currentMemento != null) {
            currentMemento.setLastPanel(panelName);
        }
    }

    public void showGameDetail(String gameTitle) {
        showCard("GAME_DETAIL");
        // Default to -1 if we don't have an ID
        gameDetailPanel.setGame(gameTitle, -1);
    }

    public void showGameDetail(String gameTitle, String gameDescription) {
        showCard("GAME_DETAIL");
        // Default to -1 if we don't have an ID
        gameDetailPanel.setGame(gameTitle, -1);
        gameDetailPanel.setGame(gameTitle, gameDescription);
    }
    
    public void showGameDetail(String gameTitle, int gameId) {
        showCard("GAME_DETAIL");
        gameDetailPanel.setGame(gameTitle, gameId);
    }
    
    public void performSearch(String query) {
        // Determine search scope based on current panel
        String searchScope = "ALL";
        if (currentPanel == libraryPanel) {
            searchScope = "LIBRARY";
        } else if (currentPanel == storePanel) {
            searchScope = "STORE";
        }
        
        searchPanel.performSearch(query, searchScope);
        showCard("SEARCH");
    }
}