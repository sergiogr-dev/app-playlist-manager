package com.sergiodev.appplaylistmanager.infrastructure.persistance.playlist;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.Optional;

public interface PlayListJpaRepository extends JpaRepository<PlaylistEntity, Long> {
    Boolean existsByNameIgnoreCase(String name);
    Optional<PlaylistEntity> findByName(String name);
    @Modifying
    @Transactional
    void deleteByName(String name);
}
