package com.senhorcafe.openfeed.comment.service;

import com.senhorcafe.openfeed.comment.dto.ComentarioDTO;
import com.senhorcafe.openfeed.comment.dto.CriarComentarioDTO;
import com.senhorcafe.openfeed.comment.dto.EditarComentarioDTO;
import com.senhorcafe.openfeed.comment.entity.Comment;
import com.senhorcafe.openfeed.comment.repository.CommentRepository;
import com.senhorcafe.openfeed.post.dto.AtualizarPostDTO;
import com.senhorcafe.openfeed.post.entity.Post;
import com.senhorcafe.openfeed.post.repository.PostRepository;
import com.senhorcafe.openfeed.user.entity.User;
import com.senhorcafe.openfeed.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public ResponseEntity<List<ComentarioDTO>> postIndex(Long id) {
        List<Comment> commentsFromDB = commentRepository.findByPostId(id);

        List<ComentarioDTO> comentarios = commentsFromDB.stream()
                .map(comment -> new ComentarioDTO(
                        comment.getId(),
                        comment.getConteudo(),
                        comment.getDataComentario(),
                        comment.getUser().getId(),
                        comment.getUser().getUsername()
                )).toList();

        return ResponseEntity.ok(comentarios);
    }

    public ResponseEntity<String> createComment(Long id, CriarComentarioDTO criarComentarioDTO) {
        Optional<Post> postOptional = postRepository.findById(id);
        Optional<User> userOptional = userRepository.findById(criarComentarioDTO.idUsuario());

        if (postOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Post nao encontrado");
        } else if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario nao encontrado");
        }

        Comment comment = new Comment();
        comment.setConteudo(criarComentarioDTO.conteudo());
        comment.setPost(postOptional.get());
        comment.setUser(userOptional.get());

        commentRepository.save(comment);

        return ResponseEntity.status(HttpStatus.CREATED).body("Comentario criado com sucesso");
    }

    public ResponseEntity<String> updateComment(Long commentId, Long idUsuario , EditarComentarioDTO editarComentarioDTO) {
        Optional<Comment> commentOptional = commentRepository.findById(commentId);

        if (commentOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Comentario nao encontrado");
        }

        if (!commentOptional.get().getUser().getId().equals(idUsuario)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Este comentario nao pertence ao usuario informado");
        }

        Comment comment = commentOptional.get();

        if (editarComentarioDTO.conteudo() != null) {
            comment.setConteudo(editarComentarioDTO.conteudo());
        }

        commentRepository.save(comment);

        return ResponseEntity.status(HttpStatus.OK).body("Comentario atualizado com sucesso");
    }

    public ResponseEntity<String> deleteComment(Long commentId, Long idUsuario) {
        Optional<Comment> commentOptional = commentRepository.findById(commentId);

        if (commentOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Comentario nao encontrado");
        }

        if (!commentOptional.get().getUser().getId().equals(idUsuario)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Este comentario nao pertence ao usuario informado");
        }

        commentRepository.deleteById(commentId);
        return ResponseEntity.status(HttpStatus.OK).body("Comentario deletado com sucesso");
    }

}
