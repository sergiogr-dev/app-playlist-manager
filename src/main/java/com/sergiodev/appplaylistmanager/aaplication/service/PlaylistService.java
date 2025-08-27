package com.sergiodev.appplaylistmanager.aaplication.service;

import com.sergiodev.appplaylistmanager.aaplication.port.out.PlaylistRepository;
import com.sergiodev.appplaylistmanager.domain.exception.type.CommonException;
import com.sergiodev.appplaylistmanager.domain.model.Playlist;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class PlaylistService {
    private final PlaylistRepository playlistRepository;

    public Mono<Playlist> create(Playlist playlist) {
        return playlistRepository.existsByNameIgnoreCase(playlist.name())
            .flatMap(exists -> exists ?
                Mono.error(CommonException.Type.RESOURCE_ALREADY_EXISTS.build(playlist.name())) :
                playlistRepository.save(playlist)
            );
    }
}
