package com.senhorcafe.openfeed.user.service;

import com.senhorcafe.openfeed.user.dto.SignInDTO;
import com.senhorcafe.openfeed.user.dto.SignUpDTO;
import com.senhorcafe.openfeed.user.dto.UserDTO;
import com.senhorcafe.openfeed.user.entity.User;
import com.senhorcafe.openfeed.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
}
