package com.gitgames.chinesecheckers.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gitgames.chinesecheckers.controller.dto.CreateGameRequest;
import com.gitgames.chinesecheckers.controller.dto.MakeMoveRequest;
import com.gitgames.chinesecheckers.controller.dto.PositionDto;
import com.gitgames.chinesecheckers.model.AiDifficulty;
import com.gitgames.chinesecheckers.model.PlayerColor;
import com.gitgames.chinesecheckers.model.PlayerType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class GameControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private CreateGameRequest twoPlayerRequest() {
        return new CreateGameRequest(List.of(
                new CreateGameRequest.SlotConfig(PlayerColor.RED, PlayerType.HUMAN, null),
                new CreateGameRequest.SlotConfig(PlayerColor.GREEN, PlayerType.AI, AiDifficulty.EASY)));
    }

    private String createGame() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(twoPlayerRequest())))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("gameId").asText();
    }

    @Test
    void createGameReturns201() throws Exception {
        mockMvc.perform(post("/api/v1/games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(twoPlayerRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.gameId").isNotEmpty())
                .andExpect(jsonPath("$.phase").value("PLAYING"))
                .andExpect(jsonPath("$.currentTurn").value("RED"))
                .andExpect(jsonPath("$.playerSlots", hasSize(2)))
                .andExpect(jsonPath("$.board.pieces", hasSize(20)));
    }

    @Test
    void getGameReturnsState() throws Exception {
        String gameId = createGame();

        mockMvc.perform(get("/api/v1/games/" + gameId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId").value(gameId))
                .andExpect(jsonPath("$.phase").value("PLAYING"));
    }

    @Test
    void getGameNotFoundReturns404() throws Exception {
        mockMvc.perform(get("/api/v1/games/nonexistent"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("GAME_NOT_FOUND"));
    }

    @Test
    void getLegalMovesReturnsMovesForRedPiece() throws Exception {
        String gameId = createGame();

        // RED piece at (3,0) should have legal moves
        mockMvc.perform(get("/api/v1/games/" + gameId + "/moves")
                        .param("row", "3")
                        .param("col", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.moves").isArray())
                .andExpect(jsonPath("$.moves", hasSize(greaterThan(0))));
    }

    @Test
    void getLegalMovesReturnsEmptyForEmptyPosition() throws Exception {
        String gameId = createGame();

        mockMvc.perform(get("/api/v1/games/" + gameId + "/moves")
                        .param("row", "8")
                        .param("col", "4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.moves", hasSize(0)));
    }

    @Test
    void makeMoveReturnsHumanAndAiMoves() throws Exception {
        String gameId = createGame();

        // Get a legal move for RED
        MvcResult movesResult = mockMvc.perform(get("/api/v1/games/" + gameId + "/moves")
                        .param("row", "3")
                        .param("col", "0"))
                .andReturn();

        var movesJson = objectMapper.readTree(movesResult.getResponse().getContentAsString());
        var firstMove = movesJson.get("moves").get(0);
        int toRow = firstMove.get("to").get("row").asInt();
        int toCol = firstMove.get("to").get("col").asInt();

        MakeMoveRequest moveRequest = new MakeMoveRequest(
                new PositionDto(3, 0),
                new PositionDto(toRow, toCol));

        mockMvc.perform(post("/api/v1/games/" + gameId + "/moves")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(moveRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.humanMove").isNotEmpty())
                .andExpect(jsonPath("$.humanMove.player").value("RED"))
                .andExpect(jsonPath("$.aiMoves").isArray())
                .andExpect(jsonPath("$.board.pieces", hasSize(20)))
                .andExpect(jsonPath("$.phase").value("PLAYING"));
    }

    @Test
    void invalidMoveReturns400() throws Exception {
        String gameId = createGame();

        MakeMoveRequest moveRequest = new MakeMoveRequest(
                new PositionDto(3, 0),
                new PositionDto(10, 5));

        mockMvc.perform(post("/api/v1/games/" + gameId + "/moves")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(moveRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_MOVE"));
    }

    @Test
    void deleteGameReturns204() throws Exception {
        String gameId = createGame();

        mockMvc.perform(delete("/api/v1/games/" + gameId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/games/" + gameId))
                .andExpect(status().isNotFound());
    }

    @Test
    void healthEndpointReturnsOk() throws Exception {
        mockMvc.perform(get("/platform/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ok"))
                .andExpect(jsonPath("$.game").value("chinese-checkers"));
    }

    @Test
    void invalidPlayerCountReturns400() throws Exception {
        CreateGameRequest request = new CreateGameRequest(List.of(
                new CreateGameRequest.SlotConfig(PlayerColor.RED, PlayerType.HUMAN, null)));

        mockMvc.perform(post("/api/v1/games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void sixPlayerGameCreatesSuccessfully() throws Exception {
        CreateGameRequest request = new CreateGameRequest(List.of(
                new CreateGameRequest.SlotConfig(PlayerColor.RED, PlayerType.HUMAN, null),
                new CreateGameRequest.SlotConfig(PlayerColor.ORANGE, PlayerType.AI, AiDifficulty.EASY),
                new CreateGameRequest.SlotConfig(PlayerColor.YELLOW, PlayerType.AI, AiDifficulty.MEDIUM),
                new CreateGameRequest.SlotConfig(PlayerColor.GREEN, PlayerType.AI, AiDifficulty.HARD),
                new CreateGameRequest.SlotConfig(PlayerColor.BLUE, PlayerType.AI, AiDifficulty.EASY),
                new CreateGameRequest.SlotConfig(PlayerColor.PURPLE, PlayerType.AI, AiDifficulty.MEDIUM)));

        mockMvc.perform(post("/api/v1/games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.playerSlots", hasSize(6)))
                .andExpect(jsonPath("$.board.pieces", hasSize(60)));
    }
}
