package com.mycompany.nocapgameslauncher.gui.components;

import com.mycompany.nocapgameslauncher.gui.utilities.LightModeToggle;
import com.mycompany.nocapgameslauncher.gui.utilities.ThemeManager;
import com.mycompany.nocapgameslauncher.gui.utilities.ThemePanel;
import javax.swing.*;
import java.awt.*;

public class GameCardCreator {
    public static final int CARD_WIDTH = 200;
    public static final int CARD_HEIGHT = 280;
    
    public static JPanel createGameCard(String title, String description, ImageIcon icon) {
        return createGameCard(title, description, icon, -1, null);
    }
    
    public static JPanel createGameCard(String title, String description, ImageIcon icon, int gameId, Runnable onClickAction) {
        ThemePanel card = new ThemePanel(new BorderLayout(10, 10)) {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(CARD_WIDTH, CARD_HEIGHT);
            }
            
            @Override
            public Dimension getMinimumSize() {
                return new Dimension(CARD_WIDTH, CARD_HEIGHT);
            }
            
            @Override
            public Dimension getMaximumSize() {
                return new Dimension(CARD_WIDTH, CARD_HEIGHT);
            }
        };

        card.putClientProperty("gameId", gameId);
        
        // Set fixed size - all three size methods to ensure it stays fixed
        card.setPreferredSize(new Dimension(CARD_WIDTH, CARD_HEIGHT));
        card.setMinimumSize(new Dimension(CARD_WIDTH, CARD_HEIGHT));
        card.setMaximumSize(new Dimension(CARD_WIDTH, CARD_HEIGHT));

        // Fixed image size
        Image scaled = icon.getImage().getScaledInstance(180, 180, Image.SCALE_SMOOTH);
        JLabel imageLabel = new JLabel(new ImageIcon(scaled));
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imageLabel.setPreferredSize(new Dimension(180, 180));
        imageLabel.setMinimumSize(new Dimension(180, 180));
        imageLabel.setMaximumSize(new Dimension(180, 180));
        card.add(imageLabel, BorderLayout.CENTER);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(LightModeToggle.getTextColor());
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(titleLabel, BorderLayout.NORTH);

        // Set initial theme
        card.setBackground(LightModeToggle.getComponentColor());
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(LightModeToggle.getTextColor().darker(), 2),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        
        ThemeManager.addComponent(() -> {
            card.setBackground(LightModeToggle.getComponentColor());
            card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LightModeToggle.getTextColor().darker(), 2),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
            ));
            titleLabel.setForeground(LightModeToggle.getTextColor());
        });
        
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                card.setBackground(LightModeToggle.getAccentColor());
                card.setCursor(onClickAction != null ? new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR) : java.awt.Cursor.getDefaultCursor());
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                card.setBackground(LightModeToggle.getComponentColor());
                card.setCursor(java.awt.Cursor.getDefaultCursor());
            }
            
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (onClickAction != null) {
                    onClickAction.run();
                }
            }
        });

        return card;
    }
}
