package com.mycompany.nocapgameslauncher.game_manager;

import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import org.json.JSONObject;
import org.json.JSONArray;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class GameDescriptionIterator implements Iterator<Map.Entry<String, String>> {
    private final JSONArray gamesArray;
    private int currentIndex = 0;

    public GameDescriptionIterator(JSONArray gamesArray) {
        this.gamesArray = gamesArray;
    }

    public static GameDescriptionIterator fromJson(InputStream inputStream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            StringBuilder jsonContent = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonContent.append(line.trim());
            }
            return new GameDescriptionIterator(new JSONArray(jsonContent.toString()));
        }
    }

    @Override
    public boolean hasNext() {
        return currentIndex < gamesArray.length();
    }

    @Override
    public Map.Entry<String, String> next() {
        if (!hasNext()) {
            throw new java.util.NoSuchElementException("No more game descriptions");
        }
        
        try {
            JSONObject game = gamesArray.getJSONObject(currentIndex++);
            String gameName = game.optString("gameName");
            String gameDescription = game.optString("gameDescription", "No description available.");
            
            return new java.util.AbstractMap.SimpleEntry<>(gameName, gameDescription);
        } catch (Exception e) {
            throw new RuntimeException("Error getting game description at index " + (currentIndex - 1), e);
        }
    }

    public Map<String, String> toMap() {
        Map<String, String> descriptions = new HashMap<>();
        while (hasNext()) {
            Map.Entry<String, String> entry = next();
            if (entry.getKey() != null && !entry.getKey().isEmpty()) {
                descriptions.put(entry.getKey(), entry.getValue());
            }
        }
        return descriptions;
    }
}