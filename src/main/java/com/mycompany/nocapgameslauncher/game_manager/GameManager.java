package com.mycompany.nocapgameslauncher.game_manager;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import com.mycompany.nocapgameslauncher.iterator.GameIterator;

public class GameManager {
    private static GameManager instance;
    
    private GameManager() {}
    
    public static synchronized GameManager getInstance() {
        if (instance == null) {
            instance = new GameManager();
        }
        return instance;
    }
    
    public List<Game> getAllGames() {
        List<Game> games = GameRepository.loadGames();
        // Load stats for each game
        games.forEach(Game::loadGameStats);
        return games;
    }
    
    public GameIterator getGameIterator() {
        return new GameIterator(getAllGames());
    }
    
    public void saveGames(List<Game> games) {
        GameRepository.saveGames(games);
    }
    
    public Game createGame(String title, String description, String imageUrl, int id, String gameUrl) {
        Game game = new GameMetadata(title, description, imageUrl, id, gameUrl);
        game.loadGameStats(); // Load any existing stats for this game
        return game;
    }
    
    public Game getGameById(int gameId) {
        Game game = GameRepository.getGameById(gameId);
        if (game != null) {
            game.loadGameStats();
        }
        return game;
    }
    
    public int getGameCount() {
        return getAllGames().size();
    }
}