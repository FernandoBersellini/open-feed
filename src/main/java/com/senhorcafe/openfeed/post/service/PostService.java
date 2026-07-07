package com.senhorcafe.openfeed.post.service;

import com.senhorcafe.openfeed.like.post_like.PostLikeRepository;
import com.senhorcafe.openfeed.post.dto.AtualizarPostDTO;
import com.senhorcafe.openfeed.post.dto.CriarPostDTO;
import com.senhorcafe.openfeed.post.dto.PostDTO;
import com.senhorcafe.openfeed.post.entity.Post;
import com.senhorcafe.openfeed.post.repository.PostRepository;
import com.senhorcafe.openfeed.post.tags.TagInvalidaException;
import com.senhorcafe.openfeed.post.tags.Tags;
import com.senhorcafe.openfeed.user.entity.User;
import com.senhorcafe.openfeed.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PostService {
    private static final String TAG_INVALIDA = "Tag invalida";

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostLikeRepository postLikeRepository;

    public ResponseEntity<List<PostDTO>> postIndex(Long userId) {
        List<Post> postsFromBackend = postRepository.findAllByUserId(userId);
        Long authenticatedUserId = getOptionalAuthenticatedUserId();

        List<PostDTO> posts = postsFromBackend.stream()
            .map(post -> new PostDTO(
                post.getId(),
                post.getTitulo(),
                post.getConteudo(),
                post.getTag(),
                post.getDataPostagem().toString(),
                postLikeRepository.countByPostId(post.getId()),
                authenticatedUserId != null && postLikeRepository.findByUserIdAndPostId(authenticatedUserId, post.getId()).isPresent()
            ))
            .toList();

        return ResponseEntity.ok(posts);
    }

    public ResponseEntity<String> createPost(CriarPostDTO criarPostDTO) {
        Long userId = getAuthenticatedUserId();
        Optional<User> userOptional = userRepository.findById(userId);

        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario nao encontrado");
        }

        Post post = new Post();
        post.setTitulo(criarPostDTO.titulo());
        post.setConteudo(criarPostDTO.conteudo());
        post.setUser(userOptional.get());
        post.setTag(sanitizeTag(criarPostDTO.tag()));
        postRepository.save(post);

        return ResponseEntity.status(HttpStatus.CREATED).body("Post salvo com sucesso");
    }

    public ResponseEntity<String> updatePost(Long postId, AtualizarPostDTO atualizarPostDTO) {
        Long userId = getAuthenticatedUserId();
        Optional<Post> postOptional = postRepository.findById(postId);

        if (postOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Post post = postOptional.get();

        if (!post.getUser().getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Este post nao pertence ao usuario informado");
        }

        if (atualizarPostDTO.titulo() != null) {
            post.setTitulo(atualizarPostDTO.titulo());
        }
        if (atualizarPostDTO.conteudo() != null) {
            post.setConteudo(atualizarPostDTO.conteudo());
        }
        if (atualizarPostDTO.tag() != null) {
            post.setTag(sanitizeTag(atualizarPostDTO.tag()));
        }

        postRepository.save(post);

        return ResponseEntity.status(HttpStatus.OK).body("Post atualizado");
    }

    public ResponseEntity<String> deletePost(Long postId) {
        Long userId = getAuthenticatedUserId();
        Optional<Post> postOptional = postRepository.findById(postId);

        if (postOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        if (!postOptional.get().getUser().getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Este post nao pertence ao usuario informado");
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
            throw new TagInvalidaException(TAG_INVALIDA);
        }
    }

    private Long getAuthenticatedUserId() {
        return (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private Long getOptionalAuthenticatedUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication() != null
            ? SecurityContextHolder.getContext().getAuthentication().getPrincipal()
            : null;

        return principal instanceof Long ? (Long) principal : null;

    }
}
