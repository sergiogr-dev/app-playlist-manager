package com.sergiodev.appplaylistmanager.web.exception;

import com.sergiodev.appplaylistmanager.domain.exception.ApplicationException;
import com.sergiodev.appplaylistmanager.domain.exception.model.ExceptionInfo;
import com.sergiodev.appplaylistmanager.domain.exception.type.CommonException;
import com.sergiodev.appplaylistmanager.web.util.ApiResponse;
import com.sergiodev.appplaylistmanager.web.util.ApiResponseBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebExchange;

import java.util.Optional;

import static java.util.Objects.isNull;

@RestControllerAdvice
@Slf4j
public class ApiExceptionHandler implements ApiResponseBuilder {

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ApiResponse<?>> handleBusinessRuleException(ApplicationException ex, ServerWebExchange exchange) {
        String traceId = exchange.getResponse().getHeaders().getFirst("X-Trace-Id");
        if (traceId == null) {
            traceId = "unknown";
        }
        String[] errorDetail = { ex.getMessage() };
        return ResponseEntity
            .status(ex.getHttpStatus())
            .body(buildErrorResponse(ex.getErrorType().body(errorDetail), traceId, ex.getHttpStatus()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex, ServerWebExchange exchange) {
        String traceId = exchange.getResponse().getHeaders().getFirst("X-Trace-Id");
        if (traceId == null) {
            traceId = "unknown";
        }
        log.error("Error validating request {} - traceId {}", getExceptionInfo(ex), traceId);
        String[] errorDetail = { ex.getMessage() };
        CommonException.Type bseType = CommonException.Type.VALIDATION_ERROR;
        return  ResponseEntity
            .status(bseType.getHttpStatus())
            .body(buildErrorResponse(bseType.body(errorDetail), traceId, bseType.getHttpStatus()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleUnknownHostException(Exception ex, ServerWebExchange exchange) {
        String traceId = exchange.getResponse().getHeaders().getFirst("X-Trace-Id");
        if (traceId == null) {
            traceId = "unknown";
        }
        log.error("Uncaught exception {} - traceId: {}", getExceptionInfo(ex), traceId);
        String[] errorDetail = { ex.getMessage() };
        CommonException.Type bseType = CommonException.Type.GENERIC_ERROR;
        return  ResponseEntity
            .status(bseType.getHttpStatus())
            .body(buildErrorResponse(bseType.body(errorDetail), traceId, bseType.getHttpStatus()));
    }

    public ExceptionInfo getExceptionInfo(Exception exception) {
        StackTraceElement origin = exception.getStackTrace()[0];

        ExceptionInfo exceptionInfo = ExceptionInfo.builder()
            .exceptionMessage(exception.getMessage())
            .causeMessage(Optional.ofNullable(exception.getCause()).map(Throwable::getMessage).orElse(""))
            .exceptionType(exception.getClass().getName())
            .build();

        if (!isNull(origin)) {
            exceptionInfo = exceptionInfo.toBuilder()
                .originClassName(origin.getClassName())
                .originMethodName(origin.getMethodName())
                .build();
        }
        return exceptionInfo;
    }
}
