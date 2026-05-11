package com.gitgames.chinesecheckers.model;

import java.util.List;

public class Move {

    private final Position from;
    private final Position to;
    private final List<Position> path;

    public Move(Position from, Position to, List<Position> path) {
        this.from = from;
        this.to = to;
        this.path = List.copyOf(path);
    }

    public static Move step(Position from, Position to) {
        return new Move(from, to, List.of(to));
    }

    public Position getFrom() {
        return from;
    }

    public Position getTo() {
        return to;
    }

    public List<Position> getPath() {
        return path;
    }

    public boolean isHop() {
        return path.size() >= 1 && !isStep();
    }

    public boolean isStep() {
        return path.size() == 1 && Board.areAdjacent(from, to);
    }
}
