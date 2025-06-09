package com.acme.dto.error;

import java.time.LocalDateTime;

public record RateLimitErrorResponse(
    String error,
    int retryAfter,
    LocalDateTime timestamp
) {
    public static RateLimitErrorResponse of(String error, int retryAfter) {
        return new RateLimitErrorResponse(error, retryAfter, LocalDateTime.now());
    }
} 