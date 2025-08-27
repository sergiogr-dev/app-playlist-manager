package com.sergiodev.appplaylistmanager.web.controller;

import com.sergiodev.appplaylistmanager.aaplication.service.PlaylistService;
import com.sergiodev.appplaylistmanager.web.dto.PlaylistRequestDTO;
import com.sergiodev.appplaylistmanager.web.dto.PlaylistResponseDTO;
import com.sergiodev.appplaylistmanager.web.util.mapper.PlaylistRequestMapper;
import com.sergiodev.appplaylistmanager.web.util.mapper.PlaylistResponseMapper;
import com.sergiodev.appplaylistmanager.web.util.ApiResponse;
import com.sergiodev.appplaylistmanager.web.util.ApiResponseBuilder;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/lists")
@RequiredArgsConstructor
public class PlaylistController implements ApiResponseBuilder {
    private final PlaylistService playlistService;
    private final PlaylistRequestMapper requestMapper;
    private final PlaylistResponseMapper responseMapper;

    @PostMapping
    public Mono<ResponseEntity<ApiResponse<PlaylistResponseDTO>>> createPlaylist(
        @RequestBody @Valid PlaylistRequestDTO playlist
    ) {
        String traceId = MDC.get("traceId");
        return playlistService.create(requestMapper.toDomain(playlist))
            .map(responseMapper::toObject)
            .map(playlistSaved -> {
                ApiResponse<PlaylistResponseDTO> playlistResponseDTOApiResponse = buildSuccessResponse(playlistSaved, traceId, HttpStatus.CREATED);
                return ResponseEntity.status(playlistResponseDTOApiResponse.httpCode()).body(playlistResponseDTOApiResponse);
            });
    }
}
