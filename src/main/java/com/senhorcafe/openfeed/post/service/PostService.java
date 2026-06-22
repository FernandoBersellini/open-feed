package com.senhorcafe.openfeed.post.service;

import com.senhorcafe.openfeed.post.dto.AtualizarPostDTO;
import com.senhorcafe.openfeed.post.dto.CriarPostDTO;
import com.senhorcafe.openfeed.post.dto.PostDTO;
import com.senhorcafe.openfeed.post.entity.Post;
import com.senhorcafe.openfeed.post.repository.PostRepository;
import com.senhorcafe.openfeed.post.tags.Tags;
import com.senhorcafe.openfeed.user.entity.User;
import com.senhorcafe.openfeed.user.repository.UserRepository;
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
    private final UserRepository userRepository;

    public ResponseEntity<List<PostDTO>> postIndex(Long userId) {
        List<Post> postsFromBackend = postRepository.findAllByUserId(userId);

        List<PostDTO> posts = postsFromBackend.stream()
            .map(post -> new PostDTO(
                post.getId(),
                post.getTitulo(),
                post.getConteudo(),
                post.getTag(),
                post.getDataPostagem().toString()
            ))
            .toList();
        
        return ResponseEntity.ok(posts);
    }

    public ResponseEntity<String> createPost(CriarPostDTO criarPostDTO) {
        Optional<User> userOptional = userRepository.findById(criarPostDTO.idUsuario());

        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario nao encontrado");
        }

        Post post = new Post();
        post.setTitulo(criarPostDTO.titulo());
        post.setConteudo(criarPostDTO.conteudo());
        post.setUser(userOptional.get());

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

        if (!post.getUser().getId().equals(atualizarPostDTO.idUsuario())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Este post nao pertence ao usuario informado");
        }

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

    public ResponseEntity<String> deletePost(Long postId, Long idUsuario) {
        Optional<Post> postOptional = postRepository.findById(postId);

        if (postOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        if (!postOptional.get().getUser().getId().equals(idUsuario)) {
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
            return "Tag invalida";
        }
    }
}
