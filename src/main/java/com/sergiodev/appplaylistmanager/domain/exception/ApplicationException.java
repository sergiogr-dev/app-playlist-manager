package com.sergiodev.appplaylistmanager.domain.exception;

import com.sergiodev.appplaylistmanager.domain.exception.util.IErrorType;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Clase base, abstracta y NO genérica para todas las excepciones controladas de la aplicación.
 * El diseño ahora es más simple, ya que recibe un IErrorType que contiene toda la configuración.
 */
@Getter
public abstract class ApplicationException extends RuntimeException {

    private final IErrorType<? extends ApplicationException> errorType;
    private final String finalDetail;

    /**
     * Constructor principal que será llamado por los métodos 'build()' del enum.
     * @param errorType El enum que implementa IErrorType y define el error.
     * @param finalDetail El mensaje de detalle final, posiblemente ya formateado.
     */
    protected ApplicationException(IErrorType<? extends ApplicationException> errorType, String finalDetail) {
        super(finalDetail); // El mensaje para la clase Exception padre.
        this.errorType = errorType;
        this.finalDetail = finalDetail;
    }

    // --- Getters que delegan la llamada al errorType ---

    public HttpStatus getHttpStatus() {
        return this.errorType.getHttpStatus();
    }
}
