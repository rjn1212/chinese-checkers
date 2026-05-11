package com.gitgames.chinesecheckers.model;

import java.util.List;

public record MoveRecord(
        PlayerColor player,
        Position from,
        Position to,
        List<Position> path,
        int moveNumber
) {
    public static MoveRecord from(PlayerColor player, Move move, int moveNumber) {
        return new MoveRecord(player, move.getFrom(), move.getTo(), move.getPath(), moveNumber);
    }
}
