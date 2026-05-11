package com.gitgames.chinesecheckers.controller.dto;

import com.gitgames.chinesecheckers.model.Game;
import com.gitgames.chinesecheckers.model.GamePhase;
import com.gitgames.chinesecheckers.model.PlayerColor;

import java.util.List;

public record MoveResponse(
        MoveDto humanMove,
        List<MoveDto> aiMoves,
        BoardDto board,
        GamePhase phase,
        PlayerColor currentTurn,
        List<PlayerSlotDto> playerSlots,
        PlayerColor winner,
        List<PlayerColor> finishOrder,
        int moveCount
) {
    public static MoveResponse from(Game game, MoveDto humanMove, List<MoveDto> aiMoves) {
        return new MoveResponse(
                humanMove,
                aiMoves,
                BoardDto.from(game.getBoard()),
                game.getPhase(),
                game.getCurrentTurn(),
                game.getPlayerSlots().stream().map(PlayerSlotDto::from).toList(),
                game.getWinner(),
                game.getFinishOrder(),
                game.getMoveCount());
    }
}
