package com.sergiodev.appplaylistmanager.domain.model;

public record Song(
    Long id,
    String title,
    String artist,
    String album,
    Integer year,
    String genre
) {
}
