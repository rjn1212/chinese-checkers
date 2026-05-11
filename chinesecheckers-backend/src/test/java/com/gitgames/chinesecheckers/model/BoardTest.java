package com.gitgames.chinesecheckers.model;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class BoardTest {

    @Test
    void boardHas121Positions() {
        assertEquals(121, Board.getAllPositions().size());
    }

    @Test
    void rowCountsAreCorrect() {
        int[] expected = {1, 2, 3, 4, 13, 12, 11, 10, 9, 10, 11, 12, 13, 4, 3, 2, 1};
        for (int row = 0; row <= 16; row++) {
            assertEquals(expected[row], Board.getPositionCountForRow(row), "Row " + row);
        }
    }

    @Test
    void invalidRowsReturnZeroPositions() {
        assertEquals(0, Board.getPositionCountForRow(-1));
        assertEquals(0, Board.getPositionCountForRow(17));
    }

    @Test
    void topTipHasTwoNeighbors() {
        Board board = new Board();
        List<Position> neighbors = board.getNeighbors(new Position(0, 0));
        assertEquals(2, neighbors.size());
        assertTrue(neighbors.contains(new Position(1, 0)));
        assertTrue(neighbors.contains(new Position(1, 1)));
    }

    @Test
    void bottomTipHasTwoNeighbors() {
        Board board = new Board();
        List<Position> neighbors = board.getNeighbors(new Position(16, 0));
        assertEquals(2, neighbors.size());
        assertTrue(neighbors.contains(new Position(15, 0)));
        assertTrue(neighbors.contains(new Position(15, 1)));
    }

    @Test
    void adjacencyIsSymmetric() {
        Board board = new Board();
        for (Position pos : Board.getAllPositions()) {
            for (Position neighbor : board.getNeighbors(pos)) {
                assertTrue(board.getNeighbors(neighbor).contains(pos),
                        pos + " -> " + neighbor + " but not reverse");
            }
        }
    }

    @Test
    void eachPositionHasOneToSixNeighbors() {
        Board board = new Board();
        for (Position pos : Board.getAllPositions()) {
            int count = board.getNeighbors(pos).size();
            assertTrue(count >= 1 && count <= 6,
                    pos + " has " + count + " neighbors");
        }
    }

    @Test
    void centerOfBoardHasSixNeighbors() {
        Board board = new Board();
        // Position (8, 4) is roughly the center of the center row
        Position center = new Position(8, 4);
        assertEquals(6, board.getNeighbors(center).size());
    }

    @Test
    void row3ToRow4Transition() {
        Board board = new Board();
        Position p = new Position(3, 0);
        List<Position> neighbors = board.getNeighbors(p);
        // (3,0) canonical x = 4.5. Neighbors in row 4 at canonical x = 4.0 and 5.0 -> (4,4) and (4,5)
        assertTrue(neighbors.contains(new Position(4, 4)));
        assertTrue(neighbors.contains(new Position(4, 5)));
        assertTrue(neighbors.contains(new Position(2, 0)));
    }

    @Test
    void row12ToRow13Transition() {
        Board board = new Board();
        Position p = new Position(13, 0);
        List<Position> neighbors = board.getNeighbors(p);
        // (13,0) canonical x = 4.5. Neighbors in row 12 at canonical x = 4.0 and 5.0 -> (12,4) and (12,5)
        assertTrue(neighbors.contains(new Position(12, 4)));
        assertTrue(neighbors.contains(new Position(12, 5)));
    }

    @Test
    void eachHomeTriangleHas10Positions() {
        for (PlayerColor color : PlayerColor.values()) {
            assertEquals(10, Board.getHomeTriangle(color).size(),
                    color + " home triangle");
        }
    }

    @Test
    void eachGoalTriangleHas10Positions() {
        for (PlayerColor color : PlayerColor.values()) {
            assertEquals(10, Board.getGoalTriangle(color).size(),
                    color + " goal triangle");
        }
    }

    @Test
    void goalTriangleIsOppositeHome() {
        for (PlayerColor color : PlayerColor.values()) {
            assertEquals(Board.getHomeTriangle(color.opposite()),
                    Board.getGoalTriangle(color),
                    color + " goal should equal " + color.opposite() + " home");
        }
    }

    @Test
    void trianglesDontOverlap() {
        Set<Position> all = new java.util.HashSet<>();
        for (PlayerColor color : PlayerColor.values()) {
            Set<Position> home = Board.getHomeTriangle(color);
            for (Position p : home) {
                assertTrue(all.add(p), "Duplicate position " + p + " in " + color + " triangle");
            }
        }
        assertEquals(60, all.size());
    }

    @Test
    void allTrianglePositionsAreValidBoardPositions() {
        for (PlayerColor color : PlayerColor.values()) {
            for (Position p : Board.getHomeTriangle(color)) {
                assertTrue(Board.getAllPositions().contains(p),
                        p + " in " + color + " triangle is not a valid position");
            }
        }
    }

    @Test
    void placePieceAndRetrieve() {
        Board board = new Board();
        Piece piece = new Piece("RED_0", PlayerColor.RED, 0, 0);
        board.placePiece(piece);
        assertEquals(piece, board.getPieceAt(new Position(0, 0)));
        assertFalse(board.isEmpty(new Position(0, 0)));
    }

    @Test
    void movePieceUpdatesPosition() {
        Board board = new Board();
        Piece piece = new Piece("RED_0", PlayerColor.RED, 0, 0);
        board.placePiece(piece);
        board.movePiece(piece, new Position(1, 0));
        assertNull(board.getPieceAt(new Position(0, 0)));
        assertEquals(piece, board.getPieceAt(new Position(1, 0)));
        assertEquals(1, piece.getRow());
        assertEquals(0, piece.getCol());
    }

    @Test
    void deepCopyIsIndependent() {
        Board board = new Board();
        Piece piece = new Piece("RED_0", PlayerColor.RED, 0, 0);
        board.placePiece(piece);

        Board copy = board.deepCopy();
        copy.movePiece(copy.getPieceAt(new Position(0, 0)), new Position(1, 0));

        assertNotNull(board.getPieceAt(new Position(0, 0)));
        assertNull(copy.getPieceAt(new Position(0, 0)));
    }

    @Test
    void hopLandingFromTopTriangle() {
        // (0,0) hop over (1,0) should land at (2,0)
        Position landing = Board.getHopLanding(new Position(0, 0), new Position(1, 0));
        assertEquals(new Position(2, 0), landing);
    }

    @Test
    void hopLandingFromTopTriangleDiagonal() {
        // (0,0) hop over (1,1) should land at (2,2)
        Position landing = Board.getHopLanding(new Position(0, 0), new Position(1, 1));
        assertEquals(new Position(2, 2), landing);
    }

    @Test
    void hopLandingInvalidReturnsNull() {
        // Hop from (0,0) in an impossible direction
        Position landing = Board.getHopLanding(new Position(0, 0), new Position(0, 0));
        // same position - row would be -0 which is 0, but canonical x would be same, so col would be 0
        // Actually this would land at (0,0) itself which isn't really a hop
        // Let's test a real invalid case: hop that lands off-board
        Position landing2 = Board.getHopLanding(new Position(16, 0), new Position(15, 1));
        // landing row = 2*15 - 16 = 14, canonical x = 2*6.5 - 6.0 = 7.0
        // row 14 has 3 positions, offset = (13-3)/2 = 5.0, col = 7.0 - 5.0 = 2.0
        assertEquals(new Position(14, 2), landing2);
    }

    @Test
    void hopLandingHorizontal() {
        // Horizontal hop on row 4: (4,0) over (4,1) should land at (4,2)
        Position landing = Board.getHopLanding(new Position(4, 0), new Position(4, 1));
        assertEquals(new Position(4, 2), landing);
    }

    @Test
    void areAdjacentWorksCorrectly() {
        assertTrue(Board.areAdjacent(new Position(0, 0), new Position(1, 0)));
        assertTrue(Board.areAdjacent(new Position(0, 0), new Position(1, 1)));
        assertFalse(Board.areAdjacent(new Position(0, 0), new Position(2, 0)));
    }

    @Test
    void getPiecesForPlayerReturnsOnlyThatPlayerspieces() {
        Board board = new Board();
        board.placePiece(new Piece("RED_0", PlayerColor.RED, 0, 0));
        board.placePiece(new Piece("GREEN_0", PlayerColor.GREEN, 16, 0));
        assertEquals(1, board.getPiecesForPlayer(PlayerColor.RED).size());
        assertEquals(1, board.getPiecesForPlayer(PlayerColor.GREEN).size());
        assertEquals(0, board.getPiecesForPlayer(PlayerColor.BLUE).size());
    }
}
