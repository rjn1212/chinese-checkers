package com.gitgames.chinesecheckers.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class Game {

    private final String id;
    private final Board board;
    private final List<PlayerSlot> playerSlots;
    private int currentTurnIndex;
    private GamePhase phase;
    private PlayerColor winner;
    private final List<PlayerColor> finishOrder;
    private final List<MoveRecord> moveHistory;
    private final Instant createdAt;
    private Instant lastActivityAt;
    private int moveCount;

    public Game(String id, Board board, List<PlayerSlot> playerSlots) {
        this.id = id;
        this.board = board;
        this.playerSlots = new ArrayList<>(playerSlots);
        this.currentTurnIndex = 0;
        this.phase = GamePhase.PLAYING;
        this.winner = null;
        this.finishOrder = new ArrayList<>();
        this.moveHistory = new ArrayList<>();
        this.createdAt = Instant.now();
        this.lastActivityAt = Instant.now();
        this.moveCount = 0;
    }

    public String getId() {
        return id;
    }

    public Board getBoard() {
        return board;
    }

    public List<PlayerSlot> getPlayerSlots() {
        return playerSlots;
    }

    public int getCurrentTurnIndex() {
        return currentTurnIndex;
    }

    public PlayerSlot getCurrentPlayerSlot() {
        return playerSlots.get(currentTurnIndex);
    }

    public PlayerColor getCurrentTurn() {
        return getCurrentPlayerSlot().getColor();
    }

    public boolean isCurrentTurnAi() {
        return getCurrentPlayerSlot().isAi();
    }

    public boolean isCurrentTurnHuman() {
        return getCurrentPlayerSlot().isHuman();
    }

    public GamePhase getPhase() {
        return phase;
    }

    public void setPhase(GamePhase phase) {
        this.phase = phase;
    }

    public PlayerColor getWinner() {
        return winner;
    }

    public void setWinner(PlayerColor winner) {
        this.winner = winner;
    }

    public List<PlayerColor> getFinishOrder() {
        return finishOrder;
    }

    public List<MoveRecord> getMoveHistory() {
        return moveHistory;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getLastActivityAt() {
        return lastActivityAt;
    }

    public int getMoveCount() {
        return moveCount;
    }

    public void recordMove(PlayerColor player, Move move) {
        moveCount++;
        moveHistory.add(MoveRecord.from(player, move, moveCount));
        lastActivityAt = Instant.now();
    }

    public void advanceTurn() {
        int startIndex = currentTurnIndex;
        do {
            currentTurnIndex = (currentTurnIndex + 1) % playerSlots.size();
        } while (playerSlots.get(currentTurnIndex).isFinished() && currentTurnIndex != startIndex);
    }

    public void markPlayerFinished(PlayerColor color) {
        for (PlayerSlot slot : playerSlots) {
            if (slot.getColor() == color) {
                slot.setFinished(true);
                finishOrder.add(color);
                slot.setFinishOrder(finishOrder.size());
                if (winner == null) {
                    winner = color;
                }
                break;
            }
        }

        long unfinishedCount = playerSlots.stream().filter(s -> !s.isFinished()).count();
        if (unfinishedCount <= 1) {
            phase = GamePhase.GAME_OVER;
            for (PlayerSlot slot : playerSlots) {
                if (!slot.isFinished()) {
                    slot.setFinished(true);
                    finishOrder.add(slot.getColor());
                    slot.setFinishOrder(finishOrder.size());
                }
            }
        }
    }

    public PlayerSlot getSlotForColor(PlayerColor color) {
        return playerSlots.stream()
                .filter(s -> s.getColor() == color)
                .findFirst()
                .orElse(null);
    }
}
