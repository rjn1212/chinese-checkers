package com.gitgames.chinesecheckers.engine;

import com.gitgames.chinesecheckers.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class GameEngineTest {

    private GameEngine engine;

    @BeforeEach
    void setUp() {
        engine = new GameEngine();
    }

    @Test
    void applyMoveUpdatesPiecePosition() {
        Board board = new Board();
        Piece piece = new Piece("RED_0", PlayerColor.RED, 0, 0);
        board.placePiece(piece);

        Move move = Move.step(new Position(0, 0), new Position(1, 0));
        engine.applyMove(board, move);

        assertNull(board.getPieceAt(new Position(0, 0)));
        assertNotNull(board.getPieceAt(new Position(1, 0)));
        assertEquals(1, piece.getRow());
        assertEquals(0, piece.getCol());
    }

    @Test
    void validateAndFindMoveReturnsValidMove() {
        Board board = new Board();
        Piece piece = new Piece("RED_0", PlayerColor.RED, 0, 0);
        board.placePiece(piece);

        Move move = engine.validateAndFindMove(board, PlayerColor.RED, new Position(0, 0), new Position(1, 0));
        assertNotNull(move);
    }

    @Test
    void validateAndFindMoveReturnsNullForWrongPlayer() {
        Board board = new Board();
        Piece piece = new Piece("RED_0", PlayerColor.RED, 0, 0);
        board.placePiece(piece);

        Move move = engine.validateAndFindMove(board, PlayerColor.GREEN, new Position(0, 0), new Position(1, 0));
        assertNull(move);
    }

    @Test
    void validateAndFindMoveReturnsNullForEmptySquare() {
        Board board = new Board();
        Move move = engine.validateAndFindMove(board, PlayerColor.RED, new Position(0, 0), new Position(1, 0));
        assertNull(move);
    }

    @Test
    void validateAndFindMoveReturnsNullForInvalidDestination() {
        Board board = new Board();
        Piece piece = new Piece("RED_0", PlayerColor.RED, 0, 0);
        board.placePiece(piece);

        Move move = engine.validateAndFindMove(board, PlayerColor.RED, new Position(0, 0), new Position(5, 5));
        assertNull(move);
    }

    @Test
    void checkAndHandleWinDetectsFinish() {
        Board board = new Board();
        Set<Position> goal = Board.getGoalTriangle(PlayerColor.RED);
        int i = 0;
        for (Position pos : goal) {
            board.placePiece(new Piece("RED_" + i, PlayerColor.RED, pos.row(), pos.col()));
            i++;
        }

        List<PlayerSlot> slots = List.of(
                new PlayerSlot(PlayerColor.RED, PlayerType.HUMAN, null),
                new PlayerSlot(PlayerColor.GREEN, PlayerType.AI, AiDifficulty.EASY));
        Game game = new Game("test", board, slots);

        assertTrue(engine.checkAndHandleWin(game, PlayerColor.RED));
        assertEquals(PlayerColor.RED, game.getWinner());
    }

    @Test
    void checkAndHandleWinReturnsFalseWhenNotFinished() {
        Board board = new Board();
        board.placePiece(new Piece("RED_0", PlayerColor.RED, 8, 4));

        List<PlayerSlot> slots = List.of(
                new PlayerSlot(PlayerColor.RED, PlayerType.HUMAN, null),
                new PlayerSlot(PlayerColor.GREEN, PlayerType.AI, AiDifficulty.EASY));
        Game game = new Game("test", board, slots);

        assertFalse(engine.checkAndHandleWin(game, PlayerColor.RED));
        assertNull(game.getWinner());
    }

    @Test
    void getLegalMovesReturnsMoves() {
        Board board = new Board();
        board.placePiece(new Piece("RED_0", PlayerColor.RED, 8, 4));

        List<Move> moves = engine.getLegalMoves(board, PlayerColor.RED);
        assertFalse(moves.isEmpty());
    }
}
