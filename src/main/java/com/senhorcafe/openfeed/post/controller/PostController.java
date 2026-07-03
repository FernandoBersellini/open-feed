package com.senhorcafe.openfeed.post.controller;

import com.senhorcafe.openfeed.like.LikeDTO;
import com.senhorcafe.openfeed.like.LikeService;
import com.senhorcafe.openfeed.post.dto.AtualizarPostDTO;
import com.senhorcafe.openfeed.post.dto.CriarPostDTO;
import com.senhorcafe.openfeed.post.dto.PostDTO;
import com.senhorcafe.openfeed.post.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("posts/")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;
    private final LikeService likeService;

    @GetMapping("retornar-postagens/{id}")
    public ResponseEntity<List<PostDTO>> index(@PathVariable Long id) {
        return postService.postIndex(id);
    }

    @PostMapping("criar-postagem")
    public ResponseEntity<String> post(@Valid @RequestBody CriarPostDTO criarPostDTO) {
        return postService.createPost(criarPostDTO);
    }

    @PostMapping("interagir-com-postagem/{id}")
    public ResponseEntity<LikeDTO> togglePostLike(@PathVariable Long id) {
        return likeService.togglePostLike(id);
    }

    @PatchMapping("atualizar-postagem/{id}")
    public ResponseEntity<String> patch(@Valid @RequestBody AtualizarPostDTO atualizarPostDTO, @PathVariable Long id) {
        return postService.updatePost(id, atualizarPostDTO);
    }

    @DeleteMapping("deletar-postagem/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        return postService.deletePost(id);
    }
}
