package com.gitgames.chinesecheckers.service;

public class GameNotFoundException extends RuntimeException {
    public GameNotFoundException(String id) {
        super("No game with id " + id);
    }
}
