package com.gitgames.chinesecheckers.model;

public class Piece implements Cloneable {

    private final String id;
    private final PlayerColor owner;
    private int row;
    private int col;

    public Piece(String id, PlayerColor owner, int row, int col) {
        this.id = id;
        this.owner = owner;
        this.row = row;
        this.col = col;
    }

    public String getId() {
        return id;
    }

    public PlayerColor getOwner() {
        return owner;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public Position getPosition() {
        return new Position(row, col);
    }

    public void setPosition(int row, int col) {
        this.row = row;
        this.col = col;
    }

    @Override
    public Piece clone() {
        try {
            return (Piece) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }
}
