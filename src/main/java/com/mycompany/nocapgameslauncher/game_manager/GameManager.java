package com.mycompany.nocapgameslauncher.game_manager;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Facade for the game_manager subsystem.
 * Provides simplified access to game loading, saving, and iteration.
 */
public class GameManager {
    private static GameManager instance;
    
    private GameManager() {}
    
    public static GameManager getInstance() {
        if (instance == null) {
            instance = new GameManager();
        }
        return instance;
    }
    
    public List<Game> getAllGames() {
        return GameRepository.loadGames();
    }
    
    public GameIterator getGameIterator() {
        return new GameIterator(GameRepository.loadGames());
    }

    public Map<String, String> getGameDescriptions(InputStream gameDataStream) throws IOException {
        return GameDescriptionIterator.fromJson(gameDataStream).toMap();
    }
    
    public void saveGames(List<Game> games) {
        GameRepository.saveGames(games);
    }
    
    public Game createGame(String title, String description, String imageUrl, int id, String gameUrl) {
        return new GameMetadata(title, description, imageUrl, id, gameUrl);
    }
    
    public Game getGameById(int gameId) {
        GameIterator iterator = getGameIterator();
        while (iterator.hasNext()) {
            Game game = iterator.next();
            if (game.getID() == gameId) {
                return game;
            }
        }
        return null;
    }
    
    public int getGameCount() {
        return getAllGames().size();
    }
}
