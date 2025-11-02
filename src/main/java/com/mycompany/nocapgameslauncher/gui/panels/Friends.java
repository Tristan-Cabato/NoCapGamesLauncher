package com.mycompany.nocapgameslauncher.gui.panels;

import com.mycompany.nocapgameslauncher.gui.mainFrame;
import com.mycompany.nocapgameslauncher.gui.utilities.LightModeToggle;
import com.mycompany.nocapgameslauncher.gui.utilities.ThemePanel;
import com.mycompany.nocapgameslauncher.gui.utilities.FontManager;
import com.mycompany.nocapgameslauncher.gui.userManager.FriendsIterator;
import javax.swing.*;
import java.awt.*;

public class Friends extends ThemePanel {
    private JList<String> friendList;
    private DefaultListModel<String> listModel;

    public Friends(mainFrame frame) {
        super(new BorderLayout());
        
        // For a list style of showing users
        listModel = new DefaultListModel<>();
        
        FriendsIterator friendsIterator = new FriendsIterator();
        if (!friendsIterator.hasNext()) {
            listModel.addElement("No users available.");
        } else {
            while (friendsIterator.hasNext()) {
                listModel.addElement(friendsIterator.next());
            }
        }

        // Styling the list
        friendList = new JList<>(listModel);
        friendList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                c.setBackground(isSelected ? LightModeToggle.getComponentColor().brighter() : LightModeToggle.getComponentColor());
                c.setForeground(LightModeToggle.getTextColor());
                setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                return c;
            }
        });
        
        /* "All Users" Label
         Added Friends will be for later
         */
        JLabel userLabel = new JLabel("    All Users");
        userLabel.setForeground(LightModeToggle.getTextColor());
        FontManager.setFont(userLabel, Font.BOLD, 24);
        add(userLabel, BorderLayout.NORTH);

        // Add the list to a scroll pane
        JScrollPane scrollPane = new JScrollPane(friendList);
        scrollPane.setBorder(BorderFactory.createLineBorder(LightModeToggle.getComponentColor()));

        add(scrollPane, BorderLayout.CENTER);
        updateTheme();
    }

    @Override
    public void updateTheme() {
        setBackground(LightModeToggle.getBackgroundColor());
        friendList.setBackground(LightModeToggle.getComponentColor());
        friendList.setForeground(LightModeToggle.getTextColor());
        
        // Update the "All Users" label color
        Component[] components = getComponents();
        for (Component comp : components) {
            if (comp instanceof JLabel) {
                JLabel label = (JLabel) comp;
                label.setForeground(LightModeToggle.getTextColor());
            }
        }
    }
}