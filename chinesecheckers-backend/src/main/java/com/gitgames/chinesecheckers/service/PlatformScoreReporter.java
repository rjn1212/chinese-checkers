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

@Service
public class PlatformScoreReporter {

    private static final Logger log = LoggerFactory.getLogger(PlatformScoreReporter.class);

    @Value("${platform.api.url:}")
    private String platformApiUrl;

    @Value("${platform.api.key:}")
    private String platformApiKey;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    public void reportGameResult(Game game, PlayerColor winner) {
        if (platformApiUrl == null || platformApiUrl.isBlank()) return;
        if (platformApiKey == null || platformApiKey.isBlank()) return;

        try {
            String body = """
                {
                    "gameSlug": "chinese-checkers",
                    "gameId": "%s",
                    "winner": "%s",
                    "moveCount": %d,
                    "playerCount": %d
                }
                """.formatted(game.getId(), winner.name(), game.getMoveCount(), game.getPlayerSlots().size());

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(platformApiUrl + "/api/scores"))
                    .header("Content-Type", "application/json")
                    .header("X-API-Key", platformApiKey)
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
