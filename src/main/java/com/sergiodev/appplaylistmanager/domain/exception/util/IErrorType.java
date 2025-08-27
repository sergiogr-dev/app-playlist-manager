package com.sergiodev.appplaylistmanager.domain.exception.util;

import com.sergiodev.appplaylistmanager.domain.exception.ApplicationException;
import com.sergiodev.appplaylistmanager.domain.exception.model.ApiExceptionResponse;
import org.springframework.http.HttpStatus;

import java.util.function.Supplier;

/**
 * Interfaz genérica que actúa como una fábrica para crear excepciones.
 * Cualquier enum que represente un grupo de errores debe implementarla.
 *
 * @param <E> El tipo de la excepción que esta fábrica puede construir, debe heredar de ApplicationException.
 */
public interface IErrorType<E extends ApplicationException> {
    // --- Métodos de datos del error ---


    String getType();
    String getTitle();
    String getCode();
    String getDetail();
    HttpStatus getHttpStatus();

    // --- Métodos de la Fábrica (Factory) ---

    /**
     * Construye y retorna una instancia de la excepción asociada.
     * Usa el 'detail' por defecto.
     * @return una nueva instancia de la excepción de tipo E.
     */
    E build();

    /**
     * Construye y retorna una instancia de la excepción, formateando el mensaje de detalle.
     * Permite añadir información dinámica al error. Ejemplo: "El usuario con ID '%s' no fue encontrado".
     * @param args los argumentos para formatear el mensaje de detalle.
     * @return una nueva instancia de la excepción de tipo E con un mensaje formateado.
     */
    E build(Object... args);

    /**
     * Proporciona un Supplier que puede ser usado para crear la excepción de forma diferida.
     * Esto es útil para evitar la creación de excepciones hasta que realmente se necesiten.
     * @return un Supplier que retorna una nueva instancia de la excepción de tipo E.
     */
    Supplier<E> defer();

    /**
     * Proporciona un Supplier que puede ser usado para crear la excepción de forma diferida,
     * permitiendo pasar argumentos para formatear el mensaje de detalle.
     * @param args los argumentos para formatear el mensaje de detalle.
     * @return un Supplier que retorna una nueva instancia de la excepción de tipo E con un mensaje formateado.
     */
    Supplier<E> defer(Object... args);

    /**
     * Permite crear el body pasando como parámetro una lista de errores
     * @param errorDetails lista de errores. Ejemplo: '{"Error de validación en el item 1", "Item 2 sin stock"}'
     * @return objeto estándar con la información de la excepción y el detalle proporcionado
     */
    ApiExceptionResponse body(String[] errorDetails);
}
