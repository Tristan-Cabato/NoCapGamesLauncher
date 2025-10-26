
package com.mycompany.nocapgameslauncher;

import com.mycompany.nocapgameslauncher.database.DatabaseHandler;
import com.mycompany.nocapgameslauncher.gui.panels.LoginForm;
import javax.swing.*;

public class NoCapGamesLauncher {
    public static void main(String[] args) {
        // Initialize database before starting the UI
        DatabaseHandler.initializeDatabase();
        
        SwingUtilities.invokeLater(() -> {
            // Show login dialog first - don't create mainFrame yet
            JFrame loginFrame = new JFrame("No Cap Games - Login");
            loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            loginFrame.getContentPane().add(new LoginForm(null));
            loginFrame.setMinimumSize(new java.awt.Dimension(500, 600));
            loginFrame.pack();
            loginFrame.setLocationRelativeTo(null);
            loginFrame.setVisible(true);
        });
    }

    /* This is for testing
    public static void straightToPage() { 
        mainFrame main = new mainFrame();
        main.setVisible(true);
    } */
}
