package com.sergiodev.appplaylistmanager.domain.model;

import lombok.Builder;

@Builder(toBuilder = true)
public record Song(
    Long id,
    String title,
    String artist,
    String album,
    Integer year,
    String genre
) {
}
