package com.gitgames.chinesecheckers.engine;

import com.gitgames.chinesecheckers.model.*;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class WinConditionCheckerTest {

    private final WinConditionChecker checker = new WinConditionChecker();

    @Test
    void playerNotFinishedWhenPiecesInHome() {
        Board board = new Board();
        int i = 0;
        for (Position pos : Board.getHomeTriangle(PlayerColor.RED)) {
            board.placePiece(new Piece("RED_" + i, PlayerColor.RED, pos.row(), pos.col()));
            i++;
        }
        assertFalse(checker.hasPlayerFinished(board, PlayerColor.RED));
    }

    @Test
    void playerFinishedWhenAllPiecesInGoal() {
        Board board = new Board();
        Set<Position> goal = Board.getGoalTriangle(PlayerColor.RED);
        int i = 0;
        for (Position pos : goal) {
            board.placePiece(new Piece("RED_" + i, PlayerColor.RED, pos.row(), pos.col()));
            i++;
        }
        assertTrue(checker.hasPlayerFinished(board, PlayerColor.RED));
    }

    @Test
    void playerNotFinishedWithPartialGoalOccupancy() {
        Board board = new Board();
        Set<Position> goal = Board.getGoalTriangle(PlayerColor.RED);
        int i = 0;
        for (Position pos : goal) {
            if (i < 9) {
                board.placePiece(new Piece("RED_" + i, PlayerColor.RED, pos.row(), pos.col()));
            }
            i++;
        }
        // Place the 10th piece outside the goal
        board.placePiece(new Piece("RED_9", PlayerColor.RED, 8, 4));
        assertFalse(checker.hasPlayerFinished(board, PlayerColor.RED));
    }

    @Test
    void gameOverWhenAllButOneFinished() {
        Board board = new Board();
        var slots = java.util.List.of(
                new PlayerSlot(PlayerColor.RED, PlayerType.HUMAN, null),
                new PlayerSlot(PlayerColor.GREEN, PlayerType.AI, AiDifficulty.EASY));
        Game game = new Game("test", board, slots);

        game.markPlayerFinished(PlayerColor.RED);
        assertTrue(checker.isGameOver(game));
    }

    @Test
    void gameNotOverWhenMultiplePlayersRemain() {
        Board board = new Board();
        var slots = java.util.List.of(
                new PlayerSlot(PlayerColor.RED, PlayerType.HUMAN, null),
                new PlayerSlot(PlayerColor.YELLOW, PlayerType.AI, AiDifficulty.EASY),
                new PlayerSlot(PlayerColor.BLUE, PlayerType.AI, AiDifficulty.EASY));
        Game game = new Game("test", board, slots);

        game.markPlayerFinished(PlayerColor.RED);
        assertFalse(checker.isGameOver(game));
    }
}
