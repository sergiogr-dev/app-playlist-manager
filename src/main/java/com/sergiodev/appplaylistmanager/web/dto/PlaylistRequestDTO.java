package com.sergiodev.appplaylistmanager.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;

@Schema(description = "DTO para solicitudes de creación y actualización de playlists")
public record PlaylistRequestDTO(

    @Schema(description = "Nombre de la playlist", example = "Mi Playlist Favorita", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "El nombre de la playlist no puede estar vacío")
    @Size(max = 100, message = "El nombre no puede exceder los 100 caracteres")
    String name,

    @Schema(description = "Descripción de la playlist", example = "Una colección de mis canciones favoritas")
    @Size(max = 500, message = "La descripción no puede exceder los 500 caracteres")
    String description,

    @Schema(description = "Conjunto de canciones de la playlist")
    @NotNull(message = "El conjunto de canciones no puede ser nulo")
    @Valid
    Set<SongRequestDTO> songs
) {
}
