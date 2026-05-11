package com.gitgames.chinesecheckers.model;

public record Position(int row, int col) {

    public boolean isValid() {
        if (row < 0 || row > 16) return false;
        int maxCol = Board.getPositionCountForRow(row);
        return maxCol > 0 && col >= 0 && col < maxCol;
    }
}
