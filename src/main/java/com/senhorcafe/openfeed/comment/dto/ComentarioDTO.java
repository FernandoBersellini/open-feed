package com.senhorcafe.openfeed.comment.dto;

import java.time.LocalDateTime;

public record ComentarioDTO(
    Long idComentario,
    String conteudo,
    LocalDateTime dataComentario,
    Long idUsuario,
    String username
) {}
