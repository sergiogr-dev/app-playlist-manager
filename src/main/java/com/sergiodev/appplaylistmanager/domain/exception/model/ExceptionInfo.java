package com.sergiodev.appplaylistmanager.domain.exception.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder(toBuilder = true)
@Schema(description = "Detalles de una excepción capturada por el manejador global.")
public record ExceptionInfo(
    @Schema(description = "Nombre de la clase donde ocurrió la excepción.", example = "com.paymentchain.controller.TransactionController")
    String originClassName,

    @Schema(description = "Nombre del método donde se lanzó la excepción.", example = "createTransaction")
    String originMethodName,

    @Schema(description = "Mensaje principal de la excepción.", example = "El ID de transacción no puede ser nulo.")
    String exceptionMessage,

    @Schema(description = "Mensaje de la causa raíz de la excepción, si existe.", example = "El parámetro 'transactionId' es obligatorio.")
    String causeMessage,

    @Schema(description = "Tipo o nombre de la excepción.", example = "java.lang.IllegalArgumentException")
    String exceptionType
) {
}
