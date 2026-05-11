package com.gitgames.chinesecheckers.platform;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

@RestController
public class PlatformController {

    private final Instant startTime = Instant.now();

    @GetMapping("/platform/health")
    public Map<String, Object> health() {
        return Map.of(
                "status", "ok",
                "game", "chinese-checkers",
                "version", "1.0.0",
                "uptime", Duration.between(startTime, Instant.now()).getSeconds());
    }
}
