package com.senhorcafe.openfeed.post.dto;

public record PostDTO(
    long id,
    String titulo,
    String conteudo,
    String tag,
    String dataPostagem,
    long totalLikes,
    boolean usuarioAtualCurtiu
) {}
