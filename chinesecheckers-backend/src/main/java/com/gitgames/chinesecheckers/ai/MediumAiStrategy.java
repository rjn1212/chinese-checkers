package com.gitgames.chinesecheckers.ai;

import com.gitgames.chinesecheckers.engine.MoveValidator;
import com.gitgames.chinesecheckers.model.*;

import java.util.*;

public class MediumAiStrategy implements AiStrategy {

    private final MoveValidator moveValidator = new MoveValidator();
    private final Random random = new Random();

    @Override
    public Move selectMove(Board board, PlayerColor player, List<PlayerSlot> allPlayers) {
        List<Move> legalMoves = moveValidator.getLegalMoves(board, player);
        if (legalMoves.isEmpty()) return null;

        double bestScore = Double.NEGATIVE_INFINITY;
        List<Move> bestMoves = new ArrayList<>();

        for (Move move : legalMoves) {
            double score = scoreMove(board, player, move);
            if (score > bestScore) {
                bestScore = score;
                bestMoves.clear();
                bestMoves.add(move);
            } else if (score == bestScore) {
                bestMoves.add(move);
            }
        }

        return bestMoves.get(random.nextInt(bestMoves.size()));
    }

    double scoreMove(Board board, PlayerColor player, Move move) {
        double score = 0;

        double distBefore = totalDistanceToGoal(board, player);
        Board copy = board.deepCopy();
        Piece piece = copy.getPieceAt(move.getFrom());
        copy.movePiece(piece, move.getTo());
        double distAfter = totalDistanceToGoal(copy, player);
        score += (distBefore - distAfter) * 10;

        Set<Position> goalTriangle = Board.getGoalTriangle(player);
        if (goalTriangle.contains(move.getTo())) {
            score += 50;
        }

        if (move.isHop()) {
            score += move.getPath().size() * 5;
        }

        Set<Position> homeTriangle = Board.getHomeTriangle(player);
        if (homeTriangle.contains(move.getFrom()) && !homeTriangle.contains(move.getTo())) {
            score += 15;
        }

        double cx = Board.canonicalX(move.getTo().row(), move.getTo().col());
        if (move.getTo().row() >= 4 && move.getTo().row() <= 12 && cx >= 4 && cx <= 9) {
            score += 3;
        }

        return score;
    }

    static double totalDistanceToGoal(Board board, PlayerColor player) {
        Set<Position> goal = Board.getGoalTriangle(player);
        double totalDist = 0;
        for (Piece p : board.getPiecesForPlayer(player)) {
            double minDist = Double.MAX_VALUE;
            double cx = Board.canonicalX(p.getRow(), p.getCol());
            for (Position g : goal) {
                double gx = Board.canonicalX(g.row(), g.col());
                double dist = Math.abs(p.getRow() - g.row()) + Math.abs(cx - gx);
                minDist = Math.min(minDist, dist);
            }
            totalDist += minDist;
        }
        return totalDist;
    }
}
