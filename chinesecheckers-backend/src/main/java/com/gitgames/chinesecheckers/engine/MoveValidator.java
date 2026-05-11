package com.gitgames.chinesecheckers.engine;

import com.gitgames.chinesecheckers.model.*;

import java.util.*;

public class MoveValidator {

    public List<Move> getLegalMoves(Board board, PlayerColor player) {
        List<Move> moves = new ArrayList<>();
        for (Piece piece : board.getPiecesForPlayer(player)) {
            moves.addAll(getMovesForPiece(board, piece));
        }
        return moves;
    }

    public List<Move> getMovesForPiece(Board board, Piece piece) {
        List<Move> moves = new ArrayList<>();
        moves.addAll(getStepMoves(board, piece));
        moves.addAll(getHopMoves(board, piece));
        return moves;
    }

    public List<Move> getStepMoves(Board board, Piece piece) {
        List<Move> moves = new ArrayList<>();
        Position from = piece.getPosition();

        for (Position neighbor : board.getNeighbors(from)) {
            if (!board.isEmpty(neighbor)) continue;
            if (isGoalExitViolation(piece.getOwner(), from, neighbor)) continue;
            moves.add(Move.step(from, neighbor));
        }
        return moves;
    }

    public List<Move> getHopMoves(Board board, Piece piece) {
        List<Move> results = new ArrayList<>();
        Set<Position> visited = new HashSet<>();
        visited.add(piece.getPosition());
        dfsHops(board, piece.getOwner(), piece.getPosition(), piece.getPosition(), visited, new ArrayList<>(), results);
        return results;
    }

    private void dfsHops(Board board, PlayerColor owner, Position origin, Position current,
                         Set<Position> visited, List<Position> pathSoFar, List<Move> results) {
        for (Position neighbor : board.getNeighbors(current)) {
            if (board.isEmpty(neighbor)) continue;

            Position landing = Board.getHopLanding(current, neighbor);
            if (landing == null) continue;
            if (!board.isValidPosition(landing)) continue;
            if (!board.isEmpty(landing)) continue;
            if (visited.contains(landing)) continue;
            if (isGoalExitViolation(owner, origin, landing)) continue;

            List<Position> newPath = new ArrayList<>(pathSoFar);
            newPath.add(landing);
            visited.add(landing);

            results.add(new Move(origin, landing, new ArrayList<>(newPath)));

            dfsHops(board, owner, origin, landing, visited, newPath, results);

            visited.remove(landing);
        }
    }

    public Move findMoveByDestination(List<Move> legalMoves, Position from, Position to) {
        Move bestMatch = null;
        for (Move move : legalMoves) {
            if (move.getFrom().equals(from) && move.getTo().equals(to)) {
                if (bestMatch == null || move.getPath().size() > bestMatch.getPath().size()) {
                    bestMatch = move;
                }
            }
        }
        return bestMatch;
    }

    private boolean isGoalExitViolation(PlayerColor owner, Position from, Position to) {
        Set<Position> goalTriangle = Board.getGoalTriangle(owner);
        return goalTriangle.contains(from) && !goalTriangle.contains(to);
    }
}
