package com.mycompany.nocapgameslauncher.userManager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class UserRepository {
    private static final String USERS_DIR = "src/main/resources/Users/";
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    
    static {
        try {
            Files.createDirectories(Paths.get(USERS_DIR));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void saveUser(UserGameData userData) {
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
            e.printStackTrace();
        }
    }
    
    public UserGameData loadUser(String username) {
        String filename = USERS_DIR + username + ".json";
        try (Reader reader = new FileReader(filename)) {
            return gson.fromJson(reader, UserGameData.class);
        } catch (IOException e) {
            return null;
        }
    }
    
    public boolean userExists(String username) {
        return Files.exists(Paths.get(USERS_DIR + username + ".json"));
    }
}
