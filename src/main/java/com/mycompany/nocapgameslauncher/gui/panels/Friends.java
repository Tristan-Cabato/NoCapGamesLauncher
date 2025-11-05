package com.mycompany.nocapgameslauncher.gui.panels;

import com.mycompany.nocapgameslauncher.gui.mainFrame;
import com.mycompany.nocapgameslauncher.gui.utilities.LightModeToggle;
import com.mycompany.nocapgameslauncher.gui.utilities.ThemePanel;
import com.mycompany.nocapgameslauncher.gui.utilities.FontManager;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import com.mycompany.nocapgameslauncher.iterator.SessionIterator;
import com.mycompany.nocapgameslauncher.iterator.UsersIterator;
import com.mycompany.nocapgameslauncher.iterator.LibraryGameIterator;
import com.mycompany.nocapgameslauncher.userManager.UserMemento;
import com.mycompany.nocapgameslauncher.resourceHandling.GameCardData;
import com.mycompany.nocapgameslauncher.userManager.UserGameData;
import com.mycompany.nocapgameslauncher.userManager.UserRepository;

public class Friends extends ThemePanel {
    private JList<String> friendList;
    private JList<String> allUsersList;
    private DefaultListModel<String> friendsModel;
    private DefaultListModel<String> allUsersModel;
    private JLabel friendsLabel;
    private JLabel allUsersLabel;

    public Friends(mainFrame frame) {
        super(new BorderLayout());
        setupUI();
    }

    private void setupUI() {
        // Main panel with vertical box layout to hold both lists
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setOpaque(false);
        
        // Friends List Section
        friendsLabel = new JLabel("Friends");
        FontManager.setFont(friendsLabel, Font.BOLD, 18);
        friendsLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        mainPanel.add(friendsLabel);
        
        // Friends list
        friendsModel = new DefaultListModel<>();
        updateFriendsList();
        
        friendList = createStyledList(friendsModel);
        JScrollPane friendsScrollPane = new JScrollPane(friendList);
        friendsScrollPane.setBorder(BorderFactory.createLineBorder(LightModeToggle.getComponentColor()));
        mainPanel.add(friendsScrollPane);
        
        // Add some space between the lists
        mainPanel.add(Box.createVerticalStrut(20));
        
        // All Users Section
        allUsersLabel = new JLabel("All Users");
        FontManager.setFont(allUsersLabel, Font.BOLD, 18);
        allUsersLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        mainPanel.add(allUsersLabel);
        
        // All users list
        allUsersModel = new DefaultListModel<>();
        UsersIterator allUsersIterator = new UsersIterator();
        if (!allUsersIterator.hasNext()) {
            allUsersModel.addElement("No users available.");
        } else {
            while (allUsersIterator.hasNext()) {
                allUsersModel.addElement(allUsersIterator.next());
            }
        }
        
        allUsersList = createStyledList(allUsersModel);
        JScrollPane allUsersScrollPane = new JScrollPane(allUsersList);
        allUsersScrollPane.setBorder(BorderFactory.createLineBorder(LightModeToggle.getComponentColor()));
        mainPanel.add(allUsersScrollPane);
        
        // Add the main panel to the center
        add(mainPanel, BorderLayout.CENTER);
        
        // Apply theme
        updateTheme();
    }
    
