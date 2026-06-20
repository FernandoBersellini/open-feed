package com.senhorcafe.openfeed.post.repository;

import com.senhorcafe.openfeed.post.entity.Post;
import com.senhorcafe.openfeed.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findAllByUserId(Long userId);
}
