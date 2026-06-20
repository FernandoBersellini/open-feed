package com.senhorcafe.openfeed.user.repository;

import com.senhorcafe.openfeed.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);
}
