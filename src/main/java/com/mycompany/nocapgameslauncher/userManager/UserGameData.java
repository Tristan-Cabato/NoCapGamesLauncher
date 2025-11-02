package com.mycompany.nocapgameslauncher.userManager;

import com.google.gson.Gson;
import com.mycompany.nocapgameslauncher.resourceHandling.resourceLoader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mycompany.nocapgameslauncher.database.DatabaseHandler;

public class UserGameData {
    private DatabaseHandler database = DatabaseHandler.getInstance();
    private List<Integer> ownedGameIds;
    private int userID;
    private String username;
    private String password;
    private static final Gson gson = new Gson();

    public UserGameData() {
        this.ownedGameIds = new ArrayList<>();
    }

    public UserGameData(String username) {
        this.username = username;
        this.ownedGameIds = new ArrayList<>();
        try {
            this.userID = database.getUserId(username);
        } catch (SQLException e) {
            System.err.println("Error getting user ID: " + e.getMessage());
        }
    }

    public List<Integer> getOwnedGameIds() {
        return new ArrayList<>(ownedGameIds);
    }

    public void setOwnedGameIds(List<Integer> ownedGameIds) {
        this.ownedGameIds = new ArrayList<>(ownedGameIds);
    }
    
    public void addGame(int gameId) {
        if (!ownedGameIds.contains(gameId)) {
            ownedGameIds.add(gameId);
            saveToFile();
        }
    }
    
    public void removeGame(int gameId) {
        if (ownedGameIds.remove(Integer.valueOf(gameId))) {
            saveToFile();
        }
    }

    public boolean ownsGame(int gameId) {
        return ownedGameIds.contains(gameId);
    }

    private void saveToFile() {
        try {
            String currentUser = com.mycompany.nocapgameslauncher.database.DatabaseHandler.getCurrentUser();
            if (currentUser == null || currentUser.trim().isEmpty()) {
                throw new IllegalStateException("No user is currently logged in.");
            }

            String userJsonPath = resourceLoader.RESOURCE_DIRECTORY + "Users/" + currentUser + ".json";
            File userFile = new File(userJsonPath);
            
            Map<String, Object> userData = new HashMap<>();
            if (userFile.exists()) {
                try (FileReader reader = new FileReader(userFile)) {
                    userData = gson.fromJson(reader, Map.class);
                }
                if (userData == null) {
                    userData = new HashMap<>();
                }
            }

            userData.put("ownedGameIds", this.ownedGameIds);

            try (FileWriter writer = new FileWriter(userFile)) {
                gson.toJson(userData, writer);
            }
        } catch (Exception e) {
            System.err.println("Error saving user game data: " + e.getMessage());
            throw new RuntimeException("Failed to save game data", e);
        }
    }

    public static UserGameData loadForUser(String username) {
        try {
            String userJsonPath = resourceLoader.RESOURCE_DIRECTORY + "Users/" + username + ".json";
            File userFile = new File(userJsonPath);
            
            if (!userFile.exists()) {
                return new UserGameData();
            }

            try (FileReader reader = new FileReader(userFile)) {
                Map<String, Object> userData = gson.fromJson(reader, Map.class);
                if (userData != null) {
                    UserGameData gameData = new UserGameData();
                    @SuppressWarnings("unchecked")
                    List<Number> gameIds = (List<Number>) userData.get("ownedGameIds");
                    if (gameIds != null) {
                        gameData.ownedGameIds = new ArrayList<>();
                        for (Number id : gameIds) {
                            gameData.ownedGameIds.add(id.intValue());
                        }
                    }
                    return gameData;
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading user game data: " + e.getMessage());
        }
        return new UserGameData();
    }

    // Previous Getters and Setters

        // Getters and Setters
    public int getUserID() {
        return userID;
    }
    
    public void setUserID(int userID) {
        this.userID = userID;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password; // In a real app, hash the password before storing
    }
}