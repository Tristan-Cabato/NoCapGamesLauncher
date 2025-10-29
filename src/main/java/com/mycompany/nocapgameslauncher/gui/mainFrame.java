package com.mycompany.nocapgameslauncher.gui;

import com.mycompany.nocapgameslauncher.gui.components.sidebarCreator;
import com.mycompany.nocapgameslauncher.gui.components.HeaderCreator;
import com.mycompany.nocapgameslauncher.gui.panels.*;
import com.mycompany.nocapgameslauncher.gui.utilities.ThemeManager;
import javax.swing.*;
import java.awt.*;

public class mainFrame extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private JPanel sidebarPanel;
    private GameDetail gameDetailPanel;
    private Search searchPanel;
    private String currentPanel = "LIBRARY";

    public mainFrame() {
        initializeFrame();
        setupUI();
        ThemeManager.updateTheme(); // Apply initial theme
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
        
        // Create and add panels
        gameDetailPanel = new GameDetail(this);
        searchPanel = new Search(this);
        mainPanel.add(new Library(this), "LIBRARY");
        mainPanel.add(new Store(this), "STORE");
        mainPanel.add(new Friends(this), "FRIENDS");
        mainPanel.add(new Profile(this), "PROFILE");
        mainPanel.add(gameDetailPanel, "GAME_DETAIL");
        mainPanel.add(searchPanel, "SEARCH");

        getContentPane().add(HeaderCreator.createHeader(this, sidebarPanel), BorderLayout.NORTH);
        getContentPane().add(sidebarPanel, BorderLayout.WEST);
        getContentPane().add(mainPanel, BorderLayout.CENTER);
        
        showCard("LIBRARY");
    }

    public void showCard(String cardName) {
        currentPanel = cardName;
        cardLayout.show(mainPanel, cardName);
    }

    public void showGameDetail(String gameTitle) {
        gameDetailPanel.setGame(gameTitle);
        cardLayout.show(mainPanel, "GAME_DETAIL");
    }

    public void showGameDetail(String gameTitle, String gameDescription) {
        gameDetailPanel.setGame(gameTitle, gameDescription);
        cardLayout.show(mainPanel, "GAME_DETAIL");
    }
    
    public void performSearch(String query) {
        // Determine search scope based on current panel
        String searchScope = "ALL";
        if (currentPanel.equals("LIBRARY")) {
            searchScope = "LIBRARY";
        } else if (currentPanel.equals("STORE")) {
            searchScope = "STORE";
        }
        
        searchPanel.performSearch(query, searchScope);
        cardLayout.show(mainPanel, "SEARCH");
    }
}