package com.mycompany.nocapgameslauncher.gui.userManager;

import java.util.Iterator;
import java.util.List;

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
        if (currentMemento == null) {
            currentMemento = UserMemento.loadFromFile();
        }
        return currentMemento != null && currentMemento.shouldRememberMe() ? currentMemento : null;
    }
    
    public static void setCurrentMemento(UserMemento memento) {
        currentMemento = memento;
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
