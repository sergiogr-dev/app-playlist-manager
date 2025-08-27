package com.sergiodev.appplaylistmanager.infrastructure.persistance.playlist;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlayListJpaRepository extends JpaRepository<PlaylistEntity, Long> {
    Boolean existsByNameIgnoreCase(String name);
    Optional<PlaylistEntity> findByName(String name);
}
