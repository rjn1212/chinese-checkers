package com.gitgames.chinesecheckers.engine;

import com.gitgames.chinesecheckers.model.*;

import java.util.List;

public class GameEngine {

    private final MoveValidator moveValidator = new MoveValidator();
    private final WinConditionChecker winChecker = new WinConditionChecker();

    public List<Move> getLegalMoves(Board board, PlayerColor player) {
        return moveValidator.getLegalMoves(board, player);
    }

    public List<Move> getMovesForPiece(Board board, Piece piece) {
        return moveValidator.getMovesForPiece(board, piece);
    }

    public Move validateAndFindMove(Board board, PlayerColor player, Position from, Position to) {
        Piece piece = board.getPieceAt(from);
        if (piece == null) return null;
        if (piece.getOwner() != player) return null;

        List<Move> legalMoves = moveValidator.getMovesForPiece(board, piece);
        return moveValidator.findMoveByDestination(legalMoves, from, to);
    }

    public void applyMove(Board board, Move move) {
        Piece piece = board.getPieceAt(move.getFrom());
        if (piece == null) {
            throw new IllegalStateException("No piece at " + move.getFrom());
        }
        board.movePiece(piece, move.getTo());
    }

    public boolean checkAndHandleWin(Game game, PlayerColor player) {
        if (winChecker.hasPlayerFinished(game.getBoard(), player)) {
            game.markPlayerFinished(player);
            return true;
        }
        return false;
    }

    public MoveValidator getMoveValidator() {
        return moveValidator;
    }

    public WinConditionChecker getWinChecker() {
        return winChecker;
    }
}
