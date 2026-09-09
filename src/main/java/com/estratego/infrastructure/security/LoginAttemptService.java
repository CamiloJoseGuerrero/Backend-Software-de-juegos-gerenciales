package com.estratego.infrastructure.security;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final Duration BLOCK_DURATION = Duration.ofMinutes(15);

    private final Map<String, Attempt> attempts = new ConcurrentHashMap<>();

    public boolean isBlocked(String key) {
        Attempt attempt = attempts.get(key);
        if (attempt == null) {
            return false;
        }
        if (attempt.lastAttempt().plus(BLOCK_DURATION).isBefore(Instant.now())) {
            attempts.remove(key);
            return false;
        }
        return attempt.failures() >= MAX_ATTEMPTS;
    }

    public void recordFailure(String key) {
        attempts.compute(key, (ignored, current) -> {
            if (current == null || current.lastAttempt().plus(BLOCK_DURATION).isBefore(Instant.now())) {
                return new Attempt(1, Instant.now());
            }
            return new Attempt(current.failures() + 1, Instant.now());
        });
    }

    public void recordSuccess(String key) {
        attempts.remove(key);
    }

    private record Attempt(int failures, Instant lastAttempt) {
    }
}