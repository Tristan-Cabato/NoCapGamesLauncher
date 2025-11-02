package com.mycompany.nocapgameslauncher.userManager;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import com.google.gson.Gson;
import com.mycompany.nocapgameslauncher.resourceHandling.resourceLoader;

public class UserGameManager {
    private static final String USER_DATA_DIR = "user_data";
    private static final String USERS_DIR = resourceLoader.RESOURCE_DIRECTORY + "Users/";
    private static final Gson gson = new Gson();

    // Getters
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

    

    public static UserGameData getUserGames(String username) {
        String filename = USERS_DIR + username + ".json";
        try {
            String json = new String(Files.readAllBytes(Paths.get(filename)));
            UserGameData data = gson.fromJson(json, UserGameData.class);
            if (data == null) {
                throw new IllegalStateException("Failed to parse user data for: " + username);
            }
            return data;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read user data for: " + username, e);
        }
    }

    // Modifications
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

    public static boolean addGameToUser(String username, int gameId) {
        try {
            UserGameData userData = getUserGames(username);
            if (userData == null) return false;
            
            userData.addGame(gameId);
            saveUserData(userData);
            return true;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to add game to user: " + username, e);
        }
    }
    
    private static void saveUserData(UserGameData userData) {
        String filename = USERS_DIR + userData.getUsername() + ".json";
        try (Writer writer = new FileWriter(filename)) {
            gson.toJson(userData, writer);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to save user data: " + userData.getUsername(), e);
        }
    }
}
