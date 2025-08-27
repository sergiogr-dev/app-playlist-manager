package com.sergiodev.appplaylistmanager.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.Set;

@Schema(description = "DTO para solicitudes de creación y actualización de playlists")
@Builder(toBuilder = true)
public record PlaylistResponseDTO(

    @Schema(description = "ID único de la playlist", example = "1")
    Long id,

    @Schema(description = "Nombre de la playlist", example = "Mi Playlist Favorita")
    String name,

    @Schema(description = "Descripción de la playlist", example = "Una colección de mis canciones favoritas")
    String description,

    @Schema(description = "Conjunto de canciones de la playlist")
    Set<SongResponseDTO> songs
) {
}
