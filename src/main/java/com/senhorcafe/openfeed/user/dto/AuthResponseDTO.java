package com.senhorcafe.openfeed.user.dto;

public record AuthResponseDTO(
        String token,
        Long id,
        String email,
        String username
) {}
