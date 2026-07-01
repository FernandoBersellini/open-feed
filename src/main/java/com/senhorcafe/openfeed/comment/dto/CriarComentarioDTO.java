package com.senhorcafe.openfeed.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CriarComentarioDTO(
        @NotBlank @Size(min = 1, max = 250) String conteudo,
        @NotNull Long idUsuario
) {}
