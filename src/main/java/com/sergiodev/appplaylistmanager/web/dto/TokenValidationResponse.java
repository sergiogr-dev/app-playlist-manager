package com.sergiodev.appplaylistmanager.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenValidationResponse {

    private boolean valid;
    private String username;
    private String email;
    private Long userId;
    private Long expiresAt;
    private String message;
}
