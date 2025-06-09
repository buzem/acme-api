package com.acme.dto.error;

import java.time.LocalDateTime;


public record ErrorResponse(
        String message,
        LocalDateTime timestamp
) {} 