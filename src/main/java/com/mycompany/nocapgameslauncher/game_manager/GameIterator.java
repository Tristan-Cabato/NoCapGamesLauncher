package com.mycompany.nocapgameslauncher.game_manager;

import java.util.Iterator;
import java.util.List;

public class GameIterator implements Iterator<Game> {
    private final List<Game> games;
    private int position = 0;
    
    public GameIterator(List<Game> games) {
        this.games = List.copyOf(games);
    }
    
    @Override
    public boolean hasNext() {
        return position < games.size();
    }
    
    @Override
    public Game next() {
        if (!hasNext()) {
            throw new java.util.NoSuchElementException("No more games");
        }
        return games.get(position++);
    }
}
