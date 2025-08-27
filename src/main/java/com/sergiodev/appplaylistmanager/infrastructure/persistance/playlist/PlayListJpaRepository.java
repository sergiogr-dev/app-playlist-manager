package com.sergiodev.appplaylistmanager.infrastructure.persistance.playlist;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayListJpaRepository extends JpaRepository<PlaylistEntity, Long> {
    Boolean existsByNameIgnoreCase(String name);
}
