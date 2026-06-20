package com.senhorcafe.openfeed.post.dto;

import jakarta.validation.constraints.*;

public record CriarPostDTO(
        @NotNull Long idUsuario,
        @NotBlank @Size(min = 5, max = 100) String titulo,
        @NotBlank @Size(min = 1, max = 250) String conteudo,
        String tag
) {}
