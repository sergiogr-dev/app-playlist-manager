package com.sergiodev.appplaylistmanager.domain.model;

import java.util.Set;

public record Playlist(
    Long id,
    String name,
    String description,
    Set<Song> songs
) {
}
