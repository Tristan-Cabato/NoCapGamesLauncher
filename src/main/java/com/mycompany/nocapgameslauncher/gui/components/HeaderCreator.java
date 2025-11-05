package com.mycompany.nocapgameslauncher.gui.components;

import com.mycompany.nocapgameslauncher.gui.mainFrame;
import com.mycompany.nocapgameslauncher.gui.utilities.*;
import com.mycompany.nocapgameslauncher.iterator.SessionIterator;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import java.awt.*;

import com.mycompany.nocapgameslauncher.NoCapGamesLauncher;

public class HeaderCreator {
    public static JPanel createHeader(mainFrame frame, JPanel sidebarPanel) {
        ThemePanel headerPanel = new ThemePanel(new BorderLayout()) {
            @Override
            public void updateTheme() {
                super.updateTheme();
                setBackground(LightModeToggle.getAccentColor());
            }
        };
        headerPanel.setBorder(new EmptyBorder(10, 20, 10, 20));

        ThemePanel headerLinks = new ThemePanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        headerLinks.setOpaque(false);
        
        ThemeButton toggleButton = new ThemeButton("=");
        FontManager.setFont(toggleButton, Font.BOLD, 24);
        toggleButton.setBorder(new EmptyBorder(0, 0, 0, 15));
        toggleButton.setFocusPainted(false);
        toggleButton.setContentAreaFilled(false);
        toggleButton.setForeground(Color.WHITE);
        toggleButton.setManageForeground(false); // Disable theme management for foreground
        toggleButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        toggleButton.addActionListener(_ -> {
            sidebarPanel.setVisible(!sidebarPanel.isVisible());
            frame.revalidate();
            frame.repaint();
        });
        headerLinks.add(toggleButton);

        ThemeButton homeButton = createHeaderLink("Home");
        homeButton.setForeground(Color.WHITE);
        homeButton.setManageForeground(false); // Disable theme management for foreground
        homeButton.addActionListener(_ -> {
            frame.showCard("LIBRARY");
        });
        ThemeButton storeLink = createHeaderLink("Store");
        storeLink.setForeground(Color.WHITE);
        storeLink.setManageForeground(false); // Disable theme management for foreground
        storeLink.addActionListener(_ -> {
            frame.showCard("STORE");
        });
        ThemeButton friendsLink = createHeaderLink("Friends");
        friendsLink.setForeground(Color.WHITE);
        friendsLink.setManageForeground(false); // Disable theme management for foreground
        friendsLink.addActionListener(_ -> {
            frame.showCard("FRIENDS");
        });
        headerLinks.add(homeButton);
        headerLinks.add(storeLink);
        headerLinks.add(friendsLink);
        headerPanel.add(headerLinks, BorderLayout.WEST);

        JTextField searchBar = new JTextField("Search games...") {
            @Override
            public void updateUI() {
                super.updateUI();
                setForeground(LightModeToggle.getTextColor());
                setBackground(LightModeToggle.getComponentColor());
                setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(LightModeToggle.getComponentColor().brighter()),
                    new EmptyBorder(8, 15, 8, 15)
                ));
            }
        };
        ThemeManager.addComponent(searchBar::updateUI);
        searchBar.setPreferredSize(new Dimension(400, 30));
        searchBar.setToolTipText("Search games...");
        searchBar.putClientProperty("JComponent.roundedCorners", true);
        
        // Add focus listener to clear placeholder text
        searchBar.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (searchBar.getText().equals("Search games...")) {
                    searchBar.setText("");
                }
            }
            
            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (searchBar.getText().isEmpty()) {
                    searchBar.setText("Search games...");
                }
            }
        });
        
        // Add action listener for Enter key
        searchBar.addActionListener(_ -> {
            String query = searchBar.getText().trim();
            if (!query.isEmpty() && !query.equals("Search games...")) {
                frame.performSearch(query);
            }
        });
        
        ThemePanel searchPanel = new ThemePanel(new GridBagLayout());
        searchPanel.setOpaque(false);
        searchPanel.add(searchBar);
        headerPanel.add(searchPanel, BorderLayout.CENTER);

        ThemePanel profilePanel = new ThemePanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        profilePanel.setOpaque(false);
        
        ThemeButton themeToggleButton = createHeaderLink(LightModeToggle.isLightMode() ? LightModeToggle.LIGHT_ICON : LightModeToggle.DARK_ICON);
        themeToggleButton.setForeground(Color.WHITE);
        themeToggleButton.setManageForeground(false); // Disable theme management for foreground
        FontManager.fixIcon(themeToggleButton, 32, 32);
        themeToggleButton.addActionListener(_ -> {
            themeToggleButton.setText(LightModeToggle.isLightMode() ? LightModeToggle.DARK_ICON : LightModeToggle.LIGHT_ICON);
            LightModeToggle.toggle();
            ThemeManager.updateTheme();
            
            // Update the session memento with the new theme state
            var currentMemento = SessionIterator.getCurrentMemento();
            if (currentMemento != null) {
                currentMemento.setThemeState(LightModeToggle.isLightMode() ? "LIGHT" : "DARK");
            }
        });
        profilePanel.add(themeToggleButton);
        
        ThemeButton profileIcon = createHeaderLink("👤");
        profileIcon.setForeground(Color.WHITE);
        profileIcon.setManageForeground(false); // Disable theme management for foreground
        FontManager.fixIcon(profileIcon, 32, 32);
        profileIcon.addActionListener(_ -> {
            JPopupMenu profileMenu = new JPopupMenu();
            profileMenu.setBackground(LightModeToggle.getComponentColor());

            JMenuItem logoutItem = new JMenuItem("Logout");
            logoutItem.setForeground(LightModeToggle.getTextColor());
            logoutItem.setBackground(LightModeToggle.getComponentColor());
            logoutItem.addActionListener(e -> {
                // Clear the session on logout
                SessionIterator.clearCurrentSession();
                frame.dispose();
                NoCapGamesLauncher.main(null);
            });
            profileMenu.add(logoutItem);
            profileMenu.show(profileIcon, 0, profileIcon.getHeight());
        });
        profilePanel.add(profileIcon);
        
        headerPanel.add(profilePanel, BorderLayout.EAST);
        return headerPanel;
    }
    
    private static ThemeButton createHeaderLink(String text) {
        ThemeButton link = new ThemeButton(text);
        link.setBorderPainted(false);
        link.setFocusPainted(false);
        link.setContentAreaFilled(false);
        link.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        return link;
    }
}