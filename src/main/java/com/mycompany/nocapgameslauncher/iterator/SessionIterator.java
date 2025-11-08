package com.mycompany.nocapgameslauncher.iterator;

import java.util.Iterator;
import java.util.List;

import com.mycompany.nocapgameslauncher.userManager.UserMemento;

public class SessionIterator implements Iterator<UserMemento> {
    private List<UserMemento> sessionList;
    private int currentIndex;
    private static UserMemento currentMemento;

    public SessionIterator(List<UserMemento> sessionList) {
        this.sessionList = sessionList;
        this.currentIndex = 0;
    }

    @Override
    public boolean hasNext() {
        return currentIndex < sessionList.size();
    }

    @Override
    public UserMemento next() {
        if (!hasNext()) {
            throw new java.util.NoSuchElementException();
        }
        currentMemento = sessionList.get(currentIndex);
        currentIndex++;
        return currentMemento;
    }
    
    public static UserMemento getCurrentMemento() {
        // Always try to load from file to ensure we have the latest session
        UserMemento loadedMemento = UserMemento.loadFromFile();
        if (loadedMemento != null && loadedMemento.shouldRememberMe()) {
            currentMemento = loadedMemento;
        }
        return currentMemento;
    }
    
    public static void setCurrentMemento(UserMemento memento) {
        currentMemento = memento;
        if (memento != null) {
            memento.saveToFile();
        } else {
            // Clear the session file if memento is null
            UserMemento.clearSession();
        }
    }
    
    public static void saveCurrentMemento() {
        if (currentMemento != null) {
            currentMemento.saveToFile();
        }
    }
    
    public static void clearCurrentSession() {
        currentMemento = null;
        UserMemento.clearSession();
    }
}
