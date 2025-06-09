package com.acme.dto.error;

import java.time.LocalDateTime;
import java.util.Map;

public record ValidationErrorResponse(
        String message,
        Map<String, String> fieldErrors,
        LocalDateTime timestamp
) {} 