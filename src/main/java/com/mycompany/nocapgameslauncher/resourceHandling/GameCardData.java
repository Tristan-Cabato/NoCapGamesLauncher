package com.mycompany.nocapgameslauncher.resourceHandling;

public class GameCardData {
    public final String title;
    public final String description;
    public final String iconPath;
    public final int gameId;

    public GameCardData(String title, String description, String iconPath, int gameId) {
        this.title = title;
        this.description = description;
        this.iconPath = iconPath;
        this.gameId = gameId;
    }
}
