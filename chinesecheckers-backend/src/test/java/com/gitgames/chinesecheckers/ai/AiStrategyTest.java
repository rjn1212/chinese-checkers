package com.gitgames.chinesecheckers.ai;

import com.gitgames.chinesecheckers.engine.BoardInitializer;
import com.gitgames.chinesecheckers.engine.MoveValidator;
import com.gitgames.chinesecheckers.model.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AiStrategyTest {

    private final MoveValidator moveValidator = new MoveValidator();

    private List<PlayerSlot> twoPlayerSlots() {
        return List.of(
                new PlayerSlot(PlayerColor.RED, PlayerType.HUMAN, null),
                new PlayerSlot(PlayerColor.GREEN, PlayerType.AI, AiDifficulty.EASY));
    }

    private Board twoPlayerBoard() {
        return BoardInitializer.createBoard(twoPlayerSlots());
    }

    @Test
    void easyAiReturnsValidMove() {
        Board board = twoPlayerBoard();
        EasyAiStrategy ai = new EasyAiStrategy();
        Move move = ai.selectMove(board, PlayerColor.GREEN, twoPlayerSlots());
        assertNotNull(move);
        List<Move> legal = moveValidator.getLegalMoves(board, PlayerColor.GREEN);
        assertTrue(legal.stream().anyMatch(m -> m.getFrom().equals(move.getFrom()) && m.getTo().equals(move.getTo())));
    }

    @Test
    void mediumAiReturnsValidMove() {
        Board board = twoPlayerBoard();
        MediumAiStrategy ai = new MediumAiStrategy();
        Move move = ai.selectMove(board, PlayerColor.GREEN, twoPlayerSlots());
        assertNotNull(move);
        List<Move> legal = moveValidator.getLegalMoves(board, PlayerColor.GREEN);
        assertTrue(legal.stream().anyMatch(m -> m.getFrom().equals(move.getFrom()) && m.getTo().equals(move.getTo())));
    }

    @Test
    void hardAiReturnsValidMove() {
        Board board = twoPlayerBoard();
        HardAiStrategy ai = new HardAiStrategy();
        Move move = ai.selectMove(board, PlayerColor.GREEN, twoPlayerSlots());
        assertNotNull(move);
        List<Move> legal = moveValidator.getLegalMoves(board, PlayerColor.GREEN);
        assertTrue(legal.stream().anyMatch(m -> m.getFrom().equals(move.getFrom()) && m.getTo().equals(move.getTo())));
    }

    @Test
    void mediumAiPrefersAdvancingMoves() {
        Board board = new Board();
        // Place a single GREEN piece in its home triangle
        board.placePiece(new Piece("GREEN_0", PlayerColor.GREEN, 13, 1));

        MediumAiStrategy ai = new MediumAiStrategy();
        List<PlayerSlot> slots = List.of(
                new PlayerSlot(PlayerColor.RED, PlayerType.HUMAN, null),
                new PlayerSlot(PlayerColor.GREEN, PlayerType.AI, AiDifficulty.MEDIUM));
        Move move = ai.selectMove(board, PlayerColor.GREEN, slots);
        assertNotNull(move);
        // Should prefer moving toward goal (RED's home = top triangle)
        assertTrue(move.getTo().row() < 13 || move.getTo().row() == 13);
    }

    @Test
    void hardAiCompletesWithinTimeLimit() {
        Board board = twoPlayerBoard();
        HardAiStrategy ai = new HardAiStrategy();
        long start = System.currentTimeMillis();
        Move move = ai.selectMove(board, PlayerColor.GREEN, twoPlayerSlots());
        long elapsed = System.currentTimeMillis() - start;
        assertNotNull(move);
        assertTrue(elapsed < 5000, "Hard AI took too long: " + elapsed + "ms");
    }

    @Test
    void allAisWorkWithSixPlayers() {
        List<PlayerSlot> slots = List.of(
                new PlayerSlot(PlayerColor.RED, PlayerType.HUMAN, null),
                new PlayerSlot(PlayerColor.ORANGE, PlayerType.AI, AiDifficulty.EASY),
                new PlayerSlot(PlayerColor.YELLOW, PlayerType.AI, AiDifficulty.MEDIUM),
                new PlayerSlot(PlayerColor.GREEN, PlayerType.AI, AiDifficulty.HARD),
                new PlayerSlot(PlayerColor.BLUE, PlayerType.AI, AiDifficulty.EASY),
                new PlayerSlot(PlayerColor.PURPLE, PlayerType.AI, AiDifficulty.MEDIUM));
        Board board = BoardInitializer.createBoard(slots);

        assertNotNull(new EasyAiStrategy().selectMove(board, PlayerColor.ORANGE, slots));
        assertNotNull(new MediumAiStrategy().selectMove(board, PlayerColor.YELLOW, slots));
        assertNotNull(new HardAiStrategy().selectMove(board, PlayerColor.GREEN, slots));
    }
}
