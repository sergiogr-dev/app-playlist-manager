package com.sergiodev.appplaylistmanager.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO para respuestas de canciones")
public record SongResponseDTO(

    @Schema(description = "Identificador único de la canción", example = "1")
    Long id,

    @Schema(description = "Título de la canción", example = "Bohemian Rhapsody")
    String title,

    @Schema(description = "Artista de la canción", example = "Queen")
    String artist,

    @Schema(description = "Álbum de la canción", example = "A Night at the Opera")
    String album,

    @Schema(description = "Año de lanzamiento de la canción", example = "1975")
    Integer year,

    @Schema(description = "Género musical", example = "Rock")
    String genre
) {
}