    private JList<String> createStyledList(DefaultListModel<String> model) {
        JList<String> list = new JList<String>(model) {
            private JWindow popup;
            
            {
                // Mouse click listener to show user games
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        int index = locationToIndex(e.getPoint());
                        if (index != -1) {
                            String value = model.getElementAt(index);
                            if (!value.startsWith("No ")) {
                                // Toggle popup on click
                                if (popup != null && popup.isVisible()) {
                                    hideUserGames();
                                } else {
                                    showUserGames(value);
                                }
                            }
                        }
                    }
                });
            }
            
            private void showUserGames(String username) {
                Graphics g = getGraphics();
                hideUserGames();
                
                // Get user's owned games
                UserRepository userRepo = new UserRepository();
                UserGameData userData = userRepo.loadUser(username);
                if (userData == null || userData.getOwnedGameIds().isEmpty()) {
                    return;
                }
                
                // Create popup window
                popup = new JWindow();
                popup.setFocusableWindowState(false);
                
                // Create content panel
                JPanel content = new JPanel();
                content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
                content.setBorder(BorderFactory.createLineBorder(LightModeToggle.getComponentColor().darker()));
                content.setBackground(LightModeToggle.getComponentColor());
                
                // Add title
                JLabel title = new JLabel(username + "'s Games");
                JSeparator separator = new JSeparator();
                separator.setForeground(LightModeToggle.getComponentColor().darker());
                separator.setMaximumSize(new Dimension(getWidth(), 1));
                title.setFont(title.getFont().deriveFont(Font.BOLD));
                title.setForeground(LightModeToggle.getTextColor());
                content.add(title);
                content.add(separator);
                
                // Add games list
                JPanel gamesPanel = new JPanel();
                gamesPanel.setLayout(new BoxLayout(gamesPanel, BoxLayout.Y_AXIS));
                gamesPanel.setBackground(LightModeToggle.getComponentColor());
                
                // Get and display games
                LibraryGameIterator gameIterator = new LibraryGameIterator(new ArrayList<>(userData.getOwnedGameIds()));
                while (gameIterator.hasNext()) {
                    GameCardData game = gameIterator.next();
                    if (game != null) {
                        JLabel gameLabel = new JLabel("• " + game.title);
                        gameLabel.setBorder(BorderFactory.createEmptyBorder(2, 20, 2, 10));
                        gameLabel.setForeground(LightModeToggle.getTextColor());
                        gamesPanel.add(gameLabel);
                    }
                }
                
                JScrollPane scrollPane = new JScrollPane(gamesPanel);
                scrollPane.setBorder(null);
                scrollPane.getViewport().setBackground(LightModeToggle.getComponentColor());
                scrollPane.setPreferredSize(new Dimension(200, 150));
                
                content.add(scrollPane);
                popup.setContentPane(content);
                popup.pack();
                
                // Position popup near the cursor
                Point mouseLoc = MouseInfo.getPointerInfo().getLocation();
                
                // Get screen dimensions to ensure popup stays on screen
                GraphicsConfiguration gc = getGraphicsConfiguration();
                Rectangle screenBounds = gc.getBounds();
                int x = mouseLoc.x + 15;  // Small offset from cursor
                int y = mouseLoc.y + 15;
                
                // Adjust position if popup would go off screen
                if (x + popup.getWidth() > screenBounds.x + screenBounds.width) {
                    x = screenBounds.x + screenBounds.width - popup.getWidth() - 5;
                }
                if (y + popup.getHeight() > screenBounds.y + screenBounds.height) {
                    y = screenBounds.y + screenBounds.height - popup.getHeight() - 5;
                }
                
                popup.setLocation(x, y);
                popup.setVisible(true);
            }
            
            private void hideUserGames() {
                if (popup != null) {
                    popup.dispose();
                    popup = null;
                }
            }
            
            @Override
            public void updateUI() {
                setCellRenderer(null);
                super.updateUI();
                setCellRenderer(createCellRenderer());
            }
        };
        list.setCellRenderer(createCellRenderer());
        return list;
    }
    
    private ListCellRenderer<? super String> createCellRenderer() {
        return new ListCellRenderer<String>() {
            private final DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();
            private final JPanel panel = new JPanel(new BorderLayout());
            private final JButton actionButton = new JButton();
            private boolean isFriendList;
            
            {
                panel.setOpaque(true);
                actionButton.setOpaque(true);
                actionButton.setOpaque(true);
                actionButton.setBorderPainted(false);
                actionButton.setContentAreaFilled(true);
                actionButton.setMargin(new Insets(2, 10, 2, 10));
                actionButton.setFocusPainted(false);
                actionButton.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
                
                actionButton.addActionListener(e -> {
                    JButton source = (JButton) e.getSource();
                    String friendUsername = (String) source.getClientProperty("username");
                    boolean isFriend = (boolean) source.getClientProperty("isFriend");
                    
                    UserRepository userRepo = new UserRepository();
                    // Get current user's data
                    UserMemento currentMemento = SessionIterator.getCurrentMemento();
                    if (currentMemento == null) return;
                    
                    UserGameData currentUser = userRepo.loadUser(currentMemento.getUsername());
                    if (currentUser == null) return;
                    
                    // Get friend's data
                    UserGameData friendUser = userRepo.loadUser(friendUsername);
                    if (friendUser == null) return;
                    
                    int friendId = friendUser.getUserID();
                    
                    if (isFriend) {
                        // Remove friend
                        currentUser.removeFriend(friendId);
                        friendsModel.removeElement(friendUsername);
                    } else {
                        // Add friend
                        currentUser.addFriend(friendId);
                        // Only add to model if not already present
                        if (!friendsModel.contains(friendUsername)) {
                            friendsModel.addElement(friendUsername);
                        }
                    }
                    
                    // Save changes
                    userRepo.saveUser(currentUser);
                    
                    // Update the UI
                    friendsModel.removeAllElements();
                    updateFriendsList();
                });
                
                panel.add(actionButton, BorderLayout.EAST);
            }
            
            @Override
            public Component getListCellRendererComponent(JList<? extends String> list, String value, 
                                                        int index, boolean isSelected, boolean cellHasFocus) {
                // Skip button for placeholder messages
                if (value.startsWith("No ")) {
                    return defaultRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                }
                
                // Get the default label
                JLabel label = (JLabel) defaultRenderer.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus);
                
                // Set panel background and foreground
                panel.setBackground(label.getBackground());
                panel.setForeground(label.getForeground());
                
                // Determine if this is the friends list or all users list
                isFriendList = list == friendList;
                
                // Configure button based on context
                if (isFriendList) {
                    actionButton.setText("Remove");
                    actionButton.putClientProperty("isFriend", true);
                } else {
                    actionButton.setText("Add");
                    actionButton.putClientProperty("isFriend", false);
                }
                
                // Store the username for the button action
                actionButton.putClientProperty("username", value);
                
                // Style the button
                if (isFriendList) {
                    actionButton.setBackground(new Color(220, 53, 69)); // Bootstrap danger red
                    actionButton.setOpaque(true);
                    actionButton.setBorderPainted(false);
                    actionButton.setForeground(Color.WHITE);
                } else {
                    actionButton.setBackground(new Color(40, 167, 69)); // Bootstrap success green
                    actionButton.setOpaque(true);
                    actionButton.setBorderPainted(false);
                    actionButton.setForeground(Color.WHITE);
                }

                if (isFriendList) {
                    actionButton.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseEntered(MouseEvent e) {
                            actionButton.setBackground(new Color(228, 96, 109)); // Lighter red on hover
                        }
                        @Override
                        public void mouseExited(MouseEvent e) {
                            actionButton.setBackground(new Color(220, 53, 69));
                        }
                    });
                } else {
                    actionButton.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseEntered(MouseEvent e) {
                            actionButton.setBackground(new Color(72, 199, 110)); // Lighter green on hover
                        }
                        @Override
                        public void mouseExited(MouseEvent e) {
                            actionButton.setBackground(new Color(40, 167, 69));
                        }
                    });
                }
                
                // Add the label to the panel
                panel.remove(label);
                panel.add(label, BorderLayout.CENTER);
                
                return panel;
            }
        };
    }
    
    private void updateFriendsList() {
        friendsModel.clear();
        UserRepository userRepo = new UserRepository();
        
        // Get current user's data
        UserMemento currentMemento = SessionIterator.getCurrentMemento();
        if (currentMemento == null) {
            friendsModel.addElement("Please log in to see friends");
            return;
        }
        
        UserGameData userData = userRepo.loadUser(currentMemento.getUsername());
        if (userData == null || userData.getFriendList().isEmpty()) {
            friendsModel.addElement("No friends yet. Add some friends!");
        } else {
            // Get usernames for all friend IDs
            for (Integer friendId : userData.getFriendList()) {
                UserGameData friendData = userRepo.getUserById(friendId);
                if (friendData != null) {
                    String friendUsername = friendData.getUsername();
                    if (!friendsModel.contains(friendUsername)) {
                        friendsModel.addElement(friendUsername);
                    }
                }
            }
        }
    }

    @Override
    public void updateTheme() {
        setBackground(LightModeToggle.getBackgroundColor());
        
        // Update lists
        if (friendList != null) {
            friendList.setBackground(LightModeToggle.getComponentColor());
            friendList.setForeground(LightModeToggle.getTextColor());
        }
        
        if (allUsersList != null) {
            allUsersList.setBackground(LightModeToggle.getComponentColor());
            allUsersList.setForeground(LightModeToggle.getTextColor());
        }
        
        // Update labels
        if (friendsLabel != null) {
            friendsLabel.setForeground(LightModeToggle.getTextColor());
        }
        
        if (allUsersLabel != null) {
            allUsersLabel.setForeground(LightModeToggle.getTextColor());
        }
    }
}