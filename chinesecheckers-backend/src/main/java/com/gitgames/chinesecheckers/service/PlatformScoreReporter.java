package com.gitgames.chinesecheckers.service;

import com.gitgames.chinesecheckers.model.Game;
import com.gitgames.chinesecheckers.model.PlayerColor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;

@Service
public class PlatformScoreReporter {

    private static final Logger log = LoggerFactory.getLogger(PlatformScoreReporter.class);

    @Value("${platform.api.url:}")
    private String platformApiUrl;

    @Value("${platform.api.key:}")
    private String platformApiKey;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    public void reportGameResult(Game game, PlayerColor winner, String userId, int score) {
        if (platformApiUrl == null || platformApiUrl.isBlank()) return;
        if (platformApiKey == null || platformApiKey.isBlank()) return;
        if (userId == null || userId.isBlank()) return;

        try {
            String result = score > 0 ? "WIN" : "LOSS";
            String body = """
                {
                    "gameSlug": "chinese-checkers",
                    "userId": "%s",
                    "score": %d,
                    "metadata": {
                        "result": "%s",
                        "gameId": "%s",
                        "winner": "%s",
                        "moveCount": %d,
                        "playerCount": %d
                    },
                    "completedAt": "%s"
                }
                """.formatted(
                    userId, score, result,
                    game.getId(), winner.name(),
                    game.getMoveCount(), game.getPlayerSlots().size(),
                    Instant.now().toString());

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(platformApiUrl + "/api/scores"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + platformApiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            httpClient.sendAsync(request, HttpResponse.BodyHandlers.discarding())
                    .thenAccept(resp -> {
                        if (resp.statusCode() >= 400) {
                            log.warn("Score report failed: HTTP {}", resp.statusCode());
                        }
                    })
                    .exceptionally(ex -> {
                        log.warn("Score report failed: {}", ex.getMessage());
                        return null;
                    });
        } catch (Exception e) {
            log.warn("Score report failed: {}", e.getMessage());
        }
    }
}
