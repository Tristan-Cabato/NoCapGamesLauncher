package com.mycompany.nocapgameslauncher.userManager;

import com.google.gson.Gson;
import com.mycompany.nocapgameslauncher.resourceHandling.resourceLoader;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class UserDataIterator implements Iterator<Integer> {
    private final List<Integer> ownedGameIds;
    private int currentIndex = 0;

    public UserDataIterator(String username) {
        this.ownedGameIds = loadUserGameIds(username);
    }

    @Override
    public boolean hasNext() {
        return currentIndex < ownedGameIds.size();
    }

    @Override
    public Integer next() {
        if (!hasNext()) {
            throw new NoSuchElementException("No more game IDs available");
        }
        return ownedGameIds.get(currentIndex++);
    }

    public List<Integer> getOwnedGameIds() {
        return Collections.unmodifiableList(ownedGameIds);
    }

    public boolean ownsGame(int gameId) {
        return ownedGameIds.contains(gameId);
    }

    public int getGameCount() {
        return ownedGameIds.size();
    }

    private List<Integer> loadUserGameIds(String username) {
        if (username == null || username.isEmpty()) {
            return Collections.emptyList();
        }

        String userJsonPath = resourceLoader.RESOURCE_DIRECTORY + "Users/" + username + ".json";
        try (FileReader reader = new FileReader(userJsonPath)) {
            Gson gson = new Gson();
            Map<String, Object> userData = gson.fromJson(reader, Map.class);
            
            @SuppressWarnings("unchecked")
            List<Double> ownedGameIdsDouble = (List<Double>) userData.get("ownedGameIds");
            
            if (ownedGameIdsDouble == null) {
                return Collections.emptyList();
            }
            
            List<Integer> ids = new ArrayList<>(ownedGameIdsDouble.size());
            for (Double id : ownedGameIdsDouble) {
                ids.add(id.intValue());
            }
            return ids;
            
        } catch (IOException e) {
            System.err.println("Error loading user data for " + username + ": " + e.getMessage());
            return Collections.emptyList();
        }
    }
}
