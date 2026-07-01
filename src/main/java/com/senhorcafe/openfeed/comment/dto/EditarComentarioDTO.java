package com.senhorcafe.openfeed.comment.dto;

import jakarta.validation.constraints.Size;

public record EditarComentarioDTO(
        @Size(min = 1, max = 250) String conteudo
) {}
