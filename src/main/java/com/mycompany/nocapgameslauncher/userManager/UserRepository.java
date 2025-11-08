package com.mycompany.nocapgameslauncher.userManager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mycompany.nocapgameslauncher.resourceHandling.resourceLoader;

import java.io.*;
import java.nio.file.*;

public class UserRepository {
    private static final String USERS_DIR = resourceLoader.RESOURCE_DIRECTORY + "Users/";
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final String ADMIN_USER = "Admin";
    
    static {
        try {
            Files.createDirectories(Paths.get(USERS_DIR));
        } catch (IOException e) {
            System.err.println("Error creating Users directory: " + e.getMessage());
        }
    }
    
    public void saveUser(UserGameData userData) {
        // Skip saving admin user data or invalid usernames
        if (userData == null || userData.getUsername() == null || 
            userData.getUsername().trim().isEmpty() ||
            userData.getUsername().equalsIgnoreCase(ADMIN_USER)) {
            // Ensure we don't create Admin.json by returning early
            return;
        }
        String filename = USERS_DIR + userData.getUsername() + ".json";
        try {
            // Ensure parent directories exist
            File file = new File(filename);
            file.getParentFile().mkdirs();
            
            // Delete existing file if it exists to ensure clean rewrite
            if (file.exists()) {
                Files.deleteIfExists(Paths.get(filename));
            }
            
            // Write the new data
            try (Writer writer = new FileWriter(filename)) {
                gson.toJson(userData, writer);
            }
        } catch (IOException e) {
            System.err.println("Error saving user data: " + e.getMessage());
        }
    }
    
    public UserGameData loadUser(String username) {
        if (username == null || username.equalsIgnoreCase(ADMIN_USER)) {
            return null;
        }
        
        String filename = USERS_DIR + username + ".json";
        try {
            File file = new File(filename);
            if (!file.exists() || !file.getName().equals(username + ".json")) {
                return null;
            }
            
            // Important: instantiate via constructor so UserGameData.loadFromFile() runs.
            // This guarantees userID and friendList are properly initialized.
            return new UserGameData(username);
        } catch (Exception e) {
            return null;
        }
    }
    
    public boolean userExists(String username) {
        if (username == null || username.equalsIgnoreCase(ADMIN_USER)) {
            return false;
        }
        File file = new File(USERS_DIR + username + ".json");
        return file.exists() && file.getName().equals(username + ".json");
    }
    
    public UserGameData getUserById(int userId) {
        File usersDir = new File(USERS_DIR);
        if (!usersDir.exists() || !usersDir.isDirectory()) {
            return null;
        }
        
        File[] userFiles = usersDir.listFiles((dir, name) -> name.endsWith(".json"));
        if (userFiles == null) {
            return null;
        }
        
        for (File userFile : userFiles) {
            try {
                String fileName = userFile.getName();
                if (!fileName.endsWith(".json")) continue;
                String username = fileName.substring(0, fileName.length() - 5);
                
                UserGameData userData = loadUser(username); // use loadUser so loadFromFile runs
                if (userData != null && userData.getUserID() == userId) {
                    return userData;
                }
            } catch (Exception e) {
                // ignore malformed files but continue searching
            }
        }
        return null;
    }
}