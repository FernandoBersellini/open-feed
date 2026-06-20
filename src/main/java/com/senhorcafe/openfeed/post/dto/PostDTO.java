package com.senhorcafe.openfeed.post.dto;

public record PostDTO(
    String titulo,
    String conteudo,
    String tag,
    String dataPostagem
) {}
