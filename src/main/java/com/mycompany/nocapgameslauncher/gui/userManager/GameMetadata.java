package com.mycompany.nocapgameslauncher.gui.userManager;

public class GameMetadata {
    private final String gameId;
    private final String gameName;
    private final String executablePath;
    private final String imagePath;
    private final String gameDescription;

    public GameMetadata(String gameId, String gameName, String executablePath, String imagePath, String gameDescription) {
        this.gameId = gameId;
        this.gameName = gameName;
        this.executablePath = executablePath;
        this.imagePath = imagePath;
        this.gameDescription = gameDescription;
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

    public String getImagePath() {
        return imagePath;
    }

    public String getGameDescription() {
        return gameDescription;
    }
}
