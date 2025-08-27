package com.sergiodev.appplaylistmanager.web.util;

import com.sergiodev.appplaylistmanager.domain.exception.model.ApiExceptionResponse;
import org.springframework.http.HttpStatus;

public interface ApiResponseBuilder {

    default <T> ApiResponse<T> buildSuccessResponse(T data, String traceId, HttpStatus code) {
        return ApiResponse.<T>builder()
            .traceId(traceId)
            .httpCode(code.value())
            .success(true)
            .data(data)
            .build();
    }

    default <T> ApiResponse<T> buildErrorResponse(ApiExceptionResponse error, String traceId, HttpStatus code) {
        return ApiResponse.<T>builder()
            .traceId(traceId)
            .httpCode(code.value())
            .success(false)
            .error(error)
            .build();
    }
}
