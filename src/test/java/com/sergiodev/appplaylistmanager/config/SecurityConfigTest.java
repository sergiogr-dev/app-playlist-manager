package com.sergiodev.appplaylistmanager.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SecurityConfig Simple Tests")
class SecurityConfigTest {

    @Mock
    private SecurityProperties securityProperties;

    @Mock
    private SecurityProperties.Cors corsProperties;

    @InjectMocks
    private SecurityConfig securityConfig;

    private static final String EXAMPLE_COM_URL = "https://example.com";

    @Test
    @DisplayName("Should create password encoder")
    void shouldCreatePasswordEncoder() {
        // When
        PasswordEncoder result = securityConfig.passwordEncoder();

        // Then
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("Should handle null CORS properties gracefully")
    void shouldHandleNullCorsPropertiesGracefully() {
        // Given
        when(securityProperties.getCors()).thenReturn(null);

        // When & Then - Esperamos NullPointerException cuando corsProperties es null
        assertThatThrownBy(() -> securityConfig.corsConfigurationSource())
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("Should mock construction of CorsConfiguration and UrlBasedCorsConfigurationSource")
    void shouldMockConstructionOfCorsConfigurationAndUrlBasedCorsConfigurationSource() {
        // Given
        when(corsProperties.getAllowedOrigins()).thenReturn(Arrays.asList(EXAMPLE_COM_URL));
        when(corsProperties.getAllowedMethods()).thenReturn(Arrays.asList("GET"));
        when(corsProperties.getAllowedHeaders()).thenReturn(Arrays.asList("*"));
        when(corsProperties.getAllowCredentials()).thenReturn(true);
        when(securityProperties.getCors()).thenReturn(corsProperties);

        // Using mockConstruction to demonstrate mocking of object creation
        try (MockedConstruction<CorsConfiguration> corsConfigMock =
                 mockConstruction(CorsConfiguration.class);
             MockedConstruction<UrlBasedCorsConfigurationSource> sourceMock =
                 mockConstruction(UrlBasedCorsConfigurationSource.class)) {

            // When
            CorsConfigurationSource result = securityConfig.corsConfigurationSource();

            // Then
            assertThat(result).isNotNull();

            // Verificar que se crearon las instancias esperadas
            assertThat(corsConfigMock.constructed()).hasSize(1);
            assertThat(sourceMock.constructed()).hasSize(1);

            // Verificar interacciones con las instancias mockeadas
            CorsConfiguration mockedConfig = corsConfigMock.constructed().get(0);
            UrlBasedCorsConfigurationSource mockedSource = sourceMock.constructed().get(0);

            // Verificar que se llamaron los métodos esperados en las instancias
            verify(mockedConfig).setAllowedOrigins(Arrays.asList(EXAMPLE_COM_URL));
            verify(mockedConfig).setAllowedMethods(Arrays.asList("GET"));
            verify(mockedConfig).setAllowedHeaders(Arrays.asList("*"));
            verify(mockedConfig).setAllowCredentials(true);
            verify(mockedConfig).setMaxAge(3600L);
            verify(mockedSource).registerCorsConfiguration(eq("/**"), eq(mockedConfig));
        }
    }
}
