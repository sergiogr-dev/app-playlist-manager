package com.sergiodev.appplaylistmanager.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "DTO para solicitudes de creación y actualización de canciones")
public record SongRequestDTO(

    @Schema(description = "Título de la canción", example = "Bohemian Rhapsody", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "El título no puede estar vacío")
    @Size(max = 200, message = "El título no puede exceder los 200 caracteres")
    String title,

    @Schema(description = "Artista de la canción", example = "Queen", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "El artista no puede estar vacío")
    @Size(max = 100, message = "El artista no puede exceder los 100 caracteres")
    String artist,

    @Schema(description = "Álbum de la canción", example = "A Night at the Opera")
    @Size(max = 150, message = "El álbum no puede exceder los 150 caracteres")
    String album,

    @Schema(description = "Año de lanzamiento de la canción", example = "1975")
    @Min(value = 1900, message = "El año debe ser mayor a 1900")
    @Max(value = 2030, message = "El año no puede ser mayor a 2030")
    Integer year,

    @Schema(description = "Género musical", example = "Rock")
    @Size(max = 50, message = "El género no puede exceder los 50 caracteres")
    String genre
) {
}
