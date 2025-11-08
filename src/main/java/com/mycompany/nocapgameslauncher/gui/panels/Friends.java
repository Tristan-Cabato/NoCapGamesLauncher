package com.mycompany.nocapgameslauncher.gui.panels;

import com.mycompany.nocapgameslauncher.gui.utilities.LightModeToggle;
import com.mycompany.nocapgameslauncher.gui.utilities.ThemeManager;
import com.mycompany.nocapgameslauncher.gui.utilities.ThemePanel;
import com.mycompany.nocapgameslauncher.gui.mainFrame;
import com.mycompany.nocapgameslauncher.gui.utilities.FontManager;
import com.mycompany.nocapgameslauncher.iterator.UsersIterator;
import com.mycompany.nocapgameslauncher.iterator.SessionIterator;
import com.mycompany.nocapgameslauncher.iterator.LibraryGameIterator;
import com.mycompany.nocapgameslauncher.userManager.UserGameData;
import com.mycompany.nocapgameslauncher.userManager.UserRepository;
import com.mycompany.nocapgameslauncher.userManager.UserMemento;
import com.mycompany.nocapgameslauncher.resourceHandling.GameCardData;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class Friends extends ThemePanel {
    private JPanel usersPanel;
    private JScrollPane scrollPane;
    private mainFrame frame;
    private UserRepository userRepo;
    private String currentUsername;
    private Set<String> friendsSet;

    public Friends(mainFrame frame) {
        super(new BorderLayout());
        this.frame = frame;
        setupUI();
    }

    public Friends(mainFrame frame, String startPanel) {
        this(frame);
        ThemeManager.updateTheme();
        if (startPanel != null && !startPanel.isEmpty() && !startPanel.equals("LOGIN")) {
            frame.showCard(startPanel);
        }
    }

    private void setupUI() {
        setLayout(new BorderLayout());
        userRepo = new UserRepository();

        // Get current user
        UserMemento currentMemento = SessionIterator.getCurrentMemento();
        currentUsername = currentMemento != null ? currentMemento.getUsername() : "";

        // Initialize friends set
        friendsSet = new HashSet<>();
        if (currentMemento != null) {
            UserGameData userData = userRepo.loadUser(currentUsername);
            if (userData != null) {
                friendsSet = userData.getFriendList().stream()
                    .map(userRepo::getUserById)
                    .filter(Objects::nonNull)
                    .map(UserGameData::getUsername)
                    .collect(Collectors.toSet());
            }
        }

        // Create title
        JLabel titleLabel = new JLabel("Friends");
        FontManager.setFont(titleLabel, Font.BOLD, 24);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        add(titleLabel, BorderLayout.NORTH);

        // Create users panel with vertical box layout
        usersPanel = new JPanel();
        usersPanel.setLayout(new BoxLayout(usersPanel, BoxLayout.Y_AXIS));
        usersPanel.setOpaque(false);

        // Add scroll pane
        scrollPane = new JScrollPane(usersPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getViewport().setOpaque(false);
        add(scrollPane, BorderLayout.CENTER);

        // Load and display users
        loadAndDisplayUsers();
        updateTheme();
    }

    private void loadAndDisplayUsers() {
        usersPanel.removeAll();

        // Add current user's friends first
        if (currentUsername.isEmpty()) {
            JLabel loginLabel = new JLabel("Please log in to see friends");
            loginLabel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
            usersPanel.add(loginLabel);
            usersPanel.revalidate();
            usersPanel.repaint();
            return;
        }

        // Get all users except current user using iterator
        UsersIterator usersIterator = new UsersIterator();
        boolean hasUsers = false;
        String previousUsername = null;

        while (usersIterator.hasNext()) {
            String username = usersIterator.next();
            if (!username.equals(currentUsername)) {
                usersPanel.add(createUserPanel(username));
                hasUsers = true;
            }
        }

        if (!hasUsers) {
            JLabel noUsersLabel = new JLabel("No other users available.");
            noUsersLabel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
            usersPanel.add(noUsersLabel);
        }

        usersPanel.revalidate();
        usersPanel.repaint();
    }

    private JPanel createUserPanel(String username) {
        boolean isFriend = friendsSet.contains(username);

        // Main panel for the user
        JPanel userPanel = new JPanel(new BorderLayout(10, 0));
        userPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(LightModeToggle.getComponentColor().darker(), 1),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        userPanel.setOpaque(true);
        userPanel.setBackground(LightModeToggle.getComponentColor());

        // Username label
        JLabel nameLabel = new JLabel(username);
        FontManager.setFont(nameLabel, Font.BOLD, 16);
        nameLabel.setForeground(LightModeToggle.getTextColor());

        // Dropdown button for games
        JButton gamesButton = new JButton("Show Games");
        gamesButton.setFocusPainted(false);
        gamesButton.setContentAreaFilled(false);
        gamesButton.setBorderPainted(false);
        gamesButton.setForeground(LightModeToggle.getTextColor());

        // Panel for games dropdown
        JPanel gamesPanel = new JPanel();
        gamesPanel.setLayout(new BoxLayout(gamesPanel, BoxLayout.Y_AXIS));
        gamesPanel.setVisible(false);
        gamesPanel.setOpaque(false);

        // Toggle games panel visibility
        gamesButton.addActionListener(e -> {
            if (!gamesPanel.isVisible()) {
                // Load games for this user
                loadUserGames(username, gamesPanel);
            }
            gamesPanel.setVisible(!gamesPanel.isVisible());
            gamesButton.setText(gamesPanel.isVisible() ? "Hide Games" : "Show Games");
            userPanel.revalidate();
            userPanel.repaint();
        });

        // Add/remove friend button
        JButton friendButton = new JButton(isFriend ? "Remove Friend" : "Add Friend");
        friendButton.setFocusPainted(false);
        friendButton.setBackground(isFriend ? new Color(255, 100, 100) : new Color(100, 200, 100));
        friendButton.setForeground(Color.WHITE);
        friendButton.setOpaque(true);
        friendButton.setBorderPainted(false);

        friendButton.addActionListener(e -> {
            toggleFriendStatus(username, friendButton);
        });

        // Top panel with username and action buttons
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftPanel.setOpaque(false);
        leftPanel.add(nameLabel);
        leftPanel.add(gamesButton);

        topPanel.add(leftPanel, BorderLayout.WEST);
        topPanel.add(friendButton, BorderLayout.EAST);

        // Assemble the user panel
        userPanel.add(topPanel, BorderLayout.NORTH);
        userPanel.add(gamesPanel, BorderLayout.CENTER);

        return userPanel;
    }

    private void loadUserGames(String username, JPanel gamesPanel) {
        gamesPanel.removeAll();

        // Find the target user using UsersIterator
        UsersIterator usersIterator = new UsersIterator();
        UserGameData targetUser = null;
        
        while (usersIterator.hasNext()) {
            String currentUsername = usersIterator.next();
            if (currentUsername.equals(username)) {
                targetUser = userRepo.loadUser(username);
                break;
            }
        }

        if (targetUser == null || targetUser.getOwnedGameIds() == null || targetUser.getOwnedGameIds().isEmpty()) {
            JLabel noGamesLabel = new JLabel("  No games in library");
            noGamesLabel.setForeground(LightModeToggle.getTextColor().darker());
            gamesPanel.add(noGamesLabel);
            return;
        }

        // Convert game IDs to integers
        List<Integer> gameIds = targetUser.getOwnedGameIds().stream()
            .map(d -> d.intValue())
            .collect(Collectors.toList());

        // Create game iterator and add game labels
        LibraryGameIterator gameIterator = new LibraryGameIterator(gameIds);
        while (gameIterator.hasNext()) {
            GameCardData gameData = gameIterator.next();
            if (gameData != null) {
                JLabel gameLabel = new JLabel("  • " + gameData.title);
                gameLabel.setForeground(LightModeToggle.getTextColor());
                gameLabel.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 5));
                gamesPanel.add(gameLabel);
            }
        }
    }

    private void toggleFriendStatus(String username, JButton friendButton) {
        if (currentUsername.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please log in to manage friends.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            UserGameData currentUser = userRepo.loadUser(currentUsername);
            if (currentUser == null) {
                throw new Exception("Current user not found");
            }

            // Find target user
            UsersIterator usersIterator = new UsersIterator();
            UserGameData targetUser = null;
            final String targetUsername = username; // Make final for lambda
            
            while (usersIterator.hasNext()) {
                String currentUsername = usersIterator.next();
                if (currentUsername.equals(targetUsername)) {
                    targetUser = userRepo.loadUser(targetUsername);
                    break;
                }
            }

            if (targetUser == null) {
                throw new Exception("Target user not found");
            }

            if (friendsSet.contains(username)) {
                // Remove friend using the proper method
                currentUser.removeFriend(targetUser.getUserID());
                userRepo.saveUser(currentUser);
                friendsSet.remove(username);
                friendButton.setText("Add Friend");
                friendButton.setBackground(new Color(100, 200, 100));
                friendButton.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        friendButton.setBackground(new Color(150, 250, 150));
                    }
                    @Override
                    public void mouseExited(MouseEvent e) {
                        friendButton.setBackground(new Color(100, 200, 100));
                    }
                });
            } else {
                // Add friend using the proper method
                currentUser.addFriend(targetUser.getUserID());
                userRepo.saveUser(currentUser);
                friendsSet.add(username);
                friendButton.setText("Remove Friend");
                friendButton.setBackground(new Color(255, 100, 100));
                friendButton.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        friendButton.setBackground(new Color(255, 150, 150));
                    }
                    @Override
                    public void mouseExited(MouseEvent e) {
                        friendButton.setBackground(new Color(255, 100, 100));
                    }
                });
            }
            loadAndDisplayUsers();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Failed to update friend status: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        } loadAndDisplayUsers();
    }

    @Override
    public void updateTheme() {
        // Update the main panel
        setBackground(LightModeToggle.getBackgroundColor());
        
        // Update scroll pane if it exists
        if (scrollPane != null) {
            scrollPane.setBackground(LightModeToggle.getBackgroundColor());
            scrollPane.getViewport().setBackground(LightModeToggle.getBackgroundColor());
            scrollPane.setBorder(BorderFactory.createLineBorder(LightModeToggle.getComponentColor().darker()));
        }
        
        // Update users panel
        if (usersPanel != null) {
            usersPanel.setBackground(LightModeToggle.getBackgroundColor());
            
            // Update all user panels
            for (Component comp : usersPanel.getComponents()) {
                if (comp instanceof JPanel) {
                    JPanel userPanel = (JPanel) comp;
                    userPanel.setBackground(LightModeToggle.getComponentColor());
                    userPanel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(LightModeToggle.getComponentColor().darker(), 1),
                        BorderFactory.createEmptyBorder(10, 15, 10, 15)
                    ));
                    
                    // Update all components in the user panel
                    updateComponentTheme(userPanel);
                }
            }
        }
        
        // Force a repaint
        revalidate();
        repaint();
    }
    
    private void updateComponentTheme(Container container) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JLabel) {
                ((JLabel) comp).setForeground(LightModeToggle.getTextColor());
            } else if (comp instanceof JButton) {
                JButton button = (JButton) comp;
                String text = button.getText();
                if ("Show Games".equals(text) || "Hide Games".equals(text)) {
                    button.setForeground(LightModeToggle.getTextColor());
                } else if ("Add Friend".equals(text) || "Remove Friend".equals(text)) {
                    button.setForeground(Color.WHITE);
                    button.setBackground(text.equals("Add Friend") ? 
                        new Color(100, 200, 100) : new Color(200, 100, 100));
                }
            } else if (comp instanceof JPanel) {
                ((JPanel) comp).setOpaque(false);
                updateComponentTheme((JPanel) comp);
            }
        }
    }
}