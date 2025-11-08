package com.mycompany.nocapgameslauncher.userManager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import com.mycompany.nocapgameslauncher.database.DatabaseHandler;
import com.mycompany.nocapgameslauncher.resourceHandling.resourceLoader;

public class UserMemento {
    private static final String SESSION_FILE = resourceLoader.RESOURCE_DIRECTORY + "Users/sessionManager.json";
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    
    private boolean rememberMe;
    private String username;
    private char[] password;
    private String lastPanel;
    private String themeState;
    private static DatabaseHandler database = DatabaseHandler.getInstance();

    public UserMemento(String username, char[] password, String lastPanel, String themeState) {
        this.username = username;
        this.password = password != null ? Arrays.copyOf(password, password.length) : null;
        this.lastPanel = lastPanel;
        this.themeState = themeState;
        this.rememberMe = true;
    }
    
    // Getters
    public String getUsername() { return username; }
    public char[] getPassword() { return password != null ? Arrays.copyOf(password, password.length) : null; }
    public String getLastPanel() { return lastPanel; }
    public String getThemeState() { return themeState; }
    public boolean shouldRememberMe() { return rememberMe; }
    
    // Setters
    public void setRememberMe(boolean rememberMe) { this.rememberMe = rememberMe; }
    public void setLastPanel(String lastPanel) { this.lastPanel = lastPanel; }
    public void setThemeState(String themeState) { this.themeState = themeState; }
    
    // Save memento to file
    public void saveToFile() {
        // Don't save session if rememberMe is false, or if the last panel was databaseMegaquery or user is admin
        if (!rememberMe || "databaseMegaquery".equals(lastPanel) || database.DB_USER.equals(username)) {
            clearSession();
            return;
        }
        
        JsonObject json = new JsonObject();
        json.addProperty("rememberMe", rememberMe);
        json.addProperty("username", username);
        json.addProperty("password", password != null ? new String(password) : "");
        json.addProperty("lastPanel", lastPanel);
        json.addProperty("themeState", themeState);
        
        try (FileWriter writer = new FileWriter(SESSION_FILE)) {
            gson.toJson(json, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    // Load memento from file
    public static UserMemento loadFromFile() {
        try {
            String content = new String(Files.readAllBytes(Paths.get(SESSION_FILE)));
            JsonObject json = gson.fromJson(content, JsonObject.class);
            
            if (json != null && json.get("rememberMe").getAsBoolean()) {
                char[] password = json.has("password") ? json.get("password").getAsString().toCharArray() : null;
                return new UserMemento(
                    json.get("username").getAsString(),
                    password,
                    json.get("lastPanel").getAsString(),
                    json.get("themeState").getAsString()
                );
            }
        } catch (Exception e) {
            // File doesn't exist or is invalid, return null
        }
        return null;
    }
    
    // Clear session data
    public static void clearSession() {
        try {
            File sessionFile = new File(SESSION_FILE);
            // Create parent directories if they don't exist
            File parentDir = sessionFile.getParentFile();
            if (parentDir != null) {
                parentDir.mkdirs();
            }
            
            JsonObject empty = new JsonObject();
            empty.addProperty("rememberMe", false);
            empty.addProperty("username", "");
            empty.addProperty("password", "");
            empty.addProperty("lastPanel", "");
            empty.addProperty("themeState", "");
            
            try (FileWriter writer = new FileWriter(sessionFile)) {
                gson.toJson(empty, writer);
                writer.flush(); // Ensure data is written to disk
            }
        } catch (IOException e) {
            e.printStackTrace();
            // Consider adding proper error logging here
        }
    }
}
