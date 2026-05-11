package com.gitgames.chinesecheckers.ai;

import com.gitgames.chinesecheckers.engine.MoveValidator;
import com.gitgames.chinesecheckers.model.*;

import java.util.*;

public class HardAiStrategy implements AiStrategy {

    private final MoveValidator moveValidator = new MoveValidator();
    private final MediumAiStrategy heuristic = new MediumAiStrategy();
    private final Random random = new Random();
    private long timeLimit;
    private long startTime;

    @Override
    public Move selectMove(Board board, PlayerColor player, List<PlayerSlot> allPlayers) {
        List<Move> legalMoves = moveValidator.getLegalMoves(board, player);
        if (legalMoves.isEmpty()) return null;
        if (legalMoves.size() == 1) return legalMoves.get(0);

        legalMoves.sort((a, b) -> Double.compare(
                heuristic.scoreMove(board, player, b),
                heuristic.scoreMove(board, player, a)));

        int activePlayers = (int) allPlayers.stream().filter(s -> !s.isFinished()).count();
        int depth = switch (activePlayers) {
            case 2 -> 4;
            case 3, 4 -> 3;
            default -> 2;
        };

        timeLimit = 2000;
        startTime = System.currentTimeMillis();

        double bestScore = Double.NEGATIVE_INFINITY;
        List<Move> bestMoves = new ArrayList<>();

        List<PlayerColor> turnOrder = buildTurnOrder(player, allPlayers);

        for (Move move : legalMoves) {
            if (isTimeUp()) break;

            Board copy = board.deepCopy();
            Piece piece = copy.getPieceAt(move.getFrom());
            copy.movePiece(piece, move.getTo());

            double score = minimax(copy, depth - 1, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY,
                    false, player, turnOrder, 1);

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

    private double minimax(Board board, int depth, double alpha, double beta,
                           boolean maximizing, PlayerColor aiPlayer,
                           List<PlayerColor> turnOrder, int turnIndex) {
        if (depth == 0 || isTimeUp()) {
            return evaluate(board, aiPlayer);
        }

        PlayerColor currentPlayer = turnOrder.get(turnIndex % turnOrder.size());
        int nextTurnIndex = turnIndex + 1;
        boolean isMaximizing = currentPlayer == aiPlayer;

        List<Move> moves = moveValidator.getLegalMoves(board, currentPlayer);
        if (moves.isEmpty()) {
            return minimax(board, depth - 1, alpha, beta, !isMaximizing, aiPlayer, turnOrder, nextTurnIndex);
        }

        if (moves.size() > 15) {
            moves.sort((a, b) -> Double.compare(
                    heuristic.scoreMove(board, currentPlayer, b),
                    heuristic.scoreMove(board, currentPlayer, a)));
            moves = moves.subList(0, Math.min(15, moves.size()));
        }

        if (isMaximizing) {
            double maxEval = Double.NEGATIVE_INFINITY;
            for (Move move : moves) {
                if (isTimeUp()) break;
                Board copy = board.deepCopy();
                copy.movePiece(copy.getPieceAt(move.getFrom()), move.getTo());
                double eval = minimax(copy, depth - 1, alpha, beta, false, aiPlayer, turnOrder, nextTurnIndex);
                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);
                if (beta <= alpha) break;
            }
            return maxEval;
        } else {
            double minEval = Double.POSITIVE_INFINITY;
            for (Move move : moves) {
                if (isTimeUp()) break;
                Board copy = board.deepCopy();
                copy.movePiece(copy.getPieceAt(move.getFrom()), move.getTo());
                double eval = minimax(copy, depth - 1, alpha, beta, true, aiPlayer, turnOrder, nextTurnIndex);
                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);
                if (beta <= alpha) break;
            }
            return minEval;
        }
    }

    private double evaluate(Board board, PlayerColor player) {
        double myDist = MediumAiStrategy.totalDistanceToGoal(board, player);

        long myInGoal = board.getPiecesForPlayer(player).stream()
                .filter(p -> Board.getGoalTriangle(player).contains(p.getPosition()))
                .count();

        return -myDist + myInGoal * 50;
    }

    private List<PlayerColor> buildTurnOrder(PlayerColor startPlayer, List<PlayerSlot> allPlayers) {
        List<PlayerColor> order = new ArrayList<>();
        for (PlayerSlot slot : allPlayers) {
            if (!slot.isFinished()) {
                order.add(slot.getColor());
            }
        }
        while (!order.isEmpty() && order.get(0) != startPlayer) {
            order.add(order.remove(0));
        }
        return order;
    }

    private boolean isTimeUp() {
        return System.currentTimeMillis() - startTime > timeLimit;
    }
}
