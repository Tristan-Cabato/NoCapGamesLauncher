package com.mycompany.nocapgameslauncher.iterator;

import com.mycompany.nocapgameslauncher.userManager.UserRepository;
import java.util.*;

public class FriendsIterator implements Iterator<String> {
    private final Iterator<String> friendsIterator;
    private final UserRepository userRepository;

    public FriendsIterator(Set<String> friendUsernames, UserRepository userRepository) {
        this.friendsIterator = friendUsernames.iterator();
        this.userRepository = userRepository;
    }

    @Override
    public boolean hasNext() {
        return friendsIterator.hasNext();
    }

    @Override
    public String next() {
        String friendUsername = friendsIterator.next();
        return friendUsername;
    }
}