package com.mycompany.nocapgameslauncher.gui.userManager;

import java.time.*;
import java.util.*;

public class UserGameData {
    private final String userId;
    private final List<GameMetadata> games;
    private final LocalDateTime lastUpdated;

    public UserGameData(String userId) {
        this.userId = userId;
        this.games = new ArrayList<>();
        this.lastUpdated = LocalDateTime.now();
    }

    public String getUserId() {
        return userId;
    }

    public List<GameMetadata> getGames() {
        return games;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }
}