package com.gitgames.chinesecheckers.controller.dto;

import com.gitgames.chinesecheckers.model.Move;
import com.gitgames.chinesecheckers.model.PlayerColor;

import java.util.List;

public record MoveDto(
        PlayerColor player,
        PositionDto from,
        PositionDto to,
        List<PositionDto> path
) {
    public static MoveDto from(PlayerColor player, Move move) {
        return new MoveDto(
                player,
                PositionDto.from(move.getFrom()),
                PositionDto.from(move.getTo()),
                move.getPath().stream().map(PositionDto::from).toList());
    }
}
