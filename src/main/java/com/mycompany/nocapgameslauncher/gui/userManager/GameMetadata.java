package com.mycompany.nocapgameslauncher.gui.userManager;

import java.time.LocalDateTime;

public class GameMetadata {
    private final String gameId;
    private final String gameName;
    private final String executablePath;
    private final LocalDateTime lastPlayed;
    private final long playTime;

    public GameMetadata(String gameId, String gameName, String executablePath, LocalDateTime lastPlayed, long playTime) {
        this.gameId = gameId;
        this.gameName = gameName;
        this.executablePath = executablePath;
        this.lastPlayed = lastPlayed;
        this.playTime = playTime;
    }

    public String getGameId() {
        return gameId;
    }

    public String getGameName() {
        return gameName;
    }

    public String getExecutablePath() {
        return executablePath;
    }

    public LocalDateTime getLastPlayed() {
        return lastPlayed;
    }

    public long getPlayTime() {
        return playTime;
    }
}
