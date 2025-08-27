package com.sergiodev.appplaylistmanager.aaplication.service;

import com.sergiodev.appplaylistmanager.aaplication.port.out.PlaylistRepository;
import com.sergiodev.appplaylistmanager.domain.exception.type.CommonException;
import com.sergiodev.appplaylistmanager.domain.model.Playlist;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlaylistServiceTest {

    @Mock
    private PlaylistRepository playlistRepository;

    @InjectMocks
    private PlaylistService playlistService;

    private Playlist playlist;

    @BeforeEach
    void setUp() {
        playlist = Playlist.builder()
            .name("Rock")
            .description("Playlist de rock")
            .build();
    }

    @Test
    void create_Success() {
        when(playlistRepository.existsByNameIgnoreCase(anyString())).thenReturn(Mono.just(false));
        when(playlistRepository.save(any(Playlist.class))).thenReturn(Mono.just(playlist));

        StepVerifier.create(playlistService.create(playlist))
            .expectNext(playlist)
            .verifyComplete();

        verify(playlistRepository).existsByNameIgnoreCase("Rock");
        verify(playlistRepository).save(playlist);
    }

    @Test
    void create_AlreadyExists_Error() {
        when(playlistRepository.existsByNameIgnoreCase(anyString())).thenReturn(Mono.just(true));

        StepVerifier.create(playlistService.create(playlist))
            .expectErrorMatches(e -> e instanceof CommonException cEx &&
                                     (cEx.getErrorType() == CommonException.Type.RESOURCE_ALREADY_EXISTS))
            .verify();

        verify(playlistRepository).existsByNameIgnoreCase("Rock");
        verify(playlistRepository, never()).save(any());
    }

    @Test
    void findById_Success() {
        when(playlistRepository.findByName(anyString())).thenReturn(Mono.just(playlist));

        StepVerifier.create(playlistService.findById("Rock"))
            .expectNext(playlist)
            .verifyComplete();

        verify(playlistRepository).findByName("Rock");
    }

    @Test
    void findById_NotFound() {
        when(playlistRepository.findByName(anyString())).thenReturn(Mono.empty());

        StepVerifier.create(playlistService.findById("Rock"))
            .expectComplete()
            .verify();

        verify(playlistRepository).findByName("Rock");
    }

    @Test
    void findAll_Success() {
        when(playlistRepository.findAll()).thenReturn(Flux.just(playlist));

        StepVerifier.create(playlistService.findAll())
            .expectNext(playlist)
            .verifyComplete();

        verify(playlistRepository).findAll();
    }

    @Test
    void findAll_Empty() {
        when(playlistRepository.findAll()).thenReturn(Flux.empty());

        StepVerifier.create(playlistService.findAll())
            .expectComplete()
            .verify();

        verify(playlistRepository).findAll();
    }

    @Test
    void deleteByName_Success() {
        when(playlistRepository.existsByNameIgnoreCase(anyString())).thenReturn(Mono.just(true));
        when(playlistRepository.deleteByName(anyString())).thenReturn(Mono.empty());

        StepVerifier.create(playlistService.deleteByName("Rock"))
            .verifyComplete();

        verify(playlistRepository).existsByNameIgnoreCase("Rock");
        verify(playlistRepository).deleteByName("Rock");
    }

    @Test
    void deleteByName_NotFound_Error() {
        when(playlistRepository.existsByNameIgnoreCase(anyString())).thenReturn(Mono.just(false));

        StepVerifier.create(playlistService.deleteByName("Rock"))
            .expectErrorMatches(e -> e instanceof CommonException cEx &&
                                     (cEx.getErrorType() == CommonException.Type.RESOURCE_NOT_FOUND))
            .verify();

        verify(playlistRepository).existsByNameIgnoreCase("Rock");
        verify(playlistRepository, never()).deleteByName(any());
    }
}