package com.mycompany.nocapgameslauncher.game_manager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

public class GameRepository {
    private static final String GAMES_FILE = "src/main/resources/store_games.json";
    
    public static List<Game> loadGames() {
        List<Game> games = new ArrayList<>();
        try {
            String content = new String(Files.readAllBytes(Paths.get(GAMES_FILE)));
            JSONArray gamesArray = new JSONArray(content);
            
            for (int i = 0; i < gamesArray.length(); i++) {
                JSONObject gameJson = gamesArray.getJSONObject(i);
                Game game = new GameMetadata(
                    gameJson.getString("gameName"),
                    gameJson.optString("gameDescription", ""),
                    gameJson.optString("imageURL", ""),
                    gameJson.getInt("gameID"),
                    gameJson.optString("gameURL", "")
                );
                games.add(game);
            }
        } catch (IOException e) {
            System.err.println("Error loading games: " + e.getMessage());
        }
        return games;
    }
    
    public static Game getGameById(int gameId) {
        try {
            String content = new String(Files.readAllBytes(Paths.get(GAMES_FILE)));
            JSONArray gamesArray = new JSONArray(content);
            
            for (int i = 0; i < gamesArray.length(); i++) {
                JSONObject gameJson = gamesArray.getJSONObject(i);
                if (gameJson.getInt("gameID") == gameId) {
                    return new GameMetadata(
                        gameJson.getString("gameName"),
                        gameJson.optString("gameDescription", ""),
                        gameJson.optString("imageURL", ""),
                        gameJson.getInt("gameID"),
                        gameJson.optString("gameURL", "")
                    );
                }
            }
        } catch (IOException e) {
            System.err.println("Error finding game: " + e.getMessage());
        }
        return null;
    }
    
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
            Files.write(Paths.get(GAMES_FILE), gamesArray.toString(4).getBytes());
        } catch (IOException e) {
            System.err.println("Error saving games: " + e.getMessage());
        }
    }
}