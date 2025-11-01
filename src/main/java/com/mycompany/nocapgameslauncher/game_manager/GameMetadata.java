package com.mycompany.nocapgameslauncher.game_manager;

public class GameMetadata implements Game {
    private final String title;
    private final String description;
    private final String imageUrl;
    private final int id;
    
    public GameMetadata(String title, String description, String imageUrl, int id) {
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.id = id;
    }
    
    @Override public int getID() { return id; }
    @Override public String getTitle() { return title; }
    @Override public String getDescription() { return description; }
    @Override public String getImageUrl() { return imageUrl; }
}