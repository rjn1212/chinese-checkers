package com.gitgames.chinesecheckers.model;

import java.util.*;

public class Board {

    private static final int[] ROW_COUNTS = {1, 2, 3, 4, 13, 12, 11, 10, 9, 10, 11, 12, 13, 4, 3, 2, 1};
    private static final Map<Position, List<Position>> ADJACENCY = buildAdjacencyGraph();
    private static final Map<PlayerColor, Set<Position>> HOME_TRIANGLES = buildHomeTriangles();
    private static final Map<PlayerColor, Set<Position>> GOAL_TRIANGLES = buildGoalTriangles();
    private static final Set<Position> ALL_POSITIONS = buildAllPositions();

    private final Map<Position, Piece> pieces;

    public Board() {
        this.pieces = new HashMap<>();
    }

    private Board(Map<Position, Piece> pieces) {
        this.pieces = new HashMap<>(pieces);
    }

    public static int getPositionCountForRow(int row) {
        if (row < 0 || row > 16) return 0;
        return ROW_COUNTS[row];
    }

    public static double canonicalX(int row, int col) {
        int width = getPositionCountForRow(row);
        return (13.0 - width) / 2.0 + col;
    }

    public static boolean areAdjacent(Position a, Position b) {
        List<Position> neighbors = ADJACENCY.get(a);
        return neighbors != null && neighbors.contains(b);
    }

    public static Set<Position> getAllPositions() {
        return ALL_POSITIONS;
    }

    public static Set<Position> getHomeTriangle(PlayerColor color) {
        return HOME_TRIANGLES.get(color);
    }

    public static Set<Position> getGoalTriangle(PlayerColor color) {
        return GOAL_TRIANGLES.get(color);
    }

    public List<Position> getNeighbors(Position pos) {
        return ADJACENCY.getOrDefault(pos, List.of());
    }

    public Piece getPieceAt(Position pos) {
        return pieces.get(pos);
    }

    public boolean isEmpty(Position pos) {
        return !pieces.containsKey(pos);
    }

    public boolean isValidPosition(Position pos) {
        return ALL_POSITIONS.contains(pos);
    }

    public void placePiece(Piece piece) {
        Position pos = piece.getPosition();
        pieces.put(pos, piece);
    }

    public void movePiece(Piece piece, Position to) {
        Position from = piece.getPosition();
        pieces.remove(from);
        piece.setPosition(to.row(), to.col());
        pieces.put(to, piece);
    }

    public List<Piece> getAllPieces() {
        return new ArrayList<>(pieces.values());
    }

    public List<Piece> getPiecesForPlayer(PlayerColor color) {
        return pieces.values().stream()
                .filter(p -> p.getOwner() == color)
                .toList();
    }

    public Board deepCopy() {
        Map<Position, Piece> copiedPieces = new HashMap<>();
        for (var entry : pieces.entrySet()) {
            copiedPieces.put(entry.getKey(), entry.getValue().clone());
        }
        return new Board(copiedPieces);
    }

    /**
     * Compute the landing position when hopping from {@code from} over {@code over}.
     * Returns null if the landing is not a valid board position.
     */
    public static Position getHopLanding(Position from, Position over) {
        int landingRow = 2 * over.row() - from.row();
        if (landingRow < 0 || landingRow > 16) return null;

        double landingCanonicalX = 2 * canonicalX(over.row(), over.col()) - canonicalX(from.row(), from.col());
        int landingWidth = getPositionCountForRow(landingRow);
        if (landingWidth == 0) return null;

        double offset = (13.0 - landingWidth) / 2.0;
        double colDouble = landingCanonicalX - offset;
        int landingCol = (int) Math.round(colDouble);

        if (Math.abs(colDouble - landingCol) > 0.01) return null;
        if (landingCol < 0 || landingCol >= landingWidth) return null;

        Position landing = new Position(landingRow, landingCol);
        return ALL_POSITIONS.contains(landing) ? landing : null;
    }

    private static Set<Position> buildAllPositions() {
        Set<Position> positions = new LinkedHashSet<>();
        for (int row = 0; row <= 16; row++) {
            int count = getPositionCountForRow(row);
            for (int col = 0; col < count; col++) {
                positions.add(new Position(row, col));
            }
        }
        return Collections.unmodifiableSet(positions);
    }

