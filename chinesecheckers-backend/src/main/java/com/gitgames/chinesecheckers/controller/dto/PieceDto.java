package com.gitgames.chinesecheckers.controller.dto;

import com.gitgames.chinesecheckers.model.Piece;
import com.gitgames.chinesecheckers.model.PlayerColor;

public record PieceDto(String id, PlayerColor owner, int row, int col) {
    public static PieceDto from(Piece p) {
        return new PieceDto(p.getId(), p.getOwner(), p.getRow(), p.getCol());
    }
}
