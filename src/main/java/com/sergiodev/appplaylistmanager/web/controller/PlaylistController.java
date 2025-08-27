package com.sergiodev.appplaylistmanager.web.controller;

import com.sergiodev.appplaylistmanager.aaplication.service.PlaylistService;
import com.sergiodev.appplaylistmanager.web.dto.PlaylistRequestDTO;
import com.sergiodev.appplaylistmanager.web.dto.PlaylistResponseDTO;
import com.sergiodev.appplaylistmanager.web.util.ApiResponseBuilder;
import com.sergiodev.appplaylistmanager.web.util.StandardizeApiResponse;
import com.sergiodev.appplaylistmanager.web.util.mapper.PlaylistRequestMapper;
import com.sergiodev.appplaylistmanager.web.util.mapper.PlaylistResponseMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/lists")
@RequiredArgsConstructor
public class PlaylistController implements ApiResponseBuilder {
    private final PlaylistService playlistService;
    private final PlaylistRequestMapper requestMapper;
    private final PlaylistResponseMapper responseMapper;

    @PostMapping
    public Mono<ResponseEntity<StandardizeApiResponse<PlaylistResponseDTO>>> createPlaylist(
        @RequestBody @Valid PlaylistRequestDTO playlist
    ) {
        String traceId = MDC.get("traceId");
        return playlistService.create(requestMapper.toDomain(playlist))
            .map(responseMapper::toObject)
            .map(playlistSaved -> {
                StandardizeApiResponse<PlaylistResponseDTO> playlistResponseDTOStandardizeApiResponse = buildSuccessResponse(playlistSaved, traceId, HttpStatus.CREATED);
                return ResponseEntity.status(playlistResponseDTOStandardizeApiResponse.httpCode()).body(playlistResponseDTOStandardizeApiResponse);
            });
    }

    @GetMapping("/{listName}")
    public Mono<ResponseEntity<StandardizeApiResponse<PlaylistResponseDTO>>> getPlaylistByName(@PathVariable String listName) {
        String traceId = MDC.get("traceId");
        return playlistService.findById(listName)
            .map(responseMapper::toObject)
            .map(playlistFound -> {
                StandardizeApiResponse<PlaylistResponseDTO> playlistResponseDTOStandardizeApiResponse = buildSuccessResponse(playlistFound, traceId, HttpStatus.OK);
                return ResponseEntity.status(playlistResponseDTOStandardizeApiResponse.httpCode()).body(playlistResponseDTOStandardizeApiResponse);
            });
    }

    @GetMapping
    public Mono<ResponseEntity<StandardizeApiResponse<List<PlaylistResponseDTO>>>> getAllPlaylists() {
        String traceId = MDC.get("traceId");
        return playlistService.findAll()
            .map(responseMapper::toObject)
            .collectList()
            .map(playlists -> {
                StandardizeApiResponse<List<PlaylistResponseDTO>> playlistResponseDTOStandardizeApiResponse = buildSuccessResponse(playlists, traceId, HttpStatus.OK);
                return ResponseEntity.status(playlistResponseDTOStandardizeApiResponse.httpCode()).body(playlistResponseDTOStandardizeApiResponse);
            });
    }

    @DeleteMapping("/{listName}")
    public Mono<ResponseEntity<Void>> deletePlaylistById(@PathVariable String listName) {
        return playlistService.deleteByName(listName)
            .then(Mono.just(ResponseEntity.noContent().build()));
    }
}