    private static Map<Position, List<Position>> buildAdjacencyGraph() {
        Set<Position> allPos = buildAllPositions();
        Map<Position, List<Position>> adj = new HashMap<>();

        for (Position pos : allPos) {
            List<Position> neighbors = new ArrayList<>();
            double cx = canonicalX(pos.row(), pos.col());

            // Same-row neighbors
            Position left = new Position(pos.row(), pos.col() - 1);
            if (allPos.contains(left)) neighbors.add(left);
            Position right = new Position(pos.row(), pos.col() + 1);
            if (allPos.contains(right)) neighbors.add(right);

            // Adjacent rows
            for (int dr : new int[]{-1, 1}) {
                int adjRow = pos.row() + dr;
                int adjWidth = getPositionCountForRow(adjRow);
                if (adjWidth == 0) continue;

                double adjOffset = (13.0 - adjWidth) / 2.0;
                for (double targetCx : new double[]{cx - 0.5, cx + 0.5}) {
                    double colDouble = targetCx - adjOffset;
                    int adjCol = (int) Math.round(colDouble);
                    if (Math.abs(colDouble - adjCol) > 0.01) continue;
                    if (adjCol < 0 || adjCol >= adjWidth) continue;
                    Position neighbor = new Position(adjRow, adjCol);
                    if (allPos.contains(neighbor)) {
                        neighbors.add(neighbor);
                    }
                }
            }

            adj.put(pos, Collections.unmodifiableList(neighbors));
        }

        return Collections.unmodifiableMap(adj);
    }

    private static Map<PlayerColor, Set<Position>> buildHomeTriangles() {
        Map<PlayerColor, Set<Position>> triangles = new EnumMap<>(PlayerColor.class);

        // RED: top triangle (rows 0-3)
        triangles.put(PlayerColor.RED, buildTrianglePositions(new int[][]{{0, 0}, {1, 0}, {1, 1}, {2, 0}, {2, 1}, {2, 2}, {3, 0}, {3, 1}, {3, 2}, {3, 3}}));

        // GREEN: bottom triangle (rows 13-16)
        triangles.put(PlayerColor.GREEN, buildTrianglePositions(new int[][]{{13, 0}, {13, 1}, {13, 2}, {13, 3}, {14, 0}, {14, 1}, {14, 2}, {15, 0}, {15, 1}, {16, 0}}));

        // ORANGE: upper-right triangle
        triangles.put(PlayerColor.ORANGE, buildTrianglePositions(new int[][]{{4, 9}, {4, 10}, {4, 11}, {4, 12}, {5, 9}, {5, 10}, {5, 11}, {6, 9}, {6, 10}, {7, 9}}));

        // YELLOW: lower-right triangle
        triangles.put(PlayerColor.YELLOW, buildTrianglePositions(new int[][]{{9, 9}, {10, 9}, {10, 10}, {11, 9}, {11, 10}, {11, 11}, {12, 9}, {12, 10}, {12, 11}, {12, 12}}));

        // BLUE: lower-left triangle
        triangles.put(PlayerColor.BLUE, buildTrianglePositions(new int[][]{{9, 0}, {10, 0}, {10, 1}, {11, 0}, {11, 1}, {11, 2}, {12, 0}, {12, 1}, {12, 2}, {12, 3}}));

        // PURPLE: upper-left triangle
        triangles.put(PlayerColor.PURPLE, buildTrianglePositions(new int[][]{{4, 0}, {4, 1}, {4, 2}, {4, 3}, {5, 0}, {5, 1}, {5, 2}, {6, 0}, {6, 1}, {7, 0}}));

        return Collections.unmodifiableMap(triangles);
    }

    private static Map<PlayerColor, Set<Position>> buildGoalTriangles() {
        Map<PlayerColor, Set<Position>> goals = new EnumMap<>(PlayerColor.class);
        Map<PlayerColor, Set<Position>> homes = buildHomeTriangles();
        for (PlayerColor color : PlayerColor.values()) {
            goals.put(color, homes.get(color.opposite()));
        }
        return Collections.unmodifiableMap(goals);
    }

    private static Set<Position> buildTrianglePositions(int[][] coords) {
        Set<Position> positions = new LinkedHashSet<>();
        for (int[] coord : coords) {
            positions.add(new Position(coord[0], coord[1]));
        }
        return Collections.unmodifiableSet(positions);
    }
}
