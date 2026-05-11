package com.gitgames.chinesecheckers.engine;

import com.gitgames.chinesecheckers.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class MoveValidatorTest {

    private MoveValidator validator;

    @BeforeEach
    void setUp() {
        validator = new MoveValidator();
    }

    @Test
    void stepMoveToEmptyAdjacentPosition() {
        Board board = new Board();
        Piece piece = new Piece("RED_0", PlayerColor.RED, 0, 0);
        board.placePiece(piece);

        List<Move> moves = validator.getStepMoves(board, piece);
        assertEquals(2, moves.size());
        assertTrue(moves.stream().anyMatch(m -> m.getTo().equals(new Position(1, 0))));
        assertTrue(moves.stream().anyMatch(m -> m.getTo().equals(new Position(1, 1))));
    }

    @Test
    void stepMoveBlockedByOccupiedPosition() {
        Board board = new Board();
        Piece piece = new Piece("RED_0", PlayerColor.RED, 0, 0);
        Piece blocker = new Piece("RED_1", PlayerColor.RED, 1, 0);
        board.placePiece(piece);
        board.placePiece(blocker);

        List<Move> moves = validator.getStepMoves(board, piece);
        assertEquals(1, moves.size());
        assertEquals(new Position(1, 1), moves.get(0).getTo());
    }

    @Test
    void hopOverAdjacentPiece() {
        Board board = new Board();
        Piece piece = new Piece("RED_0", PlayerColor.RED, 0, 0);
        Piece jumped = new Piece("GREEN_0", PlayerColor.GREEN, 1, 0);
        board.placePiece(piece);
        board.placePiece(jumped);

        List<Move> hops = validator.getHopMoves(board, piece);
        assertTrue(hops.stream().anyMatch(m -> m.getTo().equals(new Position(2, 0))));
    }

    @Test
    void hopBlockedWhenLandingOccupied() {
        Board board = new Board();
        Piece piece = new Piece("RED_0", PlayerColor.RED, 0, 0);
        Piece jumped = new Piece("GREEN_0", PlayerColor.GREEN, 1, 0);
        Piece blocker = new Piece("BLUE_0", PlayerColor.BLUE, 2, 0);
        board.placePiece(piece);
        board.placePiece(jumped);
        board.placePiece(blocker);

        List<Move> hops = validator.getHopMoves(board, piece);
        assertFalse(hops.stream().anyMatch(m -> m.getTo().equals(new Position(2, 0))));
    }

    @Test
    void chainHopProducesMultipleMoves() {
        Board board = new Board();
        // Set up a chain: piece at (2,0), pieces to hop over at (2,1) and further
        Piece piece = new Piece("RED_0", PlayerColor.RED, 2, 0);
        Piece hop1 = new Piece("GREEN_0", PlayerColor.GREEN, 2, 1);
        board.placePiece(piece);
        board.placePiece(hop1);

        List<Move> hops = validator.getHopMoves(board, piece);
        // Should be able to hop over (2,1) to land at (2,2)
        assertTrue(hops.stream().anyMatch(m -> m.getTo().equals(new Position(2, 2))));
    }

    @Test
    void hopOverOwnPieceIsAllowed() {
        Board board = new Board();
        Piece piece = new Piece("RED_0", PlayerColor.RED, 0, 0);
        Piece ownPiece = new Piece("RED_1", PlayerColor.RED, 1, 0);
        board.placePiece(piece);
        board.placePiece(ownPiece);

        List<Move> hops = validator.getHopMoves(board, piece);
        assertTrue(hops.stream().anyMatch(m -> m.getTo().equals(new Position(2, 0))));
    }

    @Test
    void noMovesWhenCompletelyBlocked() {
        Board board = new Board();
        Piece piece = new Piece("RED_0", PlayerColor.RED, 0, 0);
        Piece b1 = new Piece("X_0", PlayerColor.GREEN, 1, 0);
        Piece b2 = new Piece("X_1", PlayerColor.GREEN, 1, 1);
        // Block hop landings too
        Piece b3 = new Piece("X_2", PlayerColor.GREEN, 2, 0);
        Piece b4 = new Piece("X_3", PlayerColor.GREEN, 2, 2);
        board.placePiece(piece);
        board.placePiece(b1);
        board.placePiece(b2);
        board.placePiece(b3);
        board.placePiece(b4);

        List<Move> moves = validator.getMovesForPiece(board, piece);
        // All step moves blocked, hop landings blocked
        assertEquals(0, moves.size());
    }

    @Test
    void getLegalMovesReturnsMovesForAllPieces() {
        Board board = new Board();
        Piece p1 = new Piece("RED_0", PlayerColor.RED, 0, 0);
        Piece p2 = new Piece("RED_1", PlayerColor.RED, 2, 0);
        board.placePiece(p1);
        board.placePiece(p2);

        List<Move> moves = validator.getLegalMoves(board, PlayerColor.RED);
        assertTrue(moves.size() > 2);
    }

    @Test
    void goalExitRestrictionPreventsLeavingGoal() {
        Board board = new Board();
        // RED's goal is GREEN's home (bottom triangle, rows 13-16)
        // Place a RED piece in the goal triangle
        Piece piece = new Piece("RED_0", PlayerColor.RED, 13, 0);
        board.placePiece(piece);

        List<Move> moves = validator.getMovesForPiece(board, piece);
        Set<Position> goalTriangle = Board.getGoalTriangle(PlayerColor.RED);

        for (Move move : moves) {
            assertTrue(goalTriangle.contains(move.getTo()),
                    "Move should stay in goal triangle: " + move.getTo());
        }
    }

    @Test
    void pieceNotInGoalCanMoveFreely() {
        Board board = new Board();
        // Place RED piece in center area - should be able to move anywhere adjacent
        Piece piece = new Piece("RED_0", PlayerColor.RED, 8, 4);
        board.placePiece(piece);

        List<Move> moves = validator.getMovesForPiece(board, piece);
        assertEquals(6, moves.size()); // center piece has 6 neighbors, all empty
    }

    @Test
    void findMoveByDestinationReturnsMatch() {
        Board board = new Board();
        Piece piece = new Piece("RED_0", PlayerColor.RED, 0, 0);
        board.placePiece(piece);

        List<Move> moves = validator.getMovesForPiece(board, piece);
        Move found = validator.findMoveByDestination(moves, new Position(0, 0), new Position(1, 0));
        assertNotNull(found);
        assertEquals(new Position(0, 0), found.getFrom());
        assertEquals(new Position(1, 0), found.getTo());
    }

    @Test
    void findMoveByDestinationReturnsNullWhenNoMatch() {
        Board board = new Board();
        Piece piece = new Piece("RED_0", PlayerColor.RED, 0, 0);
        board.placePiece(piece);

        List<Move> moves = validator.getMovesForPiece(board, piece);
        Move found = validator.findMoveByDestination(moves, new Position(0, 0), new Position(5, 5));
        assertNull(found);
    }

    @Test
    void hopChainInHexBody() {
        Board board = new Board();
        // Place a piece in the hex body and create a line of pieces to hop over
        Piece piece = new Piece("RED_0", PlayerColor.RED, 8, 0);
        Piece h1 = new Piece("X_0", PlayerColor.GREEN, 8, 1);
        board.placePiece(piece);
        board.placePiece(h1);

        List<Move> hops = validator.getHopMoves(board, piece);
        assertTrue(hops.stream().anyMatch(m -> m.getTo().equals(new Position(8, 2))));
    }

    @Test
    void stepMoveIsIdentifiedCorrectly() {
        Move step = Move.step(new Position(0, 0), new Position(1, 0));
        assertTrue(step.isStep());
        assertFalse(step.isHop());
    }
}
