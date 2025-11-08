package com.mycompany.nocapgameslauncher.gui.panels;

import com.mycompany.nocapgameslauncher.gui.utilities.LightModeToggle;
import com.mycompany.nocapgameslauncher.gui.utilities.ThemePanel;
import com.mycompany.nocapgameslauncher.gui.utilities.FontManager;
import com.mycompany.nocapgameslauncher.iterator.UsersIterator;
import com.mycompany.nocapgameslauncher.iterator.SessionIterator;
import com.mycompany.nocapgameslauncher.userManager.UserGameData;
import com.mycompany.nocapgameslauncher.userManager.UserRepository;
import com.mycompany.nocapgameslauncher.userManager.UserMemento;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class Friends extends ThemePanel {
    private JList<String> friendList;
    private JList<String> allUsersList;
    private DefaultListModel<String> friendsModel;
    private DefaultListModel<String> allUsersModel;
    private JLabel friendsLabel;
    private JLabel allUsersLabel;

    public Friends() {
        super(new BorderLayout());
        setupUI();
    }

    private void setupUI() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setOpaque(false);

        friendsLabel = new JLabel("Friends");
        FontManager.setFont(friendsLabel, Font.BOLD, 18);
        friendsLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        mainPanel.add(friendsLabel);

        friendsModel = new DefaultListModel<>();
        updateFriendsList();

        friendList = new JList<>(friendsModel);
        friendList.setCellRenderer(createListCellRenderer());
        JScrollPane friendsScrollPane = new JScrollPane(friendList);
        friendsScrollPane.setBorder(BorderFactory.createLineBorder(LightModeToggle.getComponentColor()));
        mainPanel.add(friendsScrollPane);

        mainPanel.add(Box.createVerticalStrut(20)); // Space between lists

        allUsersLabel = new JLabel("All Users");
        FontManager.setFont(allUsersLabel, Font.BOLD, 18);
        allUsersLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        mainPanel.add(allUsersLabel);

        allUsersModel = new DefaultListModel<>();
        loadAllUsers();

        allUsersList = new JList<>(allUsersModel);
        allUsersList.setCellRenderer(createListCellRenderer());
        JScrollPane allUsersScrollPane = new JScrollPane(allUsersList);
        allUsersScrollPane.setBorder(BorderFactory.createLineBorder(LightModeToggle.getComponentColor()));
        mainPanel.add(allUsersScrollPane);

        add(mainPanel, BorderLayout.CENTER);
        updateTheme();
    }

    private ListCellRenderer<? super String> createListCellRenderer() {
        return new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus);

                // Skip styling for placeholder messages
                String username = (String) value;
                if (username.startsWith("No ") || username.startsWith("Please ")) {
                    return label;
                }

                // Set basic styling
                label.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                label.setOpaque(true);

                // Style based on selection
                if (isSelected) {
                    label.setBackground(LightModeToggle.getComponentColor().darker());
                    label.setForeground(LightModeToggle.getTextColor());
                } else {
                    label.setBackground(LightModeToggle.getComponentColor());
                    label.setForeground(LightModeToggle.getTextColor());
                }

                return label;
            }
        };
    }

    private void loadAllUsers() {
        UsersIterator allUsersIterator = new UsersIterator();
        String currentUsername = SessionIterator.getCurrentMemento() != null ? 
            SessionIterator.getCurrentMemento().getUsername() : "";

        if (!allUsersIterator.hasNext()) {
            allUsersModel.addElement("No users available.");
        } else {
            while (allUsersIterator.hasNext()) {
                String username = allUsersIterator.next();
                // Skip the current user
                if (!username.equals(currentUsername)) {
                    allUsersModel.addElement(username);
                }
            }

            if (allUsersModel.size() == 0) {
                allUsersModel.addElement("No other users available.");
            }
        }
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