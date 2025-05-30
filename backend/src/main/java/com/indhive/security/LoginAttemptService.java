package com.indhive.security;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {

    private final int MAX_ATTEMPTS = 5;
    private final Map<String, Integer> attemptsCache = new ConcurrentHashMap<>();

    public void loginFailed(String username) {
        int attempts = attemptsCache.getOrDefault(username, 0);
        attemptsCache.put(username, attempts + 1);
    }

    public void loginSucceeded(String username) {
        attemptsCache.remove(username);
    }

    public boolean isBlocked(String username) {
        return attemptsCache.getOrDefault(username, 0) >= MAX_ATTEMPTS;
    }

    public int getAttempts(String username) {
        return attemptsCache.getOrDefault(username, 0);
    }
}
