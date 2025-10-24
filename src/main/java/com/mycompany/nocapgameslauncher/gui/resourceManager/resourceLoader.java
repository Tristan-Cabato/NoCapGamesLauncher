package com.mycompany.nocapgameslauncher.gui.resourceManager;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;

import com.mycompany.nocapgameslauncher.gui.panels.*;

public class resourceLoader {
    private static final boolean DEBUG = true;
    public static final String PROXYIMAGE = "ImageResources/default_game_icon.jpg";
    
    public static ImageIcon loadIcon(String resourcePath) {
        if (DEBUG) System.out.println("loadIcon called with: " + resourcePath);
        
        URL url = resourceLoader.class.getResource("/" + resourcePath);
        if (url != null) {
            if (DEBUG) System.out.println(" → Loaded from classpath: " + url);
            return new ImageIcon(url);
        }
        
        String rp = resourcePath.startsWith("/") ? resourcePath.substring(1) : resourcePath;
        File f1 = new File("src/main/resources/" + rp); // Removed user generated path
        
        if (f1.exists()) { return new ImageIcon(f1.getAbsolutePath()); } 
        // If not found, return the proxy image
        return new ImageIcon(resourceLoader.class.getResource("/" + PROXYIMAGE));
    }

    public static ArrayList<String> loadGamesFromFile(String filename) {
        ArrayList<String> games = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(Library.class.getResourceAsStream(filename)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    games.add(line.trim());
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading game list from " + filename + ": " + e.getMessage());
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
