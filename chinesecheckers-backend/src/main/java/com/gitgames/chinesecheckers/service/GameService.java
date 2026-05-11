package com.gitgames.chinesecheckers.service;

import com.gitgames.chinesecheckers.ai.*;
import com.gitgames.chinesecheckers.engine.*;
import com.gitgames.chinesecheckers.model.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class GameService {

    private final GameRepository repository;
    private final PlatformScoreReporter scoreReporter;
    private final GameEngine engine = new GameEngine();

    public GameService(GameRepository repository, PlatformScoreReporter scoreReporter) {
        this.repository = repository;
        this.scoreReporter = scoreReporter;
    }

    public Game createGame(List<PlayerSlot> slots) {
        if (!BoardInitializer.isValidPlayerCount(slots.size())) {
            throw new InvalidMoveException("Player count must be 2, 3, 4, or 6");
        }

        long humanCount = slots.stream().filter(PlayerSlot::isHuman).count();
        if (humanCount < 1) {
            throw new InvalidMoveException("At least one human player is required");
        }

        Board board = BoardInitializer.createBoard(slots);
        String id = UUID.randomUUID().toString().substring(0, 8);
        Game game = new Game(id, board, slots);
        repository.save(game);
        return game;
    }

    public Game getGame(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new GameNotFoundException(id));
    }

    public List<Move> getLegalMoves(String gameId, int row, int col) {
        Game game = getGame(gameId);
        if (game.getPhase() != GamePhase.PLAYING) {
            return List.of();
        }

        Piece piece = game.getBoard().getPieceAt(new Position(row, col));
        if (piece == null) return List.of();
        if (piece.getOwner() != game.getCurrentTurn()) return List.of();

        return engine.getMovesForPiece(game.getBoard(), piece);
    }

    public MoveResult makeMove(String gameId, Position from, Position to) {
        Game game = getGame(gameId);

        if (game.getPhase() != GamePhase.PLAYING) {
            throw new InvalidMoveException("Game is already over");
        }

        if (!game.isCurrentTurnHuman()) {
            throw new InvalidMoveException("It is not a human player's turn");
        }

        Move humanMove = engine.validateAndFindMove(game.getBoard(), game.getCurrentTurn(), from, to);
        if (humanMove == null) {
            throw new InvalidMoveException("No legal move from " + from + " to " + to);
        }

        PlayerColor humanColor = game.getCurrentTurn();
        engine.applyMove(game.getBoard(), humanMove);
        game.recordMove(humanColor, humanMove);
        engine.checkAndHandleWin(game, humanColor);

        List<AiMoveRecord> aiMoves = new ArrayList<>();
        if (game.getPhase() == GamePhase.PLAYING) {
            game.advanceTurn();
            aiMoves = playAiTurns(game);
        }

        if (game.getPhase() == GamePhase.GAME_OVER && game.getWinner() != null) {
            reportScores(game);
        }

        return new MoveResult(humanColor, humanMove, aiMoves);
    }

    public List<AiMoveRecord> playInitialAiTurns(Game game) {
        return playAiTurns(game);
    }

    private List<AiMoveRecord> playAiTurns(Game game) {
        List<AiMoveRecord> aiMoves = new ArrayList<>();

        while (game.getPhase() == GamePhase.PLAYING && game.isCurrentTurnAi()) {
            PlayerSlot slot = game.getCurrentPlayerSlot();
            AiStrategy strategy = createAiStrategy(slot.getDifficulty());
            Move move = strategy.selectMove(game.getBoard(), slot.getColor(), game.getPlayerSlots());

            if (move == null) {
                game.advanceTurn();
                continue;
            }

            engine.applyMove(game.getBoard(), move);
            game.recordMove(slot.getColor(), move);
            aiMoves.add(new AiMoveRecord(slot.getColor(), move));
            engine.checkAndHandleWin(game, slot.getColor());

            if (game.getPhase() == GamePhase.PLAYING) {
                game.advanceTurn();
            }
        }

        return aiMoves;
    }

    public void abandonGame(String id) {
        getGame(id);
        repository.deleteById(id);
    }

    private void reportScores(Game game) {
        PlayerColor winner = game.getWinner();
        for (PlayerSlot slot : game.getPlayerSlots()) {
            if (slot.isHuman() && slot.getPlatformUserId() != null) {
                int score = slot.getColor() == winner ? 1 : 0;
                scoreReporter.reportGameResult(game, winner, slot.getPlatformUserId(), score);
            }
        }
    }

    private AiStrategy createAiStrategy(AiDifficulty difficulty) {
        return switch (difficulty) {
            case EASY -> new EasyAiStrategy();
            case MEDIUM -> new MediumAiStrategy();
            case HARD -> new HardAiStrategy();
        };
    }

    public record MoveResult(PlayerColor humanPlayer, Move humanMove, List<AiMoveRecord> aiMoves) {}

    public record AiMoveRecord(PlayerColor player, Move move) {}
}
