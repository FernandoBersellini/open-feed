package com.senhorcafe.openfeed.post;

import com.senhorcafe.openfeed.like.post_like.PostLikeRepository;
import com.senhorcafe.openfeed.post.dto.AtualizarPostDTO;
import com.senhorcafe.openfeed.post.dto.CriarPostDTO;
import com.senhorcafe.openfeed.post.entity.Post;
import com.senhorcafe.openfeed.post.repository.PostRepository;
import com.senhorcafe.openfeed.post.service.PostService;
import com.senhorcafe.openfeed.post.tags.TagInvalidaException;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatStream;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PostServiceTest {
    private static final Long USER_ID = 1L;
    private static final Long POST_ID = 10L;

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PostLikeRepository postLikeRepository;

    @InjectMocks
    private PostService postService;

    private MockedStatic<SecurityContextHolder> securityContextHolderMock;

    @BeforeEach
    void mockAuthenticatedUser() {
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
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
    void sanitizeTag_comTagInvalida_RetornaErro() {
        assertThatThrownBy(() -> postService.sanitizeTag("banana"))
            .isInstanceOf(TagInvalidaException.class)
            .hasMessage("Tag invalida");
    }

    @Test
    void createPost_usuarioNaoEncontrado_retorna404() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        CriarPostDTO criarPostDTO = new CriarPostDTO("titulo valido", "conteudo valido", "LAZER");
        ResponseEntity<String> response = postService.createPost(criarPostDTO);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isEqualTo("Usuario nao encontrado");
        verifyNoInteractions(postRepository);
    }

    @Test
    void createPost_comDadosValidos_salvaPostERetorna201() {
        User user = new User();
        user.setId(USER_ID);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        CriarPostDTO criarPostDTO = new CriarPostDTO("titulo valido", "conteudo valido", "lazer");
        ResponseEntity<String> response = postService.createPost(criarPostDTO);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo("Post salvo com sucesso");

        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(postCaptor.capture());

        Post savedPost = postCaptor.getValue();
        assertThat(savedPost.getTitulo()).isEqualTo("titulo valido");
        assertThat(savedPost.getConteudo()).isEqualTo("conteudo valido");
        assertThat(savedPost.getTag()).isEqualTo("LAZER");
        assertThat(savedPost.getUser()).isEqualTo(user);
    }

    @Test
    void createPost_comTagInvalida_lancaExcecaoENaoSalva() {
        User user = new User();
        user.setId(USER_ID);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        CriarPostDTO criarPostDTO = new CriarPostDTO("titulo valido", "conteudo valido", "banana");

        assertThatThrownBy(() -> postService.createPost(criarPostDTO))
            .isInstanceOf(TagInvalidaException.class);

        verify(postRepository, never()).save(any());
    }

    @Test
    void updatePost_postNaoEncontrado_retorna404() {
        when(postRepository.findById(POST_ID)).thenReturn(Optional.empty());

        AtualizarPostDTO atualizarPostDTO = new AtualizarPostDTO("novo titulo", null, null);
        ResponseEntity<String> response = postService.updatePost(POST_ID, atualizarPostDTO);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(postRepository, never()).save(any());
    }

    @Test
    void updatePost_postNaoPertenceAoUsuario_retorna403() {
        User dono = new User();
        dono.setId(2L);

        Post post = new Post();
        post.setId(POST_ID);
        post.setUser(dono);

        when(postRepository.findById(POST_ID)).thenReturn(Optional.of(post));

        AtualizarPostDTO atualizarPostDTO = new AtualizarPostDTO("novo titulo", null, null);
        ResponseEntity<String> response = postService.updatePost(POST_ID, atualizarPostDTO);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isEqualTo("Este post nao pertence ao usuario informado");
        verify(postRepository, never()).save(any());
    }

    @Test
    void updatePost_comTodosOsCampos_atualizaTituloConteudoETag() {
        User dono = new User();
        dono.setId(USER_ID);

        Post post = new Post();
        post.setId(POST_ID);
        post.setUser(dono);
        post.setTitulo("titulo antigo");
        post.setConteudo("conteudo antigo");
        post.setTag("COMIDA");

        when(postRepository.findById(POST_ID)).thenReturn(Optional.of(post));

        AtualizarPostDTO atualizarPostDTO = new AtualizarPostDTO("titulo novo", "conteudo novo", "lazer");
        ResponseEntity<String> response = postService.updatePost(POST_ID, atualizarPostDTO);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("Post atualizado");

        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(postCaptor.capture());

        Post savedPost = postCaptor.getValue();
        assertThat(savedPost.getTitulo()).isEqualTo("titulo novo");
        assertThat(savedPost.getConteudo()).isEqualTo("conteudo novo");
        assertThat(savedPost.getTag()).isEqualTo("LAZER");
    }

    @Test
    void updatePost_comCamposNulos_mantemValoresExistentes() {
        User dono = new User();
        dono.setId(USER_ID);

        Post post = new Post();
        post.setId(POST_ID);
        post.setUser(dono);
        post.setTitulo("titulo original");
        post.setConteudo("conteudo original");
        post.setTag("COMIDA");

        when(postRepository.findById(POST_ID)).thenReturn(Optional.of(post));

        AtualizarPostDTO atualizarPostDTO = new AtualizarPostDTO(null, null, null);
        ResponseEntity<String> response = postService.updatePost(POST_ID, atualizarPostDTO);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(postCaptor.capture());

        Post savedPost = postCaptor.getValue();
        assertThat(savedPost.getTitulo()).isEqualTo("titulo original");
        assertThat(savedPost.getConteudo()).isEqualTo("conteudo original");
        assertThat(savedPost.getTag()).isEqualTo("COMIDA");
    }

    @Test
    void updatePost_comTagInvalida_lancaExcecaoENaoSalva() {
        User dono = new User();
        dono.setId(USER_ID);

        Post post = new Post();
        post.setId(POST_ID);
        post.setUser(dono);

        when(postRepository.findById(POST_ID)).thenReturn(Optional.of(post));

        AtualizarPostDTO atualizarPostDTO = new AtualizarPostDTO(null, null, "banana");

        assertThatThrownBy(() -> postService.updatePost(POST_ID, atualizarPostDTO))
            .isInstanceOf(TagInvalidaException.class);

        verify(postRepository, never()).save(any());
    }

    @Test
    void deletePost_postNaoEncontrado_retorna404() {
        when(postRepository.findById(POST_ID)).thenReturn(Optional.empty());

        ResponseEntity<String> response = postService.deletePost(POST_ID);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(postRepository, never()).deleteById(any());
    }

    @Test
    void deletePost_postNaoPertenceAoUsuario_retorna403() {
        User dono = new User();
        dono.setId(2L);

        Post post = new Post();
        post.setId(POST_ID);
        post.setUser(dono);

        when(postRepository.findById(POST_ID)).thenReturn(Optional.of(post));

        ResponseEntity<String> response = postService.deletePost(POST_ID);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isEqualTo("Este post nao pertence ao usuario informado");
        verify(postRepository, never()).deleteById(any());
    }

    @Test
    void deletePost_comDadosValidos_deletaERetorna200() {
        User dono = new User();
        dono.setId(USER_ID);

        Post post = new Post();
        post.setId(POST_ID);
        post.setUser(dono);

        when(postRepository.findById(POST_ID)).thenReturn(Optional.of(post));

        ResponseEntity<String> response = postService.deletePost(POST_ID);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("Post deletado");
        verify(postRepository).deleteById(POST_ID);
    }
}
