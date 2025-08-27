package com.sergiodev.appplaylistmanager.web.controller;

import com.sergiodev.appplaylistmanager.aaplication.service.PlaylistService;
import com.sergiodev.appplaylistmanager.domain.model.Playlist;
import com.sergiodev.appplaylistmanager.web.dto.PlaylistRequestDTO;
import com.sergiodev.appplaylistmanager.web.dto.PlaylistResponseDTO;
import com.sergiodev.appplaylistmanager.web.util.StandardizeApiResponse;
import com.sergiodev.appplaylistmanager.web.util.mapper.PlaylistRequestMapper;
import com.sergiodev.appplaylistmanager.web.util.mapper.PlaylistResponseMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlaylistControllerTest {

    @Mock
    private PlaylistService playlistService;
    @Mock
    private PlaylistRequestMapper requestMapper;
    @Mock
    private PlaylistResponseMapper responseMapper;

    @InjectMocks
    private PlaylistController playlistController;

    private PlaylistRequestDTO requestDTO;
    private PlaylistResponseDTO responseDTO;
    private Playlist playlist;
    private String traceId;

    @BeforeEach
    void setUp() {
        traceId = "test-trace-id";

        requestDTO = PlaylistRequestDTO.builder()
            .name("Rock Playlist")
            .build();

        responseDTO = PlaylistResponseDTO.builder()
            .id(1L)
            .name("Rock Playlist")
            .build();

        playlist = Playlist.builder()
            .id(1L)
            .name("Rock Playlist")
            .build();
    }

    @Test
    void createPlaylist_ShouldReturnCreatedResponse() {
        try (MockedStatic<MDC> mdcMock = mockStatic(MDC.class)) {
            mdcMock.when(() -> MDC.get("traceId")).thenReturn(traceId);
            when(requestMapper.toDomain(requestDTO)).thenReturn(playlist);
            when(playlistService.create(playlist)).thenReturn(Mono.just(playlist));
            when(responseMapper.toObject(playlist)).thenReturn(responseDTO);

            Mono<ResponseEntity<StandardizeApiResponse<PlaylistResponseDTO>>> result =
                playlistController.createPlaylist(requestDTO);

            StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.CREATED, response.getStatusCode());
                    assertNotNull(response.getBody());
                    assertEquals(201, response.getBody().httpCode());
                    assertEquals(responseDTO, response.getBody().data());
                })
                .verifyComplete();

            verify(requestMapper).toDomain(requestDTO);
            verify(playlistService).create(playlist);
            verify(responseMapper).toObject(playlist);
        }
    }

    @Test
    void createPlaylist_ShouldHandleServiceException() {
        try (MockedStatic<MDC> mdcMock = mockStatic(MDC.class)) {
            mdcMock.when(() -> MDC.get("traceId")).thenReturn(traceId);
            when(requestMapper.toDomain(requestDTO)).thenReturn(playlist);
            when(playlistService.create(playlist)).thenReturn(Mono.error(new RuntimeException("Service error")));

            Mono<ResponseEntity<StandardizeApiResponse<PlaylistResponseDTO>>> result =
                playlistController.createPlaylist(requestDTO);

            StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();

            verify(requestMapper).toDomain(requestDTO);
            verify(playlistService).create(playlist);
        }
    }

    @Test
    void getPlaylistByName_ShouldReturnPlaylist() {
        try (MockedStatic<MDC> mdcMock = mockStatic(MDC.class)) {
            String listName = "Rock Playlist";
            mdcMock.when(() -> MDC.get("traceId")).thenReturn(traceId);
            when(playlistService.findById(listName)).thenReturn(Mono.just(playlist));
            when(responseMapper.toObject(playlist)).thenReturn(responseDTO);

            Mono<ResponseEntity<StandardizeApiResponse<PlaylistResponseDTO>>> result =
                playlistController.getPlaylistByName(listName);

            StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertNotNull(response.getBody());
                    assertEquals(200, response.getBody().httpCode());
                    assertEquals(responseDTO, response.getBody().data());
                })
                .verifyComplete();

            verify(playlistService).findById(listName);
            verify(responseMapper).toObject(playlist);
        }
    }

    @Test
    void getPlaylistByName_ShouldHandleNotFound() {
        try (MockedStatic<MDC> mdcMock = mockStatic(MDC.class)) {
            String listName = "NonExistent";
            mdcMock.when(() -> MDC.get("traceId")).thenReturn(traceId);
            when(playlistService.findById(listName)).thenReturn(Mono.error(new RuntimeException("Not found")));

            Mono<ResponseEntity<StandardizeApiResponse<PlaylistResponseDTO>>> result =
                playlistController.getPlaylistByName(listName);

            StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();

            verify(playlistService).findById(listName);
        }
    }

    @Test
    void getAllPlaylists_ShouldReturnAllPlaylists() {
        try (MockedStatic<MDC> mdcMock = mockStatic(MDC.class)) {
            Playlist playlist2 = Playlist.builder().id(2L).name("Pop Playlist").build();
            PlaylistResponseDTO responseDTO2 = PlaylistResponseDTO.builder().id(2L).name("Pop Playlist").build();

            mdcMock.when(() -> MDC.get("traceId")).thenReturn(traceId);
            when(playlistService.findAll()).thenReturn(Flux.just(playlist, playlist2));
            when(responseMapper.toObject(playlist)).thenReturn(responseDTO);
            when(responseMapper.toObject(playlist2)).thenReturn(responseDTO2);

            Mono<ResponseEntity<StandardizeApiResponse<List<PlaylistResponseDTO>>>> result =
                playlistController.getAllPlaylists();

            StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertNotNull(response.getBody());
                    assertEquals(200, response.getBody().httpCode());
                    assertEquals(2, response.getBody().data().size());
                })
                .verifyComplete();

            verify(playlistService).findAll();
            verify(responseMapper, times(2)).toObject(any(Playlist.class));
        }
    }

    @Test
    void getAllPlaylists_ShouldHandleEmptyList() {
        try (MockedStatic<MDC> mdcMock = mockStatic(MDC.class)) {
            mdcMock.when(() -> MDC.get("traceId")).thenReturn(traceId);
            when(playlistService.findAll()).thenReturn(Flux.empty());

            Mono<ResponseEntity<StandardizeApiResponse<List<PlaylistResponseDTO>>>> result =
                playlistController.getAllPlaylists();

            StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertNotNull(response.getBody());
                    assertEquals(200, response.getBody().httpCode());
                    assertTrue(response.getBody().data().isEmpty());
                })
                .verifyComplete();

            verify(playlistService).findAll();
        }
    }

    @Test
    void getAllPlaylists_ShouldHandleServiceException() {
        try (MockedStatic<MDC> mdcMock = mockStatic(MDC.class)) {
            mdcMock.when(() -> MDC.get("traceId")).thenReturn(traceId);
            when(playlistService.findAll()).thenReturn(Flux.error(new RuntimeException("Service error")));

            Mono<ResponseEntity<StandardizeApiResponse<List<PlaylistResponseDTO>>>> result =
                playlistController.getAllPlaylists();

            StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();

            verify(playlistService).findAll();
        }
    }

    @Test
    void deletePlaylistById_ShouldReturnNoContent() {
        String listName = "Rock Playlist";
        when(playlistService.deleteByName(listName)).thenReturn(Mono.empty());

        Mono<ResponseEntity<Void>> result = playlistController.deletePlaylistById(listName);

        StepVerifier.create(result)
            .assertNext(response -> {
                assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
                assertNull(response.getBody());
            })
            .verifyComplete();

        verify(playlistService).deleteByName(listName);
    }

    @Test
    void deletePlaylistById_ShouldHandleServiceException() {
        String listName = "NonExistent";
        when(playlistService.deleteByName(listName)).thenReturn(Mono.error(new RuntimeException("Not found")));

        Mono<ResponseEntity<Void>> result = playlistController.deletePlaylistById(listName);

        StepVerifier.create(result)
            .expectError(RuntimeException.class)
            .verify();

        verify(playlistService).deleteByName(listName);
    }

    @Test
    void createPlaylist_ShouldHandleMDCNullTraceId() {
        try (MockedStatic<MDC> mdcMock = mockStatic(MDC.class)) {
            mdcMock.when(() -> MDC.get("traceId")).thenReturn(null);
            when(requestMapper.toDomain(requestDTO)).thenReturn(playlist);
            when(playlistService.create(playlist)).thenReturn(Mono.just(playlist));
            when(responseMapper.toObject(playlist)).thenReturn(responseDTO);

            Mono<ResponseEntity<StandardizeApiResponse<PlaylistResponseDTO>>> result =
                playlistController.createPlaylist(requestDTO);

            StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.CREATED, response.getStatusCode());
                    assertNotNull(response.getBody());
                })
                .verifyComplete();

            verify(requestMapper).toDomain(requestDTO);
            verify(playlistService).create(playlist);
            verify(responseMapper).toObject(playlist);
        }
    }

    @Test
    void getPlaylistByName_ShouldHandleMDCNullTraceId() {
        try (MockedStatic<MDC> mdcMock = mockStatic(MDC.class)) {
            String listName = "Rock Playlist";
            mdcMock.when(() -> MDC.get("traceId")).thenReturn(null);
            when(playlistService.findById(listName)).thenReturn(Mono.just(playlist));
            when(responseMapper.toObject(playlist)).thenReturn(responseDTO);

            Mono<ResponseEntity<StandardizeApiResponse<PlaylistResponseDTO>>> result =
                playlistController.getPlaylistByName(listName);

            StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertNotNull(response.getBody());
                })
                .verifyComplete();

            verify(playlistService).findById(listName);
            verify(responseMapper).toObject(playlist);
        }
    }
}