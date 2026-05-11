package com.gitgames.chinesecheckers.ai;

import com.gitgames.chinesecheckers.model.*;

import java.util.List;

public interface AiStrategy {
    Move selectMove(Board board, PlayerColor player, List<PlayerSlot> allPlayers);
}
