package com.gitgames.chinesecheckers.controller.dto;

import com.gitgames.chinesecheckers.model.Board;

import java.util.List;

public record BoardDto(List<PieceDto> pieces) {
    public static BoardDto from(Board board) {
        return new BoardDto(board.getAllPieces().stream().map(PieceDto::from).toList());
    }
}
