package com.gitgames.chinesecheckers.engine;

import com.gitgames.chinesecheckers.model.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BoardInitializerTest {

    @Test
    void twoPlayerSetupPlaces20Pieces() {
        List<PlayerSlot> slots = List.of(
                new PlayerSlot(PlayerColor.RED, PlayerType.HUMAN, null),
                new PlayerSlot(PlayerColor.GREEN, PlayerType.AI, AiDifficulty.EASY));
        Board board = BoardInitializer.createBoard(slots);
        assertEquals(20, board.getAllPieces().size());
        assertEquals(10, board.getPiecesForPlayer(PlayerColor.RED).size());
        assertEquals(10, board.getPiecesForPlayer(PlayerColor.GREEN).size());
    }

    @Test
    void threePlayerSetupPlaces30Pieces() {
        List<PlayerSlot> slots = List.of(
                new PlayerSlot(PlayerColor.RED, PlayerType.HUMAN, null),
                new PlayerSlot(PlayerColor.YELLOW, PlayerType.AI, AiDifficulty.EASY),
                new PlayerSlot(PlayerColor.BLUE, PlayerType.AI, AiDifficulty.EASY));
        Board board = BoardInitializer.createBoard(slots);
        assertEquals(30, board.getAllPieces().size());
    }

    @Test
    void fourPlayerSetupPlaces40Pieces() {
        List<PlayerSlot> slots = List.of(
                new PlayerSlot(PlayerColor.RED, PlayerType.HUMAN, null),
                new PlayerSlot(PlayerColor.ORANGE, PlayerType.AI, AiDifficulty.EASY),
                new PlayerSlot(PlayerColor.GREEN, PlayerType.AI, AiDifficulty.EASY),
                new PlayerSlot(PlayerColor.BLUE, PlayerType.AI, AiDifficulty.EASY));
        Board board = BoardInitializer.createBoard(slots);
        assertEquals(40, board.getAllPieces().size());
    }

    @Test
    void sixPlayerSetupPlaces60Pieces() {
        List<PlayerSlot> slots = List.of(
                new PlayerSlot(PlayerColor.RED, PlayerType.HUMAN, null),
                new PlayerSlot(PlayerColor.ORANGE, PlayerType.AI, AiDifficulty.EASY),
                new PlayerSlot(PlayerColor.YELLOW, PlayerType.AI, AiDifficulty.EASY),
                new PlayerSlot(PlayerColor.GREEN, PlayerType.AI, AiDifficulty.EASY),
                new PlayerSlot(PlayerColor.BLUE, PlayerType.AI, AiDifficulty.EASY),
                new PlayerSlot(PlayerColor.PURPLE, PlayerType.AI, AiDifficulty.EASY));
        Board board = BoardInitializer.createBoard(slots);
        assertEquals(60, board.getAllPieces().size());
    }

    @Test
    void piecesArePlacedInHomeTriangles() {
        List<PlayerSlot> slots = List.of(
                new PlayerSlot(PlayerColor.RED, PlayerType.HUMAN, null),
                new PlayerSlot(PlayerColor.GREEN, PlayerType.AI, AiDifficulty.EASY));
        Board board = BoardInitializer.createBoard(slots);

        for (Piece piece : board.getPiecesForPlayer(PlayerColor.RED)) {
            assertTrue(Board.getHomeTriangle(PlayerColor.RED).contains(piece.getPosition()),
                    "RED piece should be in RED home triangle: " + piece.getPosition());
        }
        for (Piece piece : board.getPiecesForPlayer(PlayerColor.GREEN)) {
            assertTrue(Board.getHomeTriangle(PlayerColor.GREEN).contains(piece.getPosition()),
                    "GREEN piece should be in GREEN home triangle: " + piece.getPosition());
        }
    }

    @Test
    void invalidPlayerCountThrows() {
        assertThrows(IllegalArgumentException.class, () ->
                BoardInitializer.createBoard(List.of(
                        new PlayerSlot(PlayerColor.RED, PlayerType.HUMAN, null))));
    }

    @Test
    void invalidColorForPlayerCountThrows() {
        assertThrows(IllegalArgumentException.class, () ->
                BoardInitializer.createBoard(List.of(
                        new PlayerSlot(PlayerColor.RED, PlayerType.HUMAN, null),
                        new PlayerSlot(PlayerColor.ORANGE, PlayerType.AI, AiDifficulty.EASY))));
    }

    @Test
    void validColorsForTwoPlayers() {
        List<PlayerColor> colors = BoardInitializer.getValidColorsForPlayerCount(2);
        assertEquals(List.of(PlayerColor.RED, PlayerColor.GREEN), colors);
    }
}
