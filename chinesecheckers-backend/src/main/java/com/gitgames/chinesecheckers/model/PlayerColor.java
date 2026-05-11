package com.gitgames.chinesecheckers.model;

public enum PlayerColor {
    RED,
    ORANGE,
    YELLOW,
    GREEN,
    BLUE,
    PURPLE;

    public PlayerColor opposite() {
        return values()[(this.ordinal() + 3) % 6];
    }

    public PlayerColor next() {
        return values()[(this.ordinal() + 1) % 6];
    }
}
