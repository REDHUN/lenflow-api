package com.redhun.lendflow_api.exception;

import java.time.LocalDateTime;

public record ErrorResponse(

        boolean success,

        String message,

        int status,

        LocalDateTime timestamp
) {
}
