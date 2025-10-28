package com.mycompany.nocapgameslauncher.gui.resourceHandling;

import java.util.*;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;

public class NameFormatting {
    public static ArrayList<String> getGameTitlesFromJson() {
        ArrayList<String> titles = new ArrayList<>();
        
        // Try multiple approaches to load the file
        String[] possiblePaths = {
            "/store_games.json",  // Try root of classpath first
            "store_games.json",   // Try relative path
            "src/main/resources/store_games.json"  // Try direct file path as last resort
        };
        
        for (String path : possiblePaths) {
            try {
                // First try as a resource
                try (InputStream is = NameFormatting.class.getResourceAsStream(path)) {
                    if (is != null) {
                        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
                            String jsonContent = reader.lines().collect(java.util.stream.Collectors.joining(""));
                            JSONArray gamesArray = new JSONArray(jsonContent);
                            
                            for (int i = 0; i < gamesArray.length(); i++) {
                                JSONObject game = gamesArray.getJSONObject(i);
                                String gameName = game.getString("gameName");
                                titles.add(formatGameName(gameName));
                            }
                            
                            System.out.println("Successfully loaded " + titles.size() + " game titles from: " + path);
                            return titles; // Success - return the titles
                        }
                    }
                }
                
                // If we get here, try as a direct file
                File file = new File(path);
                if (file.exists()) {
                    try (BufferedReader reader = new BufferedReader(new FileReader(file, java.nio.charset.StandardCharsets.UTF_8))) {
                        String jsonContent = reader.lines().collect(java.util.stream.Collectors.joining(""));
                        JSONArray gamesArray = new JSONArray(jsonContent);
                        
                        for (int i = 0; i < gamesArray.length(); i++) {
                            JSONObject game = gamesArray.getJSONObject(i);
                            String gameName = game.getString("gameName");
                            titles.add(formatGameName(gameName));
                        }
                        
                        System.out.println("Successfully loaded " + titles.size() + " game titles from file: " + file.getAbsolutePath());
                        return titles; // Success - return the titles
                    }
                }
            } catch (Exception e) {
                System.err.println("Attempt failed for path '" + path + "': " + e.getMessage());
                // Continue to next path
            }
        }
        
        System.err.println("Failed to load game titles. Tried paths: " + String.join(", ", possiblePaths));
        return titles; // Return empty list if all attempts failed
    }

    public static String formatGameName(String name) {
        if (name == null || name.isEmpty()) {
            return "";
        }
        
        // Replace underscores with spaces
        String withSpaces = name.replace('_', ' ');
        
        // Capitalize first letter of each word
        String[] words = withSpaces.split(" ");
        StringBuilder result = new StringBuilder();
        
        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)))
                    .append(word.substring(1).toLowerCase())
                    .append(" ");
            }
        }
        
        return result.toString().trim();
    }
}
