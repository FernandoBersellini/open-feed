package com.senhorcafe.openfeed.comment.controller;

import com.senhorcafe.openfeed.comment.dto.ComentarioDTO;
import com.senhorcafe.openfeed.comment.dto.CriarComentarioDTO;
import com.senhorcafe.openfeed.comment.dto.EditarComentarioDTO;
import com.senhorcafe.openfeed.comment.service.CommentService;
import com.senhorcafe.openfeed.like.LikeDTO;
import com.senhorcafe.openfeed.like.LikeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("comentarios/")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;
    private final LikeService likeService;

    @GetMapping("retornar-comentarios/{postId}")
    public ResponseEntity<List<ComentarioDTO>> index(@PathVariable Long postId) {
        return commentService.commentIndex(postId);
    }

    @PostMapping("criar-comentario/{postId}")
    public ResponseEntity<String> post(@PathVariable Long postId, @Valid @RequestBody CriarComentarioDTO criarComentarioDTO) {
        return commentService.createComment(postId, criarComentarioDTO);
    }

    @PostMapping("interagir-com-comentario/{commentId}")
    public ResponseEntity<LikeDTO> toggleLike(@PathVariable Long commentId) {
        return likeService.toggleCommentLike(commentId);
    }

    @PatchMapping("editar-comentario/{commentId}")
    public ResponseEntity<String> patch(@PathVariable Long commentId, @Valid @RequestBody EditarComentarioDTO editarComentarioDTO) {
        return commentService.updateComment(commentId, editarComentarioDTO);
    }

    @DeleteMapping("deletar-comentario/{commentId}")
    public ResponseEntity<String> delete(@PathVariable Long commentId) {
        return commentService.deleteComment(commentId);
    }
}
