package com.gitgames.chinesecheckers.controller.dto;

import com.gitgames.chinesecheckers.model.AiDifficulty;
import com.gitgames.chinesecheckers.model.PlayerColor;
import com.gitgames.chinesecheckers.model.PlayerSlot;
import com.gitgames.chinesecheckers.model.PlayerType;

public record PlayerSlotDto(
        PlayerColor color,
        PlayerType type,
        AiDifficulty difficulty,
        boolean finished,
        Integer finishOrder
) {
    public static PlayerSlotDto from(PlayerSlot slot) {
        return new PlayerSlotDto(slot.getColor(), slot.getType(), slot.getDifficulty(),
                slot.isFinished(), slot.getFinishOrder());
    }
}
