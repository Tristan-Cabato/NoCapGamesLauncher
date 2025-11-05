package com.mycompany.nocapgameslauncher.iterator;

import java.util.ArrayList;
import java.util.Iterator;

import com.mycompany.nocapgameslauncher.database.DatabaseHandler;

public class UsersIterator implements Iterator<String> {
    private final DatabaseHandler database = DatabaseHandler.getInstance();
    private final ArrayList<String> users;
    private int currentIndex;

    public UsersIterator() {
        this.users = database.getAllUsers();
        this.currentIndex = 1; // Skip Admin
    }

    @Override
    public boolean hasNext() {
        return currentIndex < users.size();
    }

    @Override
    public String next() {
        if (!hasNext()) {
            throw new IndexOutOfBoundsException("No more friends");
        }
        return users.get(currentIndex++);
    }
}
