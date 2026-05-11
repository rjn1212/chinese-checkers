package com.gitgames.chinesecheckers.ai;

import com.gitgames.chinesecheckers.engine.MoveValidator;
import com.gitgames.chinesecheckers.model.*;

import java.util.List;
import java.util.Random;
import java.util.Set;

public class EasyAiStrategy implements AiStrategy {

    private final MoveValidator moveValidator = new MoveValidator();
    private final Random random = new Random();

    @Override
    public Move selectMove(Board board, PlayerColor player, List<PlayerSlot> allPlayers) {
        List<Move> legalMoves = moveValidator.getLegalMoves(board, player);
        if (legalMoves.isEmpty()) return null;

        List<Move> advancingMoves = legalMoves.stream()
                .filter(m -> distanceToGoal(player, m.getTo()) < distanceToGoal(player, m.getFrom()))
                .toList();

        if (!advancingMoves.isEmpty() && random.nextDouble() < 0.7) {
            return advancingMoves.get(random.nextInt(advancingMoves.size()));
        }

        return legalMoves.get(random.nextInt(legalMoves.size()));
    }

    private double distanceToGoal(PlayerColor player, Position pos) {
        Set<Position> goal = Board.getGoalTriangle(player);
        double cx = Board.canonicalX(pos.row(), pos.col());
        double minDist = Double.MAX_VALUE;
        for (Position g : goal) {
            double gx = Board.canonicalX(g.row(), g.col());
            double dist = Math.abs(pos.row() - g.row()) + Math.abs(cx - gx);
            minDist = Math.min(minDist, dist);
        }
        return minDist;
    }
}
