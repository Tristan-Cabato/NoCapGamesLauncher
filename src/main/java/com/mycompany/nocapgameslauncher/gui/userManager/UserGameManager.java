package com.mycompany.nocapgameslauncher.gui.userManager;

import java.io.*;
import java.util.*;

public class UserGameManager {
    private static final String USER_DATA_DIR = "user_data";
    
    public static void loadUserGames(String userId) {
        File file = new File(USER_DATA_DIR, userId + "_games.json");
        
        if (!file.exists()) {
            try {
                if (file.createNewFile()) {
                    System.out.println("JSON file created: " + file.getName());
                } else System.out.println("JSON file already exists: " + file.getName());
            } catch (IOException e) {
                System.out.println("Error creating JSON file: " + e.getMessage());
            }
        } else System.out.println("JSON file does not exist: " + file.getName());
    }

    public static void updateUserGames(String userId, List<UserGameData> games) {
        File file = new File(USER_DATA_DIR, userId + "_games.json");
        
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("User Data:\n");
            writer.write("Owned Games:\n\n[");
            for (UserGameData game : games) {
                writer.write("\n" + game.toString());
            }
            writer.write("\n]");
        } catch (IOException e) {
            System.out.println("Error saving JSON file: " + e.getMessage());
        }
    }
}
