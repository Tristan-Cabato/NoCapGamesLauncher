package com.mycompany.nocapgameslauncher.game_manager;

import com.mycompany.nocapgameslauncher.database.DatabaseHandler;
import com.mycompany.nocapgameslauncher.userManager.UserGameData;
import java.io.Serializable;

public class GameMetadata implements Game, Serializable {
    private static final long serialVersionUID = 1L;
    private final String title;
    private final String description;
    private final String imageUrl;
    private final int id;
    private final String gameUrl;
    private int playCount;
    private long lastPlayed;

    public GameMetadata(String title, String description, String imageUrl, int id, String gameUrl) {
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.id = id;
        this.gameUrl = gameUrl;
        this.playCount = 0;
        this.lastPlayed = 0;
    }

    @Override
    public int getID() { return id; }
    @Override
    public String getTitle() { return title; }
    @Override
    public String getDescription() { return description; }
    @Override
    public String getImageUrl() { return imageUrl; }
    @Override
    public String getGameUrl() { return gameUrl; }
    @Override
    public int getPlayCount() { return playCount; }
    @Override
    public long getLastPlayed() { return lastPlayed; }

    @Override
    public void incrementPlayCount() {
        this.playCount++;
        this.lastPlayed = System.currentTimeMillis();
        saveGameStats();
    }

    @Override
    public void loadGameStats() {
        try {
            String currentUser = DatabaseHandler.getCurrentUser();
            if (currentUser != null) {
                UserGameData.GameStats stats = UserGameData.loadForUser(currentUser)
                    .getGameStats(id);
                if (stats != null) {
                    this.playCount = stats.getPlayCount();
                    this.lastPlayed = stats.getLastPlayed();
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading game stats: " + e.getMessage());
        }
    }

    private void saveGameStats() {
        try {
            String currentUser = DatabaseHandler.getCurrentUser();
            if (currentUser != null) {
                UserGameData userData = UserGameData.loadForUser(currentUser);
                userData.updateGameStats(id, playCount, lastPlayed);
            }
        } catch (Exception e) {
            System.err.println("Error saving game stats: " + e.getMessage());
        }
    }
}