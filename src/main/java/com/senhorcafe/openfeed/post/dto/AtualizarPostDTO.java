package com.senhorcafe.openfeed.post.dto;

import jakarta.validation.constraints.Size;

public record AtualizarPostDTO(
        @Size(min = 5, max = 100) String titulo,
        @Size(min = 1, max = 250) String conteudo,
        String tag
) {}
