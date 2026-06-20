package com.senhorcafe.openfeed.post.service;

import com.senhorcafe.openfeed.post.dto.AtualizarPostDTO;
import com.senhorcafe.openfeed.post.dto.CriarPostDTO;
import com.senhorcafe.openfeed.post.dto.PostDTO;
import com.senhorcafe.openfeed.post.entity.Post;
import com.senhorcafe.openfeed.post.repository.PostRepository;
import com.senhorcafe.openfeed.post.tags.Tags;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PostService {
    private static final String TAG_INVALIDA = "Tag invalida";

    private final PostRepository postRepository;

    public ResponseEntity<List<PostDTO>> postIndex() {
        List<Post> postsFromBackend = postRepository.findAll();

        if (postsFromBackend.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        List<PostDTO> posts = postRepository.findAll().stream()
            .map(post -> new PostDTO(
                post.getTitulo(),
                post.getConteudo(),
                post.getTag(),
                post.getDataPostagem().toString()
            ))
            .toList();


        return ResponseEntity.ok(posts);
    }

    public ResponseEntity<String> createPost(CriarPostDTO criarPostDTO) {
        Post post = new Post();
        post.setTitulo(criarPostDTO.titulo());
        post.setConteudo(criarPostDTO.conteudo());

        String tagSanitizada = sanitizeTag(criarPostDTO.tag());

        if (TAG_INVALIDA.equals(tagSanitizada)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(TAG_INVALIDA);
        }

        post.setTag(tagSanitizada);

        postRepository.save(post);

        return ResponseEntity.status(HttpStatus.CREATED).body("Post salvo com sucesso");
    }

    public ResponseEntity<String> updatePost(Long postId, AtualizarPostDTO atualizarPostDTO) {
        Optional<Post> postOptional = postRepository.findById(postId);

        if (postOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Post post = postOptional.get();

        if (atualizarPostDTO.titulo() != null) {
            post.setTitulo(atualizarPostDTO.titulo());
        }
        if (atualizarPostDTO.conteudo() != null) {
            post.setConteudo(atualizarPostDTO.conteudo());
        }
        if (atualizarPostDTO.tag() != null) {
            String tagSanitizada = sanitizeTag(atualizarPostDTO.tag());

            if (TAG_INVALIDA.equals(tagSanitizada)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(TAG_INVALIDA);
            }

            post.setTag(tagSanitizada);
        }

        postRepository.save(post);

        return ResponseEntity.status(HttpStatus.OK).body("Post atualizado");
    }

    public ResponseEntity<String> deletePost(Long postId) {
        Optional<Post> postOptional = postRepository.findById(postId);

        if (postOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        postRepository.deleteById(postId);

        return ResponseEntity.status(HttpStatus.OK).body("Post deletado");
    }

    public String sanitizeTag(String tag) {
        if (tag == null || tag.isBlank()) {
            return null;
        }

        try {
            return Tags.valueOf(tag.trim().toUpperCase()).name();
        } catch (IllegalArgumentException e) {
            return "Tag invalida";
        }
    }
}
