package com.gitgames.chinesecheckers.engine;

import com.gitgames.chinesecheckers.model.*;

import java.util.List;
import java.util.Set;

public class WinConditionChecker {

    public boolean hasPlayerFinished(Board board, PlayerColor player) {
        Set<Position> goalTriangle = Board.getGoalTriangle(player);
        List<Piece> pieces = board.getPiecesForPlayer(player);
        if (pieces.size() != 10) return false;
        return pieces.stream().allMatch(p -> goalTriangle.contains(p.getPosition()));
    }

    public boolean isGameOver(Game game) {
        return game.getPhase() == GamePhase.GAME_OVER;
    }
}
