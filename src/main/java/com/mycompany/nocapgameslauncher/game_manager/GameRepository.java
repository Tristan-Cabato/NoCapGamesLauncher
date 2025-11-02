// Create a new file: game_manager/GameRepository.java
package com.mycompany.nocapgameslauncher.game_manager;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class GameRepository {
    private static final String GAMES_FILE = "src/main/resources/store_games.json";
    
    public static void saveGames(List<Game> games) {
        try {
            JSONArray gamesArray = new JSONArray();
            for (int i = 0; i < games.size(); i++) {
                Game game = games.get(i);
                JSONObject gameJson = new JSONObject();
                gameJson.put("gameID", i + 1);  // 1-based index
                gameJson.put("gameName", game.getTitle());
                gameJson.put("imageURL", game.getImageUrl());
                gameJson.put("gameURL", game.getGameUrl()); 
                gameJson.put("gameDescription", game.getDescription());
                gamesArray.put(gameJson);
            }
            
            Files.createDirectories(Paths.get(GAMES_FILE).getParent());
            Files.writeString(Paths.get(GAMES_FILE), gamesArray.toString(4));
        } catch (IOException e) {
            throw new RuntimeException("Failed to save games", e);
        }
    }
    
    public static List<Game> loadGames() {
        try {
            if (!Files.exists(Paths.get(GAMES_FILE))) {
                return new ArrayList<>();
            }
            
            String content = Files.readString(Paths.get(GAMES_FILE));
            JSONArray gamesArray = new JSONArray(content);
            List<Game> games = new ArrayList<>();
            
            for (int i = 0; i < gamesArray.length(); i++) {
                JSONObject gameJson = gamesArray.getJSONObject(i);
                String gameName = gameJson.getString("gameName");
                String formattedName = com.mycompany.nocapgameslauncher.resourceHandling.NameFormatting.formatGameName(gameName);
                String imageUrl = gameJson.optString("imageURL", "");
                
                // Convert the path to be relative to the resources folder and fix spaces in filenames
                if (!imageUrl.isEmpty()) {
                    if (imageUrl.startsWith("src/main/resources/")) {
                        imageUrl = imageUrl.substring("src/main/resources/".length());
                    }
                    // Replace spaces with underscores in the filename part of the path
                    int lastSlash = imageUrl.lastIndexOf('/');
                    if (lastSlash >= 0) {
                        String path = imageUrl.substring(0, lastSlash + 1);
                        String filename = imageUrl.substring(lastSlash + 1).replace(" ", "_");
                        imageUrl = path + filename;
                    } else {
                        imageUrl = imageUrl.replace(" ", "_");
                    }
                } else {
                    imageUrl = "ImageResources/" + gameName.toLowerCase() + ".jpg";
                }
                
                games.add(new GameMetadata(
                    formattedName,  // Use the formatted name for display
                    gameJson.optString("gameDescription", ""),
                    imageUrl,
                    i + 1,
                    gameJson.optString("gameURL", "")
                ));
            }
            return games;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load games", e);
        }
    }
}