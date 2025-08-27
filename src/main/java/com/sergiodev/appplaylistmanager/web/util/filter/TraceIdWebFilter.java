package com.sergiodev.appplaylistmanager.web.util.filter;

import lombok.NonNull;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Filtro web para gestionar identificadores de trazabilidad (trace IDs) en aplicaciones reactivas.
 * <p>
 * Este filtro intercepta todas las solicitudes HTTP entrantes y:
 * <ul>
 *   <li>Extrae el trace ID del header {@code X-Trace-Id} si está presente</li>
 *   <li>Genera un nuevo trace ID usando UUID si no existe</li>
 *   <li>Agrega el trace ID al MDC de SLF4J para logging contextual</li>
 *   <li>Incluye el trace ID en la respuesta HTTP como header</li>
 *   <li>Limpia el MDC al finalizar la solicitud</li>
 * </ul>
 * <p>
 * El filtro está configurado con {@code @Order(-1)} para ejecutarse antes que otros filtros.
 *
 * @author sergiogr-dev
 * @version 1.0
 * @since 1.0
 */
@Component
@Order(-1) // Asegura que este filtro se ejecute primero
public class TraceIdWebFilter implements WebFilter {

    /** Nombre del header HTTP para el identificador de trazabilidad */
    private static final String TRACE_ID_HEADER = "X-Trace-Id";

    /** Clave utilizada en el MDC para almacenar el trace ID */
    private static final String TRACE_ID_MDC_KEY = "traceId";

    /**
     * Filtra las solicitudes HTTP para gestionar el trace ID.
     * <p>
     * Obtiene o crea un trace ID, lo almacena en el MDC, lo agrega como header
     * de respuesta y asegura la limpieza del MDC al finalizar.
     *
     * @param exchange el intercambio de servidor web que contiene la solicitud y respuesta
     * @param chain la cadena de filtros web
     * @return un {@code Mono<Void>} que indica cuando el filtrado está completo
     */
    @Override
    public @NonNull Mono<Void> filter(@NonNull ServerWebExchange exchange, WebFilterChain chain) {
        String traceId = getOrCreateTraceId(exchange);

        return chain.filter(exchange)
            .doFinally(signalType -> MDC.remove(TRACE_ID_MDC_KEY))
            .contextWrite(context -> {
                MDC.put(TRACE_ID_MDC_KEY, traceId);
                exchange.getResponse().getHeaders().add(TRACE_ID_HEADER, traceId);
                return context;
            });
    }

    /**
     * Obtiene el trace ID del header de la solicitud o genera uno nuevo si no existe.
     * <p>
     * Primero busca el header {@code X-Trace-Id} en la solicitud entrante.
     * Si no está presente o está vacío, genera un nuevo UUID como trace ID.
     *
     * @param exchange el intercambio de servidor web
     * @return el trace ID existente o uno nuevo generado
     */
    private String getOrCreateTraceId(ServerWebExchange exchange) {
        String traceId = exchange.getRequest().getHeaders().getFirst(TRACE_ID_HEADER);
        if (traceId == null || traceId.isEmpty()) {
            traceId = UUID.randomUUID().toString();
        }
        return traceId;
    }
}