package com.senhorcafe.openfeed.post.entity;

import com.senhorcafe.openfeed.post.tags.Tags;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "post")
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "titulo")
    private String titulo;

    @Column(name = "conteudo")
    private String conteudo;

    @CreationTimestamp
    private LocalDateTime dataPostagem;

    @Nullable
    @Column(name = "tag")
    private String tag;
}
