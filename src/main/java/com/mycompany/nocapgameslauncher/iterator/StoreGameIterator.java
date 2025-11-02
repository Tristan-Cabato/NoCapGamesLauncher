package com.mycompany.nocapgameslauncher.iterator;

import com.mycompany.nocapgameslauncher.game_manager.Game;
import com.mycompany.nocapgameslauncher.resourceHandling.GameCardData;

import java.util.Iterator;
import java.util.List;

public class StoreGameIterator implements Iterator<GameCardData> {
    private final Iterator<Game> gameIterator;

    public StoreGameIterator(List<Game> games) {
        this.gameIterator = games.iterator();
    }

    @Override
    public boolean hasNext() {
        return gameIterator.hasNext();
    }

    @Override
    public GameCardData next() {
        Game game = gameIterator.next();
        String title = game.getTitle();
        String iconPath = "ImageResources/" + title.toLowerCase().replace(" ", "_") + ".jpg";
        return new GameCardData(
            title,
            game.getDescription(),
            iconPath,
            game.getID()
        );
    }
}