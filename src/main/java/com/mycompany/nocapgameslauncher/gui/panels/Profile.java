package com.mycompany.nocapgameslauncher.gui.panels;

import com.mycompany.nocapgameslauncher.gui.mainFrame;
import com.mycompany.nocapgameslauncher.gui.utilities.LightModeToggle;
import com.mycompany.nocapgameslauncher.gui.utilities.ThemeButton;
import com.mycompany.nocapgameslauncher.gui.utilities.ThemePanel;

import javax.swing.*;

import java.awt.*;

import com.mycompany.nocapgameslauncher.NoCapGamesLauncher;

public class Profile extends ThemePanel {

    public Profile(mainFrame frame) {
        super(new GridLayout(0, 1));
        setBorder(BorderFactory.createLineBorder(LightModeToggle.getComponentColor()));

        ThemeButton signInButton = createProfileButton("Sign In");
        signInButton.addActionListener(_ -> {
            JOptionPane.showMessageDialog(this, "Sign In clicked!");
        });

        ThemeButton changeAccountButton = createProfileButton("Change Account");
        changeAccountButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Change Account clicked!");
        });

        add(signInButton);
        add(changeAccountButton);
        updateTheme();
    }

    private ThemeButton createProfileButton(String text) {
        ThemeButton button = new ThemeButton(text);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setContentAreaFilled(false);
        button.setOpaque(true);
        button.setHorizontalAlignment(SwingConstants.LEFT);

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(LightModeToggle.getAccentColor());
                button.setForeground(Color.WHITE);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(LightModeToggle.getComponentColor());
                button.setForeground(LightModeToggle.getTextColor());
            }
        });

        return button;
    }

    @Override
    public void updateTheme() {
        super.updateTheme();
        setBorder(BorderFactory.createLineBorder(LightModeToggle.getComponentColor()));
    }
}