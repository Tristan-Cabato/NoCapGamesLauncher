package com.mycompany.nocapgameslauncher.gui.panels;

import com.mycompany.nocapgameslauncher.database.DatabaseHandler;
import com.mycompany.nocapgameslauncher.database.databaseMegaquery;
import com.mycompany.nocapgameslauncher.gui.utilities.LightModeToggle;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.io.*;
import com.mycompany.nocapgameslauncher.database.DatabaseHandler;

public class LoginForm extends JPanel {

    private CardLayout cardLayout;
    private JPanel cardPanel;

    // --- Constants for consistent styling ---
    private static final int FORM_WIDTH = 400; // Width of the centered form elements
    private static final int FIELD_HEIGHT = 45;

    // Inner class for full-screen background panel
    private static class FullScreenPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            
            // Use the codebase's background color
            g2d.setColor(LightModeToggle.getBackgroundColor());
            g2d.fillRect(0, 0, getWidth(), getHeight());

            g2d.dispose();
        }
    }

    public LoginForm(JFrame owner) {
        super(new BorderLayout());
        setBackground(LightModeToggle.getBackgroundColor());
        initializeUI();
    }

    private void initializeUI() {
        // Full-screen background panel with dark gradient
        FullScreenPanel backgroundPanel = new FullScreenPanel();
        backgroundPanel.setLayout(new GridBagLayout());
        
        // Create centered form content
        JPanel formContent = createFormContent();
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        backgroundPanel.add(formContent, gbc);
        
        add(backgroundPanel, BorderLayout.CENTER);
    }

    private JPanel createFormContent() {
        // Simplified vertical layout: logo, tab selector, card panel
        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        // Logo row
        JPanel logoRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        logoRow.setOpaque(false);
        JLabel logo = new JLabel("\uD83C\uDFAE");
        logo.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 70));
        logo.setForeground(LightModeToggle.getAccentColor());
        JLabel title = new JLabel("NO CAP GAMES");
        title.setFont(new Font("Segoe UI", Font.BOLD, 36));
        title.setForeground(LightModeToggle.getTextColor());
        logoRow.add(logo);
        logoRow.add(title);
        content.add(logoRow);
        content.add(Box.createRigidArea(new Dimension(0, 16)));

        // Tabs
        JPanel tabPanel = createTabSelector();
        tabPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(tabPanel);
        content.add(Box.createRigidArea(new Dimension(0, 12)));

        // Card container
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.setOpaque(false);
        cardPanel.add(createModernLoginPanel(), "LOGIN");
        cardPanel.add(createModernRegisterPanel(), "REGISTER");
        content.add(cardPanel);

        return content;
    }

    private JPanel createTabSelector() {
        JPanel tabPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        tabPanel.setOpaque(false);
        tabPanel.setBorder(BorderFactory.createEmptyBorder(6, 0, 6, 0));

    JToggleButton loginTab = new ToggleRoundedButton("SIGN IN");
    JToggleButton registerTab = new ToggleRoundedButton("CREATE ACCOUNT");
        Font tabFont = new Font("Segoe UI", Font.BOLD, 14);
        loginTab.setFont(tabFont);
        registerTab.setFont(tabFont);

        ButtonGroup group = new ButtonGroup();
        group.add(loginTab);
        group.add(registerTab);

        // Baseline styling
        styleTabToggle(loginTab, false);
        styleTabToggle(registerTab, false);

        // Add to panel first so selection paints correctly
        tabPanel.add(loginTab);
        tabPanel.add(registerTab);

        // Default selection
        loginTab.setSelected(true);
        styleTabToggle(loginTab, true);

        loginTab.addActionListener(evt -> {
            // ensure parameter is acknowledged (keeps static analyzers happy)
            if (evt == null) return;
            cardLayout.show(cardPanel, "LOGIN");
            styleTabToggle(registerTab, false);
            styleTabToggle(loginTab, true);
        });

        registerTab.addActionListener(evt -> {
            if (evt == null) return;
            cardLayout.show(cardPanel, "REGISTER");
            styleTabToggle(loginTab, false);
            styleTabToggle(registerTab, true);
        });

        return tabPanel;
    }

    private void styleTabToggle(JToggleButton btn, boolean active) {
        // Simplified state handling: immediate color changes, no timers
        // Remove old mouse listeners to avoid stacking
        for (MouseListener ml : btn.getMouseListeners()) {
            if (ml.getClass().getName().contains("MouseAdapter")) {
                btn.removeMouseListener(ml);
            }
        }

        btn.putClientProperty("isActiveTab", active);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(180, 42));
        // Let the custom ToggleRoundedButton paint its own background
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);

        if (active) {
            // Active tabs use the red highlight color
            btn.setBackground(new Color(220, 53, 69));
            btn.setForeground(Color.WHITE);
        } else {
            btn.setBackground(LightModeToggle.getBackgroundColor().brighter());
            btn.setForeground(LightModeToggle.getTextColor());
        }
        btn.repaint();

        // Simple hover feedback for inactive tabs
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                Boolean isActive = (Boolean) btn.getClientProperty("isActiveTab");
                if (isActive == null || !isActive) {
                    btn.setBackground(LightModeToggle.getAccentColor().brighter());
                    btn.setForeground(Color.WHITE);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                Boolean isActive = (Boolean) btn.getClientProperty("isActiveTab");
                if (isActive == null || !isActive) {
                    btn.setBackground(LightModeToggle.getBackgroundColor().brighter());
                    btn.setForeground(LightModeToggle.getTextColor());
                }
            }
        });
    }

    // (Animation removed) simple immediate color changes suffice for tabs

    // color blending removed with animation cleanup

    private JPanel createModernLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 10, 0); // Reduced from 15 to 10
        
        // Username field
        JTextField userField = createModernTextField("Username");
        gbc.gridy = 0;
        panel.add(createFieldGroup("USERNAME", userField), gbc);
        
        // Password field
        JPasswordField passField = createModernPasswordField("Password");
        gbc.gridy = 1;
        panel.add(createFieldGroup("PASSWORD", passField), gbc);
        
        // Remember me checkbox
        JCheckBox rememberMe = new JCheckBox("Remember me");
        rememberMe.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        rememberMe.setForeground(LightModeToggle.getTextColor());
        rememberMe.setOpaque(false);
        rememberMe.setFocusPainted(false);
        rememberMe.setHorizontalAlignment(SwingConstants.LEFT);
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 15, 0); // Reduced from 25 to 15
        panel.add(rememberMe, gbc);
        
        // Login button
        RoundedButton loginBtn = new RoundedButton("LOGIN", LightModeToggle.getAccentColor());
        loginBtn.addActionListener(e -> {
            if (e != null) {
                String username = userField.getText().trim();
                String password = new String(passField.getPassword());
                handleLogin(username, password);
            }
        });
        gbc.gridy = 3;
        gbc.insets = new Insets(0, 0, 0, 0); // No bottom margin for last element
        panel.add(loginBtn, gbc);
        
        // Enter key support
        ActionListener loginAction = e -> { if (e != null) loginBtn.doClick(); };
        userField.addActionListener(loginAction);
        passField.addActionListener(loginAction);
        
        return panel;
    }

    private JPanel createModernRegisterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 10, 0); // Reduced from 15 to 10
        
        // Input Fields
        JTextField userField = createModernTextField("Choose Username");
        gbc.gridy = 0;
        panel.add(createFieldGroup("CHOOSE A USERNAME", userField), gbc);
        
        JPasswordField passField = createModernPasswordField("Choose Password");
        gbc.gridy = 1;
        panel.add(createFieldGroup("CHOOSE A PASSWORD", passField), gbc);
        
        JPasswordField confirmField = createModernPasswordField("Confirm Password");
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 15, 0); // Reduced from 25 to 15
        panel.add(createFieldGroup("CONFIRM PASSWORD", confirmField), gbc);
        
        // Register button
        RoundedButton registerBtn = new RoundedButton("CREATE ACCOUNT", LightModeToggle.getAccentColor());
        registerBtn.addActionListener(e -> {
            if (e != null) {
                String username = userField.getText().trim();
                String password = new String(passField.getPassword());
                String confirm = new String(confirmField.getPassword());
                handleRegister(username, password, confirm);
            }
        });
        gbc.gridy = 3;
        gbc.insets = new Insets(0, 0, 0, 0); // No bottom margin for last element
        panel.add(registerBtn, gbc);
        
        // Enter key support
        ActionListener registerAction = e -> { if (e != null) registerBtn.doClick(); };
        userField.addActionListener(registerAction);
        passField.addActionListener(registerAction);
        confirmField.addActionListener(registerAction);
        
        return panel;
    }
    
    // Utility to group the label and the field for cleaner code and spacing
    private JPanel createFieldGroup(String labelText, JComponent field) {
        JPanel group = new JPanel(new GridBagLayout());
        group.setOpaque(false);
        group.setBorder(BorderFactory.createEmptyBorder());
        
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(LightModeToggle.getTextColor().darker());
        label.setHorizontalAlignment(SwingConstants.LEFT);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 1.0;
        group.add(label, gbc);
        
        gbc.gridy = 1;
        gbc.insets = new Insets(6, 0, 0, 0); // 6px top padding between label and field
        group.add(field, gbc);
        
        return group;
    }

    private JTextField createModernTextField(String placeholder) {
        JTextField field = new JTextField() {
            // Simple placeholder text implementation
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !isFocusOwner()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setColor(LightModeToggle.getTextColor().darker().darker());
                    g2.setFont(getFont().deriveFont(Font.ITALIC));
                    // Adjust position based on border insets
                    Insets insets = getInsets();
                    g2.drawString(placeholder, insets.left, getHeight() / 2 + g2.getFontMetrics().getAscent() / 2 - 2);
                    g2.dispose();
                }
            }
        };
        field.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        field.setPreferredSize(new Dimension(FORM_WIDTH, FIELD_HEIGHT));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, FIELD_HEIGHT));
        field.setBackground(LightModeToggle.getComponentColor().brighter()); // Slightly lighter background
        field.setForeground(LightModeToggle.getTextColor());
        field.setCaretColor(LightModeToggle.getAccentColor());
        field.setBorder(new CompoundBorder(
            new RoundedBorder(0, LightModeToggle.getComponentColor().darker()),
            BorderFactory.createEmptyBorder(0, 15, 0, 15) // Left/Right padding only
        ));

        // Focus border effect: uses the RoundedBorder class
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                // Thicker, accent-colored border on focus
                field.setBorder(new CompoundBorder(
                    new RoundedBorder(0, LightModeToggle.getAccentColor(), 2), // 2 pixel thickness
                    BorderFactory.createEmptyBorder(0, 15, 0, 15)
                ));
            }

            @Override
            public void focusLost(FocusEvent e) {
                // Revert to thin, muted border
                field.setBorder(new CompoundBorder(
                    new RoundedBorder(0, LightModeToggle.getComponentColor().darker(), 1), // 1 pixel thickness
                    BorderFactory.createEmptyBorder(0, 15, 0, 15)
                ));
            }
        });

        return field;
    }

    private JPasswordField createModernPasswordField(String placeholder) {
        // Uses the same styling as JTextField
        JPasswordField field = new JPasswordField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        field.setPreferredSize(new Dimension(FORM_WIDTH, FIELD_HEIGHT));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, FIELD_HEIGHT));
        field.setBackground(LightModeToggle.getComponentColor().brighter());
        field.setForeground(LightModeToggle.getTextColor());
        field.setCaretColor(LightModeToggle.getAccentColor());
        field.setBorder(new CompoundBorder(
            new RoundedBorder(0, LightModeToggle.getComponentColor().darker()),
            BorderFactory.createEmptyBorder(0, 15, 0, 15)
        ));

        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                field.setBorder(new CompoundBorder(
                    new RoundedBorder(0, LightModeToggle.getAccentColor(), 2),
                    BorderFactory.createEmptyBorder(0, 15, 0, 15)
                ));
            }

            @Override
            public void focusLost(FocusEvent e) {
                field.setBorder(new CompoundBorder(
                    new RoundedBorder(0, LightModeToggle.getComponentColor().darker(), 1),
                    BorderFactory.createEmptyBorder(0, 15, 0, 15)
                ));
            }
        });

        return field;
    }

    private void handleLogin(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            showModernError("Please enter both username and password");
            return;
        }

        try {
            boolean ok = DatabaseHandler.login(username, password);
            if (ok) {
                // Set the current user in DatabaseHandler
                DatabaseHandler.setCurrentUser(username);
                SwingUtilities.getWindowAncestor(this).dispose();
                
                if (username.equals("Admin")) {
                    // Admin login - show database management window
                    databaseMegaquery megaquery = new databaseMegaquery();
                    megaquery.setVisible(true);
                } else {
                    // Regular user login - create and show main frame
                    SwingUtilities.invokeLater(() -> {
                        com.mycompany.nocapgameslauncher.gui.mainFrame frame = 
                            new com.mycompany.nocapgameslauncher.gui.mainFrame();
                        frame.setVisible(true);
                    });
                }
            } else {
                showModernError("Invalid username or password");
            }
        } catch (SQLException |IOException ex) {
            showModernError("Database connection error:\n" + ex.getMessage());
        }
    }

    private void handleRegister(String username, String password, String confirm) {
        if (username.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            showModernError("All fields are required");
            return;
        }

        if (!password.equals(confirm)) {
            showModernError("Passwords do not match");
            return;
        }

        if (password.length() < 6) {
            showModernError("Password must be at least 6 characters");
            return;
        }

        try {
            boolean created = DatabaseHandler.register(username, password);
            if (created) {
                showModernSuccess("Account created successfully!\nYou can now sign in.");
                cardLayout.show(cardPanel, "LOGIN");
            } else {
                showModernError("Username already exists");
            }
        } catch (SQLException ex) {
            showModernError("Database connection error:\n" + ex.getMessage());
        }
    }

    // --- Custom UI Components (Nested Classes) ---

    // Simple rounded border implementation with adjustable thickness
    private static class RoundedBorder implements Border {
        private final int radius;
        private final Color lineColor;
        private final int thickness;

        public RoundedBorder(int radius, Color lineColor) {
            this(radius, lineColor, 1);
        }

        public RoundedBorder(int radius, Color lineColor, int thickness) {
            this.radius = radius;
            this.lineColor = lineColor;
            this.thickness = thickness;
        }

        @Override
        public Insets getBorderInsets(Component c) {
            // Adjust insets based on thickness for proper content padding
            return new Insets(thickness, thickness, thickness, thickness);
        }

        @Override
        public boolean isBorderOpaque() {
            return false;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(lineColor);
            g2.setStroke(new BasicStroke(thickness));
            // Draw the border slightly inside the bounds
            g2.drawRoundRect(x + thickness / 2, y + thickness / 2, width - thickness, height - thickness, radius, radius);
            g2.dispose();
        }
    }

    // A custom JButton that draws a rounded background with smooth animations
    private static class RoundedButton extends JButton {
        private final int radius = 12;
        private Color currentColor;
        private Color targetColor;
        private Timer animationTimer;

        public RoundedButton(String text, Color primaryColor) {
            super(text);
            this.currentColor = primaryColor;
            this.targetColor = primaryColor;
            
            setFont(new Font("Segoe UI", Font.BOLD, 16));
            setPreferredSize(new Dimension(FORM_WIDTH, FIELD_HEIGHT));
            setMaximumSize(new Dimension(FORM_WIDTH, FIELD_HEIGHT));
            setForeground(Color.WHITE);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            // Smooth color transition animation
            animationTimer = new Timer(20, evt -> {
                if (evt != null && !currentColor.equals(targetColor)) {
                    currentColor = blendColors(currentColor, targetColor, 0.2f);
                    repaint();
                    if (Math.abs(currentColor.getRed() - targetColor.getRed()) < 2) {
                        currentColor = targetColor;
                        animationTimer.stop();
                    }
                }
            });

            // Enhanced hover/press effects with smooth transitions
            addMouseListener(new MouseAdapter() {
                Color baseColor = currentColor; // Store initial color
                
                @Override
                public void mouseEntered(MouseEvent e) {
                    targetColor = brighten(baseColor, 1.15f);
                    animationTimer.start();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    targetColor = baseColor;
                    animationTimer.start();
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    targetColor = darken(baseColor, 0.85f);
                    currentColor = targetColor; // Instant feedback on press
                    repaint();
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    targetColor = brighten(baseColor, 1.15f);
                    animationTimer.start();
                }
            });
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            // Draw subtle outer glow when hovered
            if (getModel().isRollover()) {
                g2.setColor(new Color(currentColor.getRed(), currentColor.getGreen(), currentColor.getBlue(), 60));
                g2.fillRoundRect(-2, -2, getWidth() + 4, getHeight() + 4, radius + 4, radius + 4);
            }

            // Draw the rounded background with gradient
            GradientPaint gradient = new GradientPaint(
                0, 0, currentColor,
                0, getHeight(), darken(currentColor, 0.92f)
            );
            g2.setPaint(gradient);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);

            // Subtle highlight on top edge
            g2.setColor(new Color(255, 255, 255, 30));
            g2.fillRoundRect(0, 0, getWidth(), getHeight() / 3, radius, radius);

            g2.dispose();
            super.paintComponent(g);
        }

        private Color blendColors(Color c1, Color c2, float ratio) {
            int r = (int) (c1.getRed() + (c2.getRed() - c1.getRed()) * ratio);
            int gr = (int) (c1.getGreen() + (c2.getGreen() - c1.getGreen()) * ratio);
            int b = (int) (c1.getBlue() + (c2.getBlue() - c1.getBlue()) * ratio);
            return new Color(
                Math.max(0, Math.min(255, r)),
                Math.max(0, Math.min(255, gr)),
                Math.max(0, Math.min(255, b))
            );
        }

        private Color brighten(Color c, float factor) {
            return new Color(
                Math.min(255, (int)(c.getRed() * factor)),
                Math.min(255, (int)(c.getGreen() * factor)),
                Math.min(255, (int)(c.getBlue() * factor))
            );
        }

        private Color darken(Color c, float factor) {
            return new Color(
                (int)(c.getRed() * factor),
                (int)(c.getGreen() * factor),
                (int)(c.getBlue() * factor)
            );
        }
    }

    // ToggleRoundedButton - a compact rounded toggle button styled like the main sign-in button
    private static class ToggleRoundedButton extends JToggleButton {
        private final int radius = 10;

        public ToggleRoundedButton(String text) {
            super(text);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setForeground(Color.WHITE);
            setFont(new Font("Segoe UI", Font.BOLD, 13));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(180, 42));
            setHorizontalAlignment(SwingConstants.CENTER);
            setRolloverEnabled(true);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color bg = getBackground();
            if (bg == null) bg = LightModeToggle.getAccentColor();
            // Gradient similar to RoundedButton
            GradientPaint gradient = new GradientPaint(0, 0, bg, 0, getHeight(), darken(bg, 0.92f));
            g2.setPaint(gradient);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);

            // Slight top highlight
            g2.setColor(new Color(255, 255, 255, 30));
            g2.fillRoundRect(0, 0, getWidth(), getHeight() / 3, radius, radius);

            // Outer glow when hovered
            if (getModel().isRollover()) {
                g2.setColor(new Color(bg.getRed(), bg.getGreen(), bg.getBlue(), 60));
                g2.fillRoundRect(-2, -2, getWidth() + 4, getHeight() + 4, radius + 4, radius + 4);
            }

            g2.dispose();
            super.paintComponent(g);
        }

        private Color darken(Color c, double factor) {
            return new Color(
                (int) Math.max(0, Math.min(255, c.getRed() * factor)),
                (int) Math.max(0, Math.min(255, c.getGreen() * factor)),
                (int) Math.max(0, Math.min(255, c.getBlue() * factor))
            );
        }
    }
    
    // --- Message Dialogs (Unchanged) ---

    private void showModernError(String message) {
        // Use a more visual/accessible JDialog in a real app, but for simplicity, keep JOptionPane here.
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showModernSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }
}
