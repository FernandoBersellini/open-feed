package com.senhorcafe.openfeed.comment.controller;

import com.senhorcafe.openfeed.comment.dto.ComentarioDTO;
import com.senhorcafe.openfeed.comment.dto.CriarComentarioDTO;
import com.senhorcafe.openfeed.comment.dto.EditarComentarioDTO;
import com.senhorcafe.openfeed.comment.service.CommentService;
import com.senhorcafe.openfeed.post.dto.AtualizarPostDTO;
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

    @GetMapping("retornar-comentarios/{postId}")
    public ResponseEntity<List<ComentarioDTO>> index(@PathVariable Long postId) {
        return commentService.postIndex(postId);
    }

    @PostMapping("criar-comentario/{postId}")
    public ResponseEntity<String> post(@PathVariable Long postId, @Valid @RequestBody CriarComentarioDTO criarComentarioDTO) {
        return commentService.createComment(postId, criarComentarioDTO);
    }

    @PatchMapping("editar-comentario/{commentId}/")
    public ResponseEntity<String> patch(@PathVariable Long commentId, @RequestParam Long idUsuario, @Valid  @RequestBody EditarComentarioDTO editarComentarioDTO) {
        return commentService.updateComment(commentId, idUsuario, editarComentarioDTO);
    }

    @DeleteMapping("deletar-comentario/{commentId}/")
    public ResponseEntity<String> delete(@PathVariable Long commentId, @RequestParam Long idUsuario) {
        return commentService.deleteComment(commentId, idUsuario);
    }
}
