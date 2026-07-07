package com.senhorcafe.openfeed.like;

import com.senhorcafe.openfeed.comment.entity.Comment;
import com.senhorcafe.openfeed.comment.repository.CommentRepository;
import com.senhorcafe.openfeed.like.comment_like.CommentLike;
import com.senhorcafe.openfeed.like.comment_like.CommentLikeRepository;
import com.senhorcafe.openfeed.like.post_like.PostLike;
import com.senhorcafe.openfeed.like.post_like.PostLikeRepository;
import com.senhorcafe.openfeed.post.entity.Post;
import com.senhorcafe.openfeed.post.repository.PostRepository;
import com.senhorcafe.openfeed.user.entity.User;
import com.senhorcafe.openfeed.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LikeServiceTest {

    private static final Long USER_ID = 1L;
    private static final Long POST_ID = 10L;
    private static final Long COMMENT_ID = 20L;

    @Mock
    private PostLikeRepository postLikeRepository;
    @Mock
    private CommentLikeRepository commentLikeRepository;
    @Mock
    private PostRepository postRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private LikeService likeService;

    private MockedStatic<SecurityContextHolder> securityContextHolderMock;

    @BeforeEach
    void mockAuthenticatedUser() {
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(USER_ID);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        securityContextHolderMock = mockStatic(SecurityContextHolder.class);
        securityContextHolderMock.when(SecurityContextHolder::getContext).thenReturn(securityContext);
    }

    @AfterEach
    void closeSecurityContextMock() {
        securityContextHolderMock.close();
    }

    @Test
    void togglePostLike_postNaoExiste_retorna404() {
        when(postRepository.findById(POST_ID)).thenReturn(Optional.empty());

        ResponseEntity<LikeDTO> response = likeService.togglePostLike(POST_ID);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verifyNoInteractions(postLikeRepository);
    }

    @Test
    void togglePostLike_semLikeExistente_criaLikeERetornaCurtido() {
        Post post = new Post();
        post.setId(POST_ID);
        User user = new User();
        user.setId(USER_ID);

        when(postRepository.findById(POST_ID)).thenReturn(Optional.of(post));
        when(postLikeRepository.findByUserIdAndPostId(USER_ID, POST_ID)).thenReturn(Optional.empty());
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(postLikeRepository.countByPostId(POST_ID)).thenReturn(1L);

        ResponseEntity<LikeDTO> response = likeService.togglePostLike(POST_ID);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().totalLikes()).isEqualTo(1L);
        assertThat(response.getBody().usuarioAtualCurtiu()).isTrue();
        verify(postLikeRepository).save(any(PostLike.class));
        verify(postLikeRepository, never()).delete(any());
    }

    @Test
    void togglePostLike_comLikeExistente_removeLikeERetornaDescurtido() {
        Post post = new Post();
        post.setId(POST_ID);
        PostLike existingLike = new PostLike();

        when(postRepository.findById(POST_ID)).thenReturn(Optional.of(post));
        when(postLikeRepository.findByUserIdAndPostId(USER_ID, POST_ID)).thenReturn(Optional.of(existingLike));
        when(postLikeRepository.countByPostId(POST_ID)).thenReturn(0L);

        ResponseEntity<LikeDTO> response = likeService.togglePostLike(POST_ID);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().totalLikes()).isEqualTo(0L);
        assertThat(response.getBody().usuarioAtualCurtiu()).isFalse();
        verify(postLikeRepository).delete(existingLike);
        verify(postLikeRepository, never()).save(any());
    }

    @Test
    void togglePostLike_usuarioNaoExiste_retorna404() {
        Post post = new Post();
        post.setId(POST_ID);

        when(postRepository.findById(POST_ID)).thenReturn(Optional.of(post));
        when(postLikeRepository.findByUserIdAndPostId(USER_ID, POST_ID)).thenReturn(Optional.empty());
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        ResponseEntity<LikeDTO> response = likeService.togglePostLike(POST_ID);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(postLikeRepository, never()).save(any());
    }

    @Test
    void togglePostLike_curtidaConcorrenteDuplicada_naoLancaExcecao() {
        Post post = new Post();
        post.setId(POST_ID);
        User user = new User();
        user.setId(USER_ID);

        when(postRepository.findById(POST_ID)).thenReturn(Optional.of(post));
        when(postLikeRepository.findByUserIdAndPostId(USER_ID, POST_ID)).thenReturn(Optional.empty());
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(postLikeRepository.save(any(PostLike.class))).thenThrow(new DataIntegrityViolationException("duplicado"));
        when(postLikeRepository.countByPostId(POST_ID)).thenReturn(1L);

        ResponseEntity<LikeDTO> response = likeService.togglePostLike(POST_ID);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().usuarioAtualCurtiu()).isTrue();
    }

    @Test
    void toggleCommentLike_commentNaoExiste_retorna404() {
        when(commentRepository.findById(COMMENT_ID)).thenReturn(Optional.empty());

        ResponseEntity<LikeDTO> response = likeService.toggleCommentLike(COMMENT_ID);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verifyNoInteractions(commentLikeRepository);
    }

    @Test
    void toggleCommentLike_semLikeExistente_criaLikeERetornaCurtido() {
        Comment comment = new Comment();
        comment.setId(COMMENT_ID);
        User user = new User();
        user.setId(USER_ID);

        when(commentRepository.findById(COMMENT_ID)).thenReturn(Optional.of(comment));
        when(commentLikeRepository.findByUserIdAndCommentId(USER_ID, COMMENT_ID)).thenReturn(Optional.empty());
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(commentLikeRepository.countByCommentId(COMMENT_ID)).thenReturn(1L);

        ResponseEntity<LikeDTO> response = likeService.toggleCommentLike(COMMENT_ID);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().totalLikes()).isEqualTo(1L);
        assertThat(response.getBody().usuarioAtualCurtiu()).isTrue();
        verify(commentLikeRepository).save(any(CommentLike.class));
    }

    @Test
    void toggleCommentLike_comLikeExistente_removeLikeERetornaDescurtido() {
        Comment comment = new Comment();
        comment.setId(COMMENT_ID);
        CommentLike existingLike = new CommentLike();

        when(commentRepository.findById(COMMENT_ID)).thenReturn(Optional.of(comment));
        when(commentLikeRepository.findByUserIdAndCommentId(USER_ID, COMMENT_ID)).thenReturn(Optional.of(existingLike));
        when(commentLikeRepository.countByCommentId(COMMENT_ID)).thenReturn(0L);

        ResponseEntity<LikeDTO> response = likeService.toggleCommentLike(COMMENT_ID);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().usuarioAtualCurtiu()).isFalse();
        verify(commentLikeRepository).delete(existingLike);
    }
}
