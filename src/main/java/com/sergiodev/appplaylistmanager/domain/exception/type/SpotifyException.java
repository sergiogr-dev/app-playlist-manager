package com.sergiodev.appplaylistmanager.domain.exception.type;

import com.sergiodev.appplaylistmanager.domain.exception.ApplicationException;
import com.sergiodev.appplaylistmanager.domain.exception.model.ApiExceptionResponse;
import com.sergiodev.appplaylistmanager.domain.exception.util.IErrorType;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.function.Supplier;

public class SpotifyException extends ApplicationException {
    // El constructor simplemente pasa los datos a la clase padre.
    // Es 'privado' para forzar el uso del enum como fábrica.
    private SpotifyException(IErrorType<SpotifyException> errorType, String finalDetail) {
        super(errorType, finalDetail);
    }

    /**
     * Enum que agrupa errores genéricos de la aplicación y actúa como fábrica.
     */
    @Getter
    public enum Type implements IErrorType<SpotifyException> {

        SPOTIFY_SERVICE_CLIENT_EXCEPTION(
            HttpStatus.SERVICE_UNAVAILABLE,
            "Error en el servicio de Spotify",
            "SPOTIFY-001",
            "Error al comunicarse con el servicio de Spotify: %s"
        ),
        SPOTIFY_SERVICE_SERVER_EXCEPTION(
            HttpStatus.SERVICE_UNAVAILABLE,
            "Error en el servicio de Spotify",
            "SPOTIFY-002",
            "El servicio de Spotify no está disponible temporalmente. Inténtelo de nuevo más tarde."
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
        public SpotifyException build() {
            // Llama al constructor privado con el mensaje de detalle por defecto.
            return new SpotifyException(this, this.detail);
        }

        @Override
        public SpotifyException build(Object... args) {
            // Llama al constructor privado con un mensaje formateado.
            return new SpotifyException(this, String.format(this.detail, args));
        }

        @Override
        public Supplier<SpotifyException> defer() {
            return () -> new SpotifyException(this, this.detail);
        }

        @Override
        public Supplier<SpotifyException> defer(Object... args) {
            return () -> new SpotifyException(this, String.format(this.detail, args));
        }

        @Override
        public String getType() {
            return SpotifyException.class.getSimpleName();
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
