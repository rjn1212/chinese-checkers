package com.gitgames.chinesecheckers.controller.dto;

import com.gitgames.chinesecheckers.model.Game;
import com.gitgames.chinesecheckers.model.GamePhase;
import com.gitgames.chinesecheckers.model.PlayerColor;

import java.util.List;

public record GameStateResponse(
        String gameId,
        GamePhase phase,
        PlayerColor currentTurn,
        List<PlayerSlotDto> playerSlots,
        BoardDto board,
        PlayerColor winner,
        List<PlayerColor> finishOrder,
        int moveCount,
        List<MoveDto> aiMoves
) {
    public static GameStateResponse from(Game game) {
        return from(game, List.of());
    }

    public static GameStateResponse from(Game game, List<MoveDto> aiMoves) {
        return new GameStateResponse(
                game.getId(),
                game.getPhase(),
                game.getCurrentTurn(),
                game.getPlayerSlots().stream().map(PlayerSlotDto::from).toList(),
                BoardDto.from(game.getBoard()),
                game.getWinner(),
                game.getFinishOrder(),
                game.getMoveCount(),
                aiMoves);
    }
}
