package com.mycompany.nocapgameslauncher.gui.panels;

import com.mycompany.nocapgameslauncher.gui.utilities.LightModeToggle;
import com.mycompany.nocapgameslauncher.gui.utilities.ThemeManager;
import com.mycompany.nocapgameslauncher.gui.utilities.ThemePanel;
import com.mycompany.nocapgameslauncher.gui.utilities.ThemeButton;
import com.mycompany.nocapgameslauncher.gui.utilities.FontManager;
import com.mycompany.nocapgameslauncher.iterator.UsersIterator;
import com.mycompany.nocapgameslauncher.iterator.SessionIterator;
import com.mycompany.nocapgameslauncher.iterator.LibraryGameIterator;
import com.mycompany.nocapgameslauncher.userManager.UserGameData;
import com.mycompany.nocapgameslauncher.userManager.UserRepository;
import com.mycompany.nocapgameslauncher.userManager.UserMemento;
import com.mycompany.nocapgameslauncher.resourceHandling.GameCardData;
import com.mycompany.nocapgameslauncher.gui.mainFrame;

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
    private final UserRepository userRepo;
    private String currentUsername;
    private Set<String> friendsSet;
    private final mainFrame frame;
    private JLabel titleLabel;

    public Friends(mainFrame frame) {
        super(new BorderLayout());
        this.frame = frame;
        this.userRepo = new UserRepository();
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
        
        // Initialize UI components first
        titleLabel = new JLabel("Friends");
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

        // Get current user
        UserMemento currentMemento = SessionIterator.getCurrentMemento();
        currentUsername = currentMemento != null ? currentMemento.getUsername() : "";

        // Initialize friends set
        friendsSet = new HashSet<>();
        if (currentMemento != null && !currentUsername.isEmpty()) {
            UserGameData currentUser = userRepo.loadUser(currentUsername);
            if (currentUser != null) {
                // Get all users to map IDs to usernames
                Map<Integer, String> userIdToUsername = new HashMap<>();
                UsersIterator allUsers = new UsersIterator();
                while (allUsers.hasNext()) {
                    String u = allUsers.next();
                    UserGameData user = userRepo.loadUser(u);
                    if (user != null) {
                        userIdToUsername.put(user.getUserID(), u);
                    }
                }
                
                // Populate friendsSet with usernames of friends
                for (Integer friendId : currentUser.getFriendList()) {
                    String friendUsername = userIdToUsername.get(friendId);
                    if (friendUsername != null) {
                        friendsSet.add(friendUsername);
                    } else {
                        System.out.println("Warning: Could not find username for friend ID: " + friendId);
                    }
                }
            }
        }

        // Now load and display users
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

        // Get all users except current user and Admin using iterator
        UsersIterator usersIterator = new UsersIterator();
        boolean hasUsers = false;
        int userCount = 0;
        final int MAX_USERS_PER_PAGE = 10; // Adjust this number as needed

        // Create a wrapper panel for better scrolling
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);

        while (usersIterator.hasNext() && userCount < MAX_USERS_PER_PAGE) {
            String username = usersIterator.next();
            // Skip current user and Admin user (case-insensitive check)
            if (!username.equals(currentUsername) && !username.equals("Admin")) {
                contentPanel.add(createUserPanel(username));
                hasUsers = true;
                userCount++;
            }
        }
        
        // Add the content panel to usersPanel with glue for proper spacing
        usersPanel.removeAll();
        usersPanel.setLayout(new BorderLayout());
        usersPanel.add(contentPanel, BorderLayout.NORTH);
        usersPanel.add(Box.createVerticalGlue(), BorderLayout.CENTER);

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
        ThemePanel userPanel = new ThemePanel(new BorderLayout(10, 0));
        userPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        userPanel.setBackground(isFriend ? LightModeToggle.getComponentColor() : LightModeToggle.getBackgroundColor());

        // Username label
        JLabel nameLabel = new JLabel(username);
        FontManager.setFont(nameLabel, Font.BOLD, 16);
        ThemeManager.addComponent(new ThemeManager.Themeable() {
            @Override
            public void updateTheme() {
                nameLabel.setForeground(LightModeToggle.getTextColor());
            }
        });

        // Dropdown button for games
        ThemeButton gamesButton = new ThemeButton("Show Games", false, true, LightModeToggle.getComponentColor(), false);
        gamesButton.setBorderPainted(false);
        gamesButton.setContentAreaFilled(false);
        gamesButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

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
        ThemeButton friendButton = new ThemeButton(
            isFriend ? "Remove Friend" : "Add Friend",
            false,
            true,
            Color.WHITE,
            true
        );
        friendButton.setBackground(isFriend ? new Color(255, 100, 100) : new Color(100, 200, 100));
        friendButton.setOpaque(true);
        friendButton.setBorderPainted(false);
        friendButton.setForeground(Color.WHITE);

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
            
            // Register for theme updates
            ThemeManager.addComponent(new ThemeManager.Themeable() {
                @Override
                public void updateTheme() {
                    noGamesLabel.setForeground(LightModeToggle.getTextColor().darker());
                }
            });
            
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
                
                // Register the label to update with theme changes
                ThemeManager.addComponent(new ThemeManager.Themeable() {
                    @Override
                    public void updateTheme() {
                        gameLabel.setForeground(LightModeToggle.getTextColor());
                    }
                });
                
                gamesPanel.add(gameLabel);
            }
        }
        
        // Make sure the panel updates
        gamesPanel.revalidate();
        gamesPanel.repaint();
    }

    private void toggleFriendStatus(String username, JButton friendButton) {
        if (currentUsername.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please log in to manage friends.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // Load current user data
            UserGameData currentUser = userRepo.loadUser(currentUsername);
            if (currentUser == null) {
                throw new Exception("Current user not found");
            }

            // Load target user data
            UserGameData targetUser = userRepo.loadUser(username);
            if (targetUser == null) {
                throw new Exception("Target user not found");
            }

            int targetId = targetUser.getUserID();
            boolean isNowFriend;
            
            // Check current friend status by ID
            boolean isCurrentlyFriend = currentUser.getFriendList().contains(targetId);
            
            if (isCurrentlyFriend) {
                // Remove friend
                currentUser.removeFriend(targetId);
                friendsSet.remove(username);
                isNowFriend = false;
            } else {
                // Add friend
                currentUser.addFriend(targetId);
                friendsSet.add(username);
                isNowFriend = true;
            }
            
            // Save changes
            userRepo.saveUser(currentUser);
            
            // Update UI
            friendButton.setText(isNowFriend ? "Remove Friend" : "Add Friend");
            friendButton.setBackground(isNowFriend 
                ? new Color(255, 100, 100) 
                : new Color(100, 200, 100));
            friendButton.setForeground(Color.WHITE);

            // Update the parent panel
            JPanel userPanel = (JPanel) friendButton.getParent().getParent();
            userPanel.setBackground(isNowFriend 
                ? LightModeToggle.getComponentColor().brighter() 
                : LightModeToggle.getComponentColor());
                
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Failed to update friend status: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void updateTheme() {
        super.updateTheme(); // Let ThemePanel handle the basics
        
        // Set the main panel's background
        setBackground(LightModeToggle.getComponentColor());

        if (titleLabel != null) {
            titleLabel.setForeground(LightModeToggle.getTextColor());
        }
        
        if (scrollPane != null) {
            scrollPane.setBackground(LightModeToggle.getComponentColor());
            scrollPane.getViewport().setBackground(LightModeToggle.getComponentColor());
            scrollPane.setBorder(BorderFactory.createLineBorder(LightModeToggle.getComponentColor().darker()));
        }
        
        if (usersPanel != null) {
            usersPanel.setBackground(LightModeToggle.getComponentColor());
            
            for (Component comp : usersPanel.getComponents()) {
                if (comp instanceof JPanel) {
                    JPanel userPanel = (JPanel) comp;
                    boolean isFriend = friendsSet.stream()
                        .anyMatch(name -> {
                            for (Component c : userPanel.getComponents()) {
                                if (c instanceof JLabel && ((JLabel) c).getText().equals(name)) {
                                    return true;
                                }
                            }
                            return false;
                        });
                    
                    userPanel.setBackground(isFriend 
                        ? LightModeToggle.getComponentColor().brighter() 
                        : LightModeToggle.getComponentColor());
                    
                    // Update friend button colors
                    for (Component c : userPanel.getComponents()) {
                        if (c instanceof JButton) {
                            JButton button = (JButton) c;
                            if (button.getText().equals("Remove Friend")) {
                                button.setBackground(new Color(255, 100, 100)); // Red for remove
                                button.setForeground(Color.WHITE);
                                button.setOpaque(true);
                            } else if (button.getText().equals("Add Friend")) {
                                button.setBackground(new Color(100, 200, 100)); // Green for add
                                button.setForeground(Color.WHITE);
                                button.setOpaque(true);
                            }
                        }
                    }
                }
            }
        }
    }
}