package com.gitgames.chinesecheckers.controller;

import com.gitgames.chinesecheckers.controller.dto.*;
import com.gitgames.chinesecheckers.model.*;
import com.gitgames.chinesecheckers.service.GameService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/games")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping
    public ResponseEntity<GameStateResponse> createGame(
            @RequestBody CreateGameRequest request,
            HttpServletRequest httpRequest) {
        String platformUserId = (String) httpRequest.getAttribute("platformUserId");

        List<PlayerSlot> slots = request.playerSlots().stream()
                .map(s -> new PlayerSlot(s.color(), s.type(), s.difficulty()))
                .toList();

        if (platformUserId != null) {
            slots.stream()
                    .filter(PlayerSlot::isHuman)
                    .findFirst()
                    .ifPresent(slot -> slot.setPlatformUserId(platformUserId));
        }

        Game game = gameService.createGame(slots);

        List<MoveDto> initialAiMoves = List.of();
        if (game.getPhase() == GamePhase.PLAYING && game.isCurrentTurnAi()) {
            List<GameService.AiMoveRecord> aiRecords = gameService.playInitialAiTurns(game);
            initialAiMoves = aiRecords.stream()
                    .map(r -> MoveDto.from(r.player(), r.move()))
                    .toList();
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(GameStateResponse.from(game, initialAiMoves));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GameStateResponse> getGame(@PathVariable String id) {
        Game game = gameService.getGame(id);
        return ResponseEntity.ok(GameStateResponse.from(game));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> abandonGame(@PathVariable String id) {
        gameService.abandonGame(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/moves")
    public ResponseEntity<LegalMovesResponse> getLegalMoves(
            @PathVariable String id,
            @RequestParam int row,
            @RequestParam int col) {
        List<Move> moves = gameService.getLegalMoves(id, row, col);
        List<MoveDto> moveDtos = moves.stream()
                .map(m -> MoveDto.from(null, m))
                .toList();
        return ResponseEntity.ok(new LegalMovesResponse(moveDtos));
    }

    @PostMapping("/{id}/moves")
    public ResponseEntity<MoveResponse> makeMove(
            @PathVariable String id,
            @RequestBody MakeMoveRequest request) {
        GameService.MoveResult result = gameService.makeMove(
                id, request.from().toPosition(), request.to().toPosition());

        Game game = gameService.getGame(id);
        MoveDto humanMoveDto = MoveDto.from(result.humanPlayer(), result.humanMove());
        List<MoveDto> aiMoveDtos = result.aiMoves().stream()
                .map(r -> MoveDto.from(r.player(), r.move()))
                .toList();

        return ResponseEntity.ok(MoveResponse.from(game, humanMoveDto, aiMoveDtos));
    }
}
