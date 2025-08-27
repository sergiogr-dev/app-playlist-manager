package com.sergiodev.appplaylistmanager.web.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sergiodev.appplaylistmanager.domain.exception.model.ApiExceptionResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Respuesta estandarizada de la API para todos los endpoints.")
public record ApiResponse<T>(
    @Schema(description = "Indica si la solicitud fue exitosa.", example = "true")
    Boolean success,

    @Schema(description = "Indica el código http de respuesta de la solicitud")
    Integer httpCode,

    @Schema(description = "Identificador único de la traza para correlacionar eventos.", example = "6e5b4b1a-0b2a-4c2d-9e7f-8c3e4d5a6b7c")
    String traceId,

    @Schema(description = "Datos de la respuesta, si la solicitud fue exitosa.")
    T data,

    @Schema(description = "Detalles del error, si la solicitud falló.")
    ApiExceptionResponse error
) {
}
