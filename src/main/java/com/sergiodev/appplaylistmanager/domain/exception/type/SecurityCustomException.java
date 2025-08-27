package com.sergiodev.appplaylistmanager.domain.exception.type;

import com.sergiodev.appplaylistmanager.domain.exception.ApplicationException;
import com.sergiodev.appplaylistmanager.domain.exception.model.ApiExceptionResponse;
import com.sergiodev.appplaylistmanager.domain.exception.util.IErrorType;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.function.Supplier;

public class SecurityCustomException extends ApplicationException {
    // El constructor simplemente pasa los datos a la clase padre.
    // Es 'privado' para forzar el uso del enum como fábrica.
    private SecurityCustomException(IErrorType<SecurityCustomException> errorType, String finalDetail) {
        super(errorType, finalDetail);
    }

    /**
     * Enum que agrupa errores genéricos de la aplicación y actúa como fábrica.
     */
    @Getter
    public enum Type implements IErrorType<SecurityCustomException> {

        USERNAME_ALREADY_EXISTS(
            HttpStatus.CONFLICT,
            "Username already exists",
            "SEC-001",
            "The username %s is already taken. Please choose a different one."
        ),
        EMAIL_ALREADY_EXISTS(
            HttpStatus.CONFLICT,
            "Email already exists",
            "SEC-002",
            "The email %s is already registered. Please use a different email."
        ),;

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
        public SecurityCustomException build() {
            // Llama al constructor privado con el mensaje de detalle por defecto.
            return new SecurityCustomException(this, this.detail);
        }

        @Override
        public SecurityCustomException build(Object... args) {
            // Llama al constructor privado con un mensaje formateado.
            return new SecurityCustomException(this, String.format(this.detail, args));
        }

        @Override
        public Supplier<SecurityCustomException> defer() {
            return () -> new SecurityCustomException(this, this.detail);
        }

        @Override
        public Supplier<SecurityCustomException> defer(Object... args) {
            return () -> new SecurityCustomException(this, String.format(this.detail, args));
        }

        @Override
        public String getType() {
            return SecurityCustomException.class.getSimpleName();
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
