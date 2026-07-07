package com.senhorcafe.openfeed.comment;

import com.senhorcafe.openfeed.comment.dto.ComentarioDTO;
import com.senhorcafe.openfeed.comment.dto.CriarComentarioDTO;
import com.senhorcafe.openfeed.comment.dto.EditarComentarioDTO;
import com.senhorcafe.openfeed.comment.entity.Comment;
import com.senhorcafe.openfeed.comment.repository.CommentRepository;
import com.senhorcafe.openfeed.comment.service.CommentService;
import com.senhorcafe.openfeed.like.comment_like.CommentLike;
import com.senhorcafe.openfeed.like.comment_like.CommentLikeRepository;
import com.senhorcafe.openfeed.post.entity.Post;
import com.senhorcafe.openfeed.post.repository.PostRepository;
import com.senhorcafe.openfeed.user.entity.User;
import com.senhorcafe.openfeed.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {
    private static final Long USER_ID = 1L;
    private static final Long POST_ID = 10L;
    private static final Long COMMENT_ID = 20L;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CommentLikeRepository commentLikeRepository;

    @InjectMocks
    private CommentService commentService;

    private Authentication authentication;
    private MockedStatic<SecurityContextHolder> securityContextHolderMock;

    @BeforeEach
    void mockAuthenticatedUser() {
        SecurityContext securityContext = mock(SecurityContext.class);
        authentication = mock(Authentication.class);
        lenient().when(authentication.getPrincipal()).thenReturn(USER_ID);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);

        securityContextHolderMock = mockStatic(SecurityContextHolder.class);
        securityContextHolderMock.when(SecurityContextHolder::getContext).thenReturn(securityContext);
    }

    @AfterEach
    void closeSecurityContextMock() {
        securityContextHolderMock.close();
    }

    @Test
    void commentIndex_usuarioAutenticadoCurtiu_retornaTotalLikesEUsuarioAtualCurtiuTrue() {
        User autor = new User();
        autor.setId(USER_ID);
        autor.setUsername("autor");

        Comment comment = new Comment();
        comment.setId(COMMENT_ID);
        comment.setConteudo("comentario de teste");
        comment.setUser(autor);

        when(commentRepository.findByPostId(POST_ID)).thenReturn(List.of(comment));
        when(commentLikeRepository.countByCommentId(COMMENT_ID)).thenReturn(3L);
        when(commentLikeRepository.findByUserIdAndCommentId(USER_ID, COMMENT_ID)).thenReturn(Optional.of(new CommentLike()));

        ResponseEntity<List<ComentarioDTO>> response = commentService.commentIndex(POST_ID);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        ComentarioDTO dto = response.getBody().get(0);
        assertThat(dto.totalLikes()).isEqualTo(3L);
        assertThat(dto.usuarioAtualCurtiu()).isTrue();
    }

    @Test
    void commentIndex_usuarioNaoAutenticado_usuarioAtualCurtiuFalso() {
        when(authentication.getPrincipal()).thenReturn("anonymousUser");

        User autor = new User();
        autor.setId(2L);
        autor.setUsername("outro");

        Comment comment = new Comment();
        comment.setId(COMMENT_ID);
        comment.setConteudo("comentario de teste");
        comment.setUser(autor);

        when(commentRepository.findByPostId(POST_ID)).thenReturn(List.of(comment));
        when(commentLikeRepository.countByCommentId(COMMENT_ID)).thenReturn(0L);

        ResponseEntity<List<ComentarioDTO>> response = commentService.commentIndex(POST_ID);

        ComentarioDTO dto = response.getBody().get(0);
        assertThat(dto.usuarioAtualCurtiu()).isFalse();
        verify(commentLikeRepository, never()).findByUserIdAndCommentId(any(), any());
    }

    @Test
    void createComment_postNaoEncontrado_retorna404() {
        when(postRepository.findById(POST_ID)).thenReturn(Optional.empty());

        ResponseEntity<String> response = commentService.createComment(POST_ID, new CriarComentarioDTO("comentario valido"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isEqualTo("Post nao encontrado");
        verify(commentRepository, never()).save(any());
    }

    @Test
    void createComment_usuarioNaoEncontrado_retorna404() {
        Post post = new Post();
        post.setId(POST_ID);

        when(postRepository.findById(POST_ID)).thenReturn(Optional.of(post));
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        ResponseEntity<String> response = commentService.createComment(POST_ID, new CriarComentarioDTO("comentario valido"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isEqualTo("Usuario nao encontrado");
        verify(commentRepository, never()).save(any());
    }

    @Test
    void createComment_comDadosValidos_salvaComentarioERetorna201() {
        Post post = new Post();
        post.setId(POST_ID);

        User user = new User();
        user.setId(USER_ID);

        when(postRepository.findById(POST_ID)).thenReturn(Optional.of(post));
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        ResponseEntity<String> response = commentService.createComment(POST_ID, new CriarComentarioDTO("comentario valido"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo("Comentario criado com sucesso");

        ArgumentCaptor<Comment> commentCaptor = ArgumentCaptor.forClass(Comment.class);
        verify(commentRepository).save(commentCaptor.capture());

        Comment savedComment = commentCaptor.getValue();
        assertThat(savedComment.getConteudo()).isEqualTo("comentario valido");
        assertThat(savedComment.getPost()).isEqualTo(post);
        assertThat(savedComment.getUser()).isEqualTo(user);
    }

    @Test
    void updateComment_comentarioNaoEncontrado_retorna404() {
        when(commentRepository.findById(COMMENT_ID)).thenReturn(Optional.empty());

        ResponseEntity<String> response = commentService.updateComment(COMMENT_ID, new EditarComentarioDTO("novo conteudo"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isEqualTo("Comentario nao encontrado");
        verify(commentRepository, never()).save(any());
    }

    @Test
    void updateComment_comentarioNaoPertenceAoUsuario_retorna403() {
        User dono = new User();
        dono.setId(2L);

        Comment comment = new Comment();
        comment.setId(COMMENT_ID);
        comment.setUser(dono);

        when(commentRepository.findById(COMMENT_ID)).thenReturn(Optional.of(comment));

        ResponseEntity<String> response = commentService.updateComment(COMMENT_ID, new EditarComentarioDTO("novo conteudo"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isEqualTo("Este comentario nao pertence ao usuario informado");
        verify(commentRepository, never()).save(any());
    }

    @Test
    void updateComment_comConteudoInformado_atualizaConteudo() {
        User dono = new User();
        dono.setId(USER_ID);

        Comment comment = new Comment();
        comment.setId(COMMENT_ID);
        comment.setUser(dono);
        comment.setConteudo("conteudo antigo");

        when(commentRepository.findById(COMMENT_ID)).thenReturn(Optional.of(comment));

        ResponseEntity<String> response = commentService.updateComment(COMMENT_ID, new EditarComentarioDTO("conteudo novo"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("Comentario atualizado com sucesso");

        ArgumentCaptor<Comment> commentCaptor = ArgumentCaptor.forClass(Comment.class);
        verify(commentRepository).save(commentCaptor.capture());
        assertThat(commentCaptor.getValue().getConteudo()).isEqualTo("conteudo novo");
    }

    @Test
    void updateComment_semConteudo_mantemValorExistente() {
        User dono = new User();
        dono.setId(USER_ID);

        Comment comment = new Comment();
        comment.setId(COMMENT_ID);
        comment.setUser(dono);
        comment.setConteudo("conteudo original");

        when(commentRepository.findById(COMMENT_ID)).thenReturn(Optional.of(comment));

        ResponseEntity<String> response = commentService.updateComment(COMMENT_ID, new EditarComentarioDTO(null));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        ArgumentCaptor<Comment> commentCaptor = ArgumentCaptor.forClass(Comment.class);
        verify(commentRepository).save(commentCaptor.capture());
        assertThat(commentCaptor.getValue().getConteudo()).isEqualTo("conteudo original");
    }

    @Test
    void deleteComment_comentarioNaoEncontrado_retorna404() {
        when(commentRepository.findById(COMMENT_ID)).thenReturn(Optional.empty());

        ResponseEntity<String> response = commentService.deleteComment(COMMENT_ID);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isEqualTo("Comentario nao encontrado");
        verify(commentRepository, never()).deleteById(any());
    }

    @Test
    void deleteComment_comentarioNaoPertenceAoUsuario_retorna403() {
        User dono = new User();
        dono.setId(2L);

        Comment comment = new Comment();
        comment.setId(COMMENT_ID);
        comment.setUser(dono);

        when(commentRepository.findById(COMMENT_ID)).thenReturn(Optional.of(comment));

        ResponseEntity<String> response = commentService.deleteComment(COMMENT_ID);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isEqualTo("Este comentario nao pertence ao usuario informado");
        verify(commentRepository, never()).deleteById(any());
    }

    @Test
    void deleteComment_comDadosValidos_deletaERetorna200() {
        User dono = new User();
        dono.setId(USER_ID);

        Comment comment = new Comment();
        comment.setId(COMMENT_ID);
        comment.setUser(dono);

        when(commentRepository.findById(COMMENT_ID)).thenReturn(Optional.of(comment));

        ResponseEntity<String> response = commentService.deleteComment(COMMENT_ID);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("Comentario deletado com sucesso");
        verify(commentRepository).deleteById(COMMENT_ID);
    }
}
