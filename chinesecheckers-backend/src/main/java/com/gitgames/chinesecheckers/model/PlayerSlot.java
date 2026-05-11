package com.gitgames.chinesecheckers.model;

public class PlayerSlot {

    private final PlayerColor color;
    private final PlayerType type;
    private final AiDifficulty difficulty;
    private boolean finished;
    private Integer finishOrder;
    private String platformUserId;

    public PlayerSlot(PlayerColor color, PlayerType type, AiDifficulty difficulty) {
        this.color = color;
        this.type = type;
        this.difficulty = difficulty;
        this.finished = false;
        this.finishOrder = null;
    }

    public PlayerColor getColor() {
        return color;
    }

    public PlayerType getType() {
        return type;
    }

    public AiDifficulty getDifficulty() {
        return difficulty;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public Integer getFinishOrder() {
        return finishOrder;
    }

    public void setFinishOrder(Integer finishOrder) {
        this.finishOrder = finishOrder;
    }

    public String getPlatformUserId() {
        return platformUserId;
    }

    public void setPlatformUserId(String platformUserId) {
        this.platformUserId = platformUserId;
    }

    public boolean isHuman() {
        return type == PlayerType.HUMAN;
    }

    public boolean isAi() {
        return type == PlayerType.AI;
    }
}
