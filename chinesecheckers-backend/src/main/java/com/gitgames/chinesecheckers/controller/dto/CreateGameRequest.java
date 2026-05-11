package com.gitgames.chinesecheckers.controller.dto;

import com.gitgames.chinesecheckers.model.AiDifficulty;
import com.gitgames.chinesecheckers.model.PlayerColor;
import com.gitgames.chinesecheckers.model.PlayerType;

import java.util.List;

public record CreateGameRequest(List<SlotConfig> playerSlots) {
    public record SlotConfig(PlayerColor color, PlayerType type, AiDifficulty difficulty) {}
}
