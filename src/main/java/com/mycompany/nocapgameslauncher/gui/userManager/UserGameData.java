package com.mycompany.nocapgameslauncher.gui.userManager;

import java.util.ArrayList;
import java.util.List;
import java.sql.SQLException;
import com.mycompany.nocapgameslauncher.database.DatabaseHandler;

public class UserGameData {
    private int userID;
    private String username;
    private String password; // Note: In a real application, store only hashed passwords
    private List<Integer> ownedGameIds;
    
    // Default constructor required for Gson
    public UserGameData() {
        this.ownedGameIds = new ArrayList<>();
    }
    
    public UserGameData(String username) {
        this.username = username;
        this.ownedGameIds = new ArrayList<>();
        try {
            this.userID = DatabaseHandler.getUserId(username);
        } catch (SQLException e) {
            System.err.println("Error getting user ID: " + e.getMessage());
        }
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
        this.password = password; // In a real app, hash the password before storing
    }
    
    public List<Integer> getOwnedGameIds() {
        return ownedGameIds;
    }
    
    public void setOwnedGameIds(List<Integer> ownedGameIds) {
        this.ownedGameIds = ownedGameIds;
    }
    
    public void addGame(int gameId) {
        if (!ownedGameIds.contains(gameId)) {
            ownedGameIds.add(gameId);
        }
    }
    
    public void removeGame(int gameId) {
        ownedGameIds.remove(Integer.valueOf(gameId));
    }
}