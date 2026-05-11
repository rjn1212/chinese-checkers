package com.gitgames.chinesecheckers.controller.dto;

import com.gitgames.chinesecheckers.model.Position;

public record PositionDto(int row, int col) {
    public static PositionDto from(Position p) {
        return new PositionDto(p.row(), p.col());
    }

    public Position toPosition() {
        return new Position(row, col);
    }
}
