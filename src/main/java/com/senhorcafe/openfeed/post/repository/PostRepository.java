package com.senhorcafe.openfeed.post.repository;

import com.senhorcafe.openfeed.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
}
