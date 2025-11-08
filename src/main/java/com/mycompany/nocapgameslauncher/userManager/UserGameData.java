package com.mycompany.nocapgameslauncher.userManager;

import com.mycompany.nocapgameslauncher.database.DatabaseHandler;
import com.mycompany.nocapgameslauncher.resourceHandling.resourceLoader;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.*;
import java.util.*;

public class UserGameData {
    private final Set<Integer> ownedGameIds;
    private final Map<Integer, GameStats> gameStats;
    private final Set<Integer> friendList;
    private int userID;
    private String username;
    private String password;
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public UserGameData(String username) {
        this.username = username;
        this.ownedGameIds = new HashSet<>();
        this.gameStats = new HashMap<>();
        this.friendList = new HashSet<>();
        loadFromFile();
    }

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
        this.password = password;
    }

    public Set<Integer> getOwnedGameIds() {
        return new HashSet<>(ownedGameIds);
    }

    public Set<Integer> getFriendList() {
        return friendList != null ? new HashSet<>(friendList) : new HashSet<>();
    }

    // Game Management
    public void addGame(int gameId) {
        if (ownedGameIds.add(gameId)) {
            saveToFile();
        }
    }

    public void removeGame(int gameId) {
        if (ownedGameIds.remove(gameId)) {
            gameStats.remove(gameId);
            saveToFile();
        }
    }

    public boolean ownsGame(int gameId) {
        return ownedGameIds.contains(gameId);
    }
    
    // Friend Management
    public void addFriend(int friendId) {
        if (friendList.add(friendId)) {
            saveToFile();
        }
    }

    public void removeFriend(int friendId) {
        if (friendList.remove(friendId)) 
            saveToFile();
    }

    public boolean hasFriend(int friendId) {
        return friendList.contains(friendId);
    }

    // Game Statistics
    public void updateGameStats(int gameId, int playCount, long lastPlayed) {
        gameStats.put(gameId, new GameStats(playCount, lastPlayed));
        saveToFile();
    }

    public GameStats getGameStats(int gameId) {
        return gameStats.getOrDefault(gameId, new GameStats(0, 0));
    }

    // File Operations
    private void loadFromFile() {
        try {
            String userJsonPath = resourceLoader.RESOURCE_DIRECTORY + "Users/" + username + ".json";
            File userFile = new File(userJsonPath);
            
            if (!userFile.exists()) {
                // If file doesn't exist, initialize with empty data and save it
                this.ownedGameIds.clear();
                this.gameStats.clear();
                this.friendList.clear();
                saveToFile();  // This will create the file with empty data
                return;
            }

            try (FileReader reader = new FileReader(userFile)) {
                Map<String, Object> userData = gson.fromJson(reader, Map.class);
                if (userData != null) {
                    // Load owned games
                    if (userData.containsKey("ownedGameIds")) {
                        List<Double> gameIds = (List<Double>) userData.get("ownedGameIds");
                        if (gameIds != null) {
                            ownedGameIds.clear();
                            for (Double id : gameIds) {
                                ownedGameIds.add(id.intValue());
                            }
                        }
                    }

                    // Load game stats
                    if (userData.containsKey("gameStats")) {
                        Map<String, Map<String, Object>> statsMap = 
                            (Map<String, Map<String, Object>>) userData.get("gameStats");
                        if (statsMap != null) {
                            gameStats.clear();
                            for (Map.Entry<String, Map<String, Object>> entry : statsMap.entrySet()) {
                                try {
                                    int gameId = Integer.parseInt(entry.getKey());
                                    Map<String, Object> statData = entry.getValue();
                                    int playCount = ((Double) statData.getOrDefault("playCount", 0.0)).intValue();
                                    long lastPlayed = ((Double) statData.getOrDefault("lastPlayed", 0.0)).longValue();
                                    gameStats.put(gameId, new GameStats(playCount, lastPlayed));
                                } catch (NumberFormatException e) {
                                    System.err.println("Invalid game ID in stats: " + entry.getKey());
                                }
                            }
                        }
                    }

                    // Load friends list
                    if (userData.containsKey("friendList")) {
                        List<Double> friends = (List<Double>) userData.get("friendList");
                        if (friends != null) {
                            friendList.clear();
                            for (Double id : friends) {
                                friendList.add(id.intValue());
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading user data: " + e.getMessage());
            // Initialize with empty data if there's an error
            this.ownedGameIds.clear();
            this.gameStats.clear();
            this.friendList.clear();
        }
    }

    public static UserGameData loadForUser(String username) {
        UserGameData userData = new UserGameData(username);
        return userData;
    }

    private void saveToFile() {
        try {
            String currentUser = DatabaseHandler.getCurrentUser();
            if (currentUser == null || currentUser.trim().isEmpty()) {
                throw new IllegalStateException("No user is currently logged in.");
            }

            String userJsonPath = resourceLoader.RESOURCE_DIRECTORY + "Users/" + currentUser + ".json";
            File userFile = new File(userJsonPath);
            
            // Create parent directories if they don't exist
            userFile.getParentFile().mkdirs();

            Map<String, Object> userData = new HashMap<>();
            if (userFile.exists()) {
                try (FileReader reader = new FileReader(userFile)) {
                    userData = gson.fromJson(reader, Map.class);
                }
                if (userData == null) {
                    userData = new HashMap<>();
                }
            }

            // Save owned games
            userData.put("ownedGameIds", new ArrayList<>(ownedGameIds));
            
            // Save game stats
            Map<String, Map<String, Object>> statsMap = new HashMap<>();
            gameStats.forEach((gameId, stats) -> {
                Map<String, Object> statMap = new HashMap<>();
                statMap.put("playCount", stats.getPlayCount());
                statMap.put("lastPlayed", stats.getLastPlayed());
                statsMap.put(gameId.toString(), statMap);
            });
            userData.put("gameStats", statsMap);

            // Write back to file
            try (FileWriter writer = new FileWriter(userFile)) {
                gson.toJson(userData, writer);
            }
        } catch (Exception e) {
            System.err.println("Error saving user data: " + e.getMessage());
        }
    }

    public static class GameStats {
        private final int playCount;
        private final long lastPlayed;

        public GameStats(int playCount, long lastPlayed) {
            this.playCount = playCount;
            this.lastPlayed = lastPlayed;
        }

        public int getPlayCount() { 
            return playCount; 
        }
        
        public long getLastPlayed() { 
            return lastPlayed; 
        }
    }
}