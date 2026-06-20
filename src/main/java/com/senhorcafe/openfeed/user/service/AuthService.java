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
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public ResponseEntity<String> signUp(SignUpDTO signUpDTO) {
        User user = new User();
        user.setEmail(signUpDTO.email());

        String encodedPassword = passwordEncoder.encode(signUpDTO.password());
        user.setPassword(encodedPassword);

        if (signUpDTO.username() != null) {
            user.setUsername(signUpDTO.username());
        }

        userRepository.save(user);

        return ResponseEntity.status(HttpStatus.CREATED).body("Conta criada com sucesso");
    }

    public ResponseEntity<?> signIn(SignInDTO signInDTO) {
        User user = userRepository.findByEmail(signInDTO.email());

        if (user == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email ou senha invalidos");
        }

        if(passwordEncoder.matches(signInDTO.password(), user.getPassword())) {
            UserDTO userInfo = new UserDTO(user.getId(), user.getEmail(), user.getUsername());

            return ResponseEntity.status(HttpStatus.OK).body(userInfo);

        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email ou senha invalidos");
        }
    }
}
