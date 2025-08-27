package com.sergiodev.appplaylistmanager.web.util;

import com.sergiodev.appplaylistmanager.domain.exception.model.ApiExceptionResponse;
import org.springframework.http.HttpStatus;

public interface ApiResponseBuilder {

    default <T> StandardizeApiResponse<T> buildSuccessResponse(T data, String traceId, HttpStatus code) {
        return StandardizeApiResponse.<T>builder()
            .traceId(traceId)
            .httpCode(code.value())
            .success(true)
            .data(data)
            .build();
    }

    default <T> StandardizeApiResponse<T> buildErrorResponse(ApiExceptionResponse error, String traceId, HttpStatus code) {
        return StandardizeApiResponse.<T>builder()
            .traceId(traceId)
            .httpCode(code.value())
            .success(false)
            .error(error)
            .build();
    }
}
