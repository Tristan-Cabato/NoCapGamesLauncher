package com.mycompany.nocapgameslauncher.userManager;

import java.util.ArrayList;
import java.util.Iterator;

import com.mycompany.nocapgameslauncher.database.DatabaseHandler;

public class FriendsIterator implements Iterator<String> {
    private final DatabaseHandler database = DatabaseHandler.getInstance();
    private final ArrayList<String> friends;
    private int currentIndex;

    public FriendsIterator() {
        this.friends = database.getAllUsers();
        this.currentIndex = 1; // Skip Admin
    }

    @Override
    public boolean hasNext() {
        return currentIndex < friends.size();
    }

    @Override
    public String next() {
        if (!hasNext()) {
            throw new IndexOutOfBoundsException("No more friends");
        }
        return friends.get(currentIndex++);
    }
}
