
package com.mycompany.nocapgameslauncher;

import com.mycompany.nocapgameslauncher.database.DatabaseHandler;
import com.mycompany.nocapgameslauncher.gui.panels.LoginForm;
import com.mycompany.nocapgameslauncher.iterator.SessionIterator;
import com.mycompany.nocapgameslauncher.resourceHandling.resourceLoader;
import com.mycompany.nocapgameslauncher.userManager.UserMemento;
import javax.swing.*;

public class NoCapGamesLauncher {
    public static void main(String[] args) {
        // Initialize database and load any saved session
        DatabaseHandler.getInstance().initializeDatabase();
        UserMemento savedSession = SessionIterator.getCurrentMemento();
        
        SwingUtilities.invokeLater(() -> {
            JFrame loginFrame = new JFrame("No Cap Games - Login");
            loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            
            // Set window icon
            try {
                ImageIcon icon = new ImageIcon(resourceLoader.RESOURCE_DIRECTORY + "/ImageResources/logo.png");
                loginFrame.setIconImage(icon.getImage());
            } catch (Exception e) {
                System.err.println("Error loading window icon: " + e.getMessage());
            }
            
            LoginForm loginForm = new LoginForm(savedSession);
            
            // Pass the saved session to the login form
            loginFrame.getContentPane().add(loginForm);
            loginFrame.setMinimumSize(new java.awt.Dimension(500, 600));
            loginFrame.pack();
            loginFrame.setLocationRelativeTo(null);
            loginFrame.setVisible(false);

            loginForm.initAutoLogin();

            if (!loginForm.userRemembered) loginFrame.setVisible(true);
        });
        
        // Add shutdown hook to save session on exit
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            SessionIterator.saveCurrentMemento();
        }));
    }
}
