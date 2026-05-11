package com.gitgames.chinesecheckers.engine;

import com.gitgames.chinesecheckers.model.*;

import java.util.List;
import java.util.Set;

public class BoardInitializer {

    private static final List<PlayerColor> TWO_PLAYER_COLORS = List.of(PlayerColor.RED, PlayerColor.GREEN);
    private static final List<PlayerColor> THREE_PLAYER_COLORS = List.of(PlayerColor.RED, PlayerColor.YELLOW, PlayerColor.BLUE);
    private static final List<PlayerColor> FOUR_PLAYER_COLORS = List.of(PlayerColor.RED, PlayerColor.ORANGE, PlayerColor.GREEN, PlayerColor.BLUE);
    private static final List<PlayerColor> SIX_PLAYER_COLORS = List.of(PlayerColor.RED, PlayerColor.ORANGE, PlayerColor.YELLOW, PlayerColor.GREEN, PlayerColor.BLUE, PlayerColor.PURPLE);

    public static List<PlayerColor> getValidColorsForPlayerCount(int count) {
        return switch (count) {
            case 2 -> TWO_PLAYER_COLORS;
            case 3 -> THREE_PLAYER_COLORS;
            case 4 -> FOUR_PLAYER_COLORS;
            case 6 -> SIX_PLAYER_COLORS;
            default -> throw new IllegalArgumentException("Player count must be 2, 3, 4, or 6, got: " + count);
        };
    }

    public static boolean isValidPlayerCount(int count) {
        return count == 2 || count == 3 || count == 4 || count == 6;
    }

    public static Board createBoard(List<PlayerSlot> slots) {
        int count = slots.size();
        if (!isValidPlayerCount(count)) {
            throw new IllegalArgumentException("Player count must be 2, 3, 4, or 6, got: " + count);
        }

        List<PlayerColor> validColors = getValidColorsForPlayerCount(count);
        for (PlayerSlot slot : slots) {
            if (!validColors.contains(slot.getColor())) {
                throw new IllegalArgumentException(
                        "Color " + slot.getColor() + " is not valid for " + count + "-player game. Valid: " + validColors);
            }
        }

        Board board = new Board();
        for (PlayerSlot slot : slots) {
            placePiecesInHomeTriangle(board, slot.getColor());
        }
        return board;
    }

    private static void placePiecesInHomeTriangle(Board board, PlayerColor color) {
        Set<Position> homePositions = Board.getHomeTriangle(color);
        int index = 0;
        for (Position pos : homePositions) {
            String id = color.name() + "_" + index;
            board.placePiece(new Piece(id, color, pos.row(), pos.col()));
            index++;
        }
    }
}
