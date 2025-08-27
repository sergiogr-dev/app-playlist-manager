package com.sergiodev.appplaylistmanager.integration;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class PasswordEncoderTest {


    @Test
    void generateEncodedPassword() {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String rawPassword = "password123";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        System.out.println("Raw password: " + rawPassword);
        System.out.println("Encoded password: " + encodedPassword);
        System.out.println("Matches: " + passwordEncoder.matches(rawPassword, encodedPassword));
    }
}
