package com.mycompany.nocapgameslauncher.gui.userManager;

import java.util.ArrayList;
import java.util.Iterator;

import com.mycompany.nocapgameslauncher.database.DatabaseHandler;

public class FriendsIterator implements Iterator<String> {
    private final ArrayList<String> friends;
    private int currentIndex;

    public FriendsIterator() {
        this.friends = DatabaseHandler.getAllUsers();
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
