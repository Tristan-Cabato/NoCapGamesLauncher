package com.mycompany.nocapgameslauncher.gui.resourceHandling;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import javax.swing.*;
import org.json.*;

import com.mycompany.nocapgameslauncher.gui.panels.*;

public class resourceLoader {
    private static final boolean DEBUG = true;
    public static final String RESOURCE_DIRECTORY = "src/main/resources/";
    public static final String PROXYIMAGE = "ImageResources/default_game_icon.jpg";
    
    public static ImageIcon loadIcon(String resourcePath) {
        if (DEBUG) System.out.println("loadIcon called with: " + resourcePath);
        
        URL url = resourceLoader.class.getResource("/" + resourcePath);
        if (url != null) {
            if (DEBUG) System.out.println(" → Loaded from classpath: " + url);
            return new ImageIcon(url);
        }
        
        String rp = resourcePath.startsWith("/") ? resourcePath.substring(1) : resourcePath;
        File f1 = new File(RESOURCE_DIRECTORY + rp); // Removed user generated path
        
        if (f1.exists()) { return new ImageIcon(f1.getAbsolutePath()); } 
        // If not found, return the proxy image
        return new ImageIcon(resourceLoader.class.getResource("/" + PROXYIMAGE));
    }

    public static Map<String, String> getGameById(int gameId) {
        String storeGamesPath = RESOURCE_DIRECTORY + "store_games.json";
        try (BufferedReader reader = new BufferedReader(new FileReader(storeGamesPath))) {
            String json = reader.lines().collect(java.util.stream.Collectors.joining());
            JSONArray gamesArray = new JSONArray(json);
            
            for (int i = 0; i < gamesArray.length(); i++) {
                JSONObject game = gamesArray.getJSONObject(i);
                if (game.getInt("gameID") == gameId) {
                    Map<String, String> gameDetails = new HashMap<>();
                    gameDetails.put("gameName", game.getString("gameName"));
                    gameDetails.put("description", game.optString("description", "No description available."));
                    gameDetails.put("imageURL", game.optString("imageURL", ""));
                    return gameDetails;
                }
            }
            System.err.println("Game with ID " + gameId + " not found in " + storeGamesPath);
        } catch (Exception e) {
            System.err.println("Error loading game details for ID " + gameId + ": " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    
    public static ArrayList<String> loadGamesFromFile(String filename) {
        ArrayList<String> games = new ArrayList<>();
        try {
            // First try to load from classpath
            InputStream is = resourceLoader.class.getResourceAsStream(filename.startsWith("/") ? filename : "/" + filename);
            
            // If not found in classpath, try loading from filesystem
            if (is == null) {
                is = new FileInputStream(filename);
            }
            
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    games.add(line.trim());
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading file " + filename + ": " + e.getMessage());
        }
        return games;
    }

    public static ArrayList<String> loadGameDescriptionsFromFile(String filename) {
        ArrayList<String> descriptions = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(Library.class.getResourceAsStream(filename)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    descriptions.add(line.trim());
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading game descriptions from " + filename + ": " + e.getMessage());
        }
        return descriptions;
    }
}
