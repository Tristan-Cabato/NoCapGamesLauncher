package com.mycompany.nocapgameslauncher.game_manager;

import java.util.List;

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
    
    public void saveGames(List<Game> games) {
        GameRepository.saveGames(games);
    }
    
    public Game createGame(String title, String description, String imageUrl, int id, String gameUrl) {
        return new GameMetadata(title, description, imageUrl, id, gameUrl);
    }
    
    public Game getGameById(int gameId) {
        List<Game> games = getAllGames();
        for (Game game : games) {
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
