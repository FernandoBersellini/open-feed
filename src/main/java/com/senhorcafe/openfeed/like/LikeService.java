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
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LikeService {
    private final PostLikeRepository postLikeRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    public ResponseEntity<LikeDTO> togglePostLike(Long postId) {
        Long userId = getAuthenticatedUserId();
        Optional<Post> postOptional = postRepository.findById(postId);

        if (postOptional.isEmpty()) {

            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Optional<PostLike> postLikeOptional = postLikeRepository.findByUserIdAndPostId(userId, postId);
        boolean usuarioAtualCurtiu;

        if (postLikeOptional.isPresent()) {
            postLikeRepository.delete(postLikeOptional.get());
            usuarioAtualCurtiu = false;
        } else {
            Optional<User> userOptional = userRepository.findById(userId);

            if (userOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            PostLike postLike = new PostLike();
            postLike.setUser(userOptional.get());
            postLike.setPost(postOptional.get());

            try {
                postLikeRepository.save(postLike);
            } catch (DataIntegrityViolationException e) {
            }

            usuarioAtualCurtiu = true;
        }

        long totalLikes = postLikeRepository.countByPostId(postId);

        return ResponseEntity.ok(new LikeDTO(totalLikes, usuarioAtualCurtiu));
    }

    public ResponseEntity<LikeDTO> toggleCommentLike(Long commentId) {
        Long userId = getAuthenticatedUserId();
        Optional<Comment> commentOptional = commentRepository.findById(commentId);

        if (commentOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Optional<CommentLike> commentLikeOptional = commentLikeRepository.findByUserIdAndCommentId(userId, commentId);
        boolean usuarioAtualCurtiu;

        if (commentLikeOptional.isPresent()) {
            commentLikeRepository.delete(commentLikeOptional.get());
            usuarioAtualCurtiu = false;
        } else {
            Optional<User> userOptional = userRepository.findById(userId);

            if (userOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            CommentLike commentLike = new CommentLike();
            commentLike.setUser(userOptional.get());
            commentLike.setComment(commentOptional.get());

            try {
                commentLikeRepository.save(commentLike);
            } catch (DataIntegrityViolationException e) {

            }

            usuarioAtualCurtiu = true;
        }

        long totalLikes = commentLikeRepository.countByCommentId(commentId);

        return ResponseEntity.ok(new LikeDTO(totalLikes, usuarioAtualCurtiu));
    }

    private Long getAuthenticatedUserId() {
        return (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
