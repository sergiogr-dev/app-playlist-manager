package com.sergiodev.appplaylistmanager.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

import static java.util.Objects.isNull;

public record SpotifyTokenResponse(

    @JsonProperty("access_token")
    String accessToken,

    @JsonProperty("token_type")
    String tokenType,

    @JsonProperty("expires_in")
    Integer expiresIn,

    Instant createdAt
) {
    // Constructor para crear la respuesta con timestamp actual
    public SpotifyTokenResponse(String accessToken, String tokenType, Integer expiresIn) {
        this(accessToken, tokenType, expiresIn, Instant.now());
    }

    // Metodo para verificar si el token ha expirado
    public boolean isExpired() {
        if (isNull(createdAt) || isNull(expiresIn)) {
            return true;
        }
        // Agregamos un margen de seguridad de 60 segundos
        return Instant.now().isAfter(createdAt.plusSeconds(expiresIn - 60));
    }
}