package com.sergiodev.appplaylistmanager.domain.model;

import lombok.Builder;

import java.util.Set;

@Builder(toBuilder = true)
public record Playlist(
    Long id,
    String name,
    String description,
    Set<Song> songs
) {
}
