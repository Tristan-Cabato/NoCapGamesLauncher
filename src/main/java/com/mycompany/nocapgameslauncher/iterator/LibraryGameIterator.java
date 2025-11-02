package com.mycompany.nocapgameslauncher.iterator;

import com.mycompany.nocapgameslauncher.resourceHandling.GameCardData;
import com.mycompany.nocapgameslauncher.resourceHandling.NameFormatting;
import com.mycompany.nocapgameslauncher.resourceHandling.resourceLoader;


import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class LibraryGameIterator implements Iterator<GameCardData> {
    private final Iterator<Integer> gameIdIterator;

    public LibraryGameIterator(List<Integer> gameIds) {
        this.gameIdIterator = gameIds.iterator();
    }

    @Override
    public boolean hasNext() {
        return gameIdIterator.hasNext();
    }

    @Override
    public GameCardData next() {
        int gameId = gameIdIterator.next();
        Map<String, String> gameDetails = resourceLoader.getGameById(gameId);
        if (gameDetails == null) {
            return null;
        }
        
        String title = NameFormatting.formatGameName(gameDetails.get("gameName"));
        String description = gameDetails.get("gameDescription");
        String iconPath = "ImageResources/" + title.toLowerCase().replace(" ", "_") + ".jpg";
        
        return new GameCardData(title, description, iconPath, gameId);
    }
}