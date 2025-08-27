package com.sergiodev.appplaylistmanager.domain.exception.type;

import com.sergiodev.appplaylistmanager.domain.exception.ApplicationException;
import com.sergiodev.appplaylistmanager.domain.exception.model.ApiExceptionResponse;
import com.sergiodev.appplaylistmanager.domain.exception.util.IErrorType;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.function.Supplier;

public class CommonException extends ApplicationException {
    // El constructor simplemente pasa los datos a la clase padre.
    // Es 'privado' para forzar el uso del enum como fábrica.
    private CommonException(IErrorType<CommonException> errorType, String finalDetail) {
        super(errorType, finalDetail);
    }

    /**
     * Enum que agrupa errores genéricos de la aplicación y actúa como fábrica.
     */
    @Getter
    public enum Type implements IErrorType<CommonException> {

        RESOURCE_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "Resource Not Found",
            "GEN-001",
            "The resource you are looking for was not found. Key: %s"
        ),
        RESOURCE_ALREADY_EXISTS(
            HttpStatus.BAD_REQUEST,
            "Resource Already Exists",
            "GEN-008",
            "The resource you are trying to create already exists. Key: %s"
        ),
        BAD_REQUEST(
            HttpStatus.BAD_REQUEST,
            "Invalid Petition",
            "GEN-002",
            "The request contains invalid or incorrectly formatted data. Field: %s"
        ),
        UNAUTHORIZED(
            HttpStatus.UNAUTHORIZED,
            "Unauthorized",
            "GEN-003",
            "You do not have permission to access this resource. Token: %s"
        ),
        CANNOT_CONNECT_WITH_SERVICE(
            HttpStatus.SERVICE_UNAVAILABLE,
            "Unable to connect to the service",
            "GEN-005",
            "No connection could be established with the external service. Details: %s"
        ),
        ERROR_RECEIVED_FROM_SERVICE(
            HttpStatus.BAD_REQUEST,
            "Error in the external service",
            "GEN-006",
            "The external service has responded with an error. Details: %s"
        ),
        VALIDATION_ERROR(
            HttpStatus.BAD_REQUEST,
            "Error validating request data.",
            "GEN-007",
            "Conditions for the entry object were not met. Details: %s"
        ),
        GENERIC_ERROR(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Internal Server Error",
            "GEN-004",
            "An unexpected server error occurred. Details: %s"
        );

        private final HttpStatus httpStatus;
        private final String title;
        private final String code;
        private final String detail;

        Type(HttpStatus httpStatus, String title, String code, String detail) {
            this.httpStatus = httpStatus;
            this.title = title;
            this.code = code;
            this.detail = detail;
        }

        @Override
        public CommonException build() {
            // Llama al constructor privado con el mensaje de detalle por defecto.
            return new CommonException(this, this.detail);
        }

        @Override
        public CommonException build(Object... args) {
            // Llama al constructor privado con un mensaje formateado.
            return new CommonException(this, String.format(this.detail, args));
        }

        @Override
        public Supplier<CommonException> defer() {
            return () -> new CommonException(this, this.detail);
        }

        @Override
        public Supplier<CommonException> defer(Object... args) {
            return () -> new CommonException(this, String.format(this.detail, args));
        }

        @Override
        public String getType() {
            return CommonException.class.getSimpleName();
        }

        @Override
        public ApiExceptionResponse body(String[] errorDetails) {
            return new ApiExceptionResponse(
                this.getType(),
                this.getTitle(),
                this.getCode(),
                errorDetails
            );
        }
    }
}
