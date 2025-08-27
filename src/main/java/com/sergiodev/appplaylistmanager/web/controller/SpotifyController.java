package com.sergiodev.appplaylistmanager.web.controller;

import com.sergiodev.appplaylistmanager.aaplication.service.SpotifyService;
import com.sergiodev.appplaylistmanager.web.util.ApiResponseBuilder;
import com.sergiodev.appplaylistmanager.web.util.StandardizeApiResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/v1/spotify")
@RequiredArgsConstructor
public class SpotifyController implements ApiResponseBuilder {

    private final SpotifyService spotifyService;

    @GetMapping
    public Mono<ResponseEntity<StandardizeApiResponse<List<String>>>> getMarkets() {
        String traceId = MDC.get("traceId");
        return spotifyService.getMarkets()
            .collectList()
            .map(markets -> ResponseEntity.ok(buildSuccessResponse(markets, traceId, HttpStatus.OK)));
    }
}
