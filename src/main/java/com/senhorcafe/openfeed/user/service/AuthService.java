package com.senhorcafe.openfeed.user.service;

import com.senhorcafe.openfeed.config.JwtCookie;
import com.senhorcafe.openfeed.config.JwtService;
import com.senhorcafe.openfeed.config.TokenDenylist;
import com.senhorcafe.openfeed.user.dto.AuthResponseDTO;
import com.senhorcafe.openfeed.user.dto.SignInDTO;
import com.senhorcafe.openfeed.user.dto.SignUpDTO;
import com.senhorcafe.openfeed.user.entity.User;
import com.senhorcafe.openfeed.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TokenDenylist tokenDenylist;
    private final JwtCookie jwtCookie;

    public ResponseEntity<?> signUp(SignUpDTO signUpDTO) {
        if (userRepository.existsByEmail(signUpDTO.email())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email ja cadastrado");
        }

        if (signUpDTO.username() != null && userRepository.existsByUsername(signUpDTO.username())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Username ja cadastrado");
        }

        User user = new User();
        user.setEmail(signUpDTO.email());
        user.setPassword(passwordEncoder.encode(signUpDTO.password()));

        if (signUpDTO.username() != null) {
            user.setUsername(signUpDTO.username());
        }

        userRepository.save(user);

        String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .header(HttpHeaders.SET_COOKIE, jwtCookie.build(token).toString())
                .body(new AuthResponseDTO(user.getId(), user.getEmail(), user.getUsername()));
    }

    public ResponseEntity<?> signIn(SignInDTO signInDTO) {
        User user = userRepository.findByEmail(signInDTO.email());

        if (user == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email ou senha invalidos");
        }

        if (!passwordEncoder.matches(signInDTO.password(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email ou senha invalidos");
        }

        String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getUsername());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.build(token).toString())
                .body(new AuthResponseDTO(user.getId(), user.getEmail(), user.getUsername()));
    }

    public ResponseEntity<String> signOut(String token) {
        tokenDenylist.revoke(jwtService.extractTokenId(token), jwtService.extractExpiration(token).toInstant());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.clear().toString())
                .body("Logout realizado com sucesso");
    }

    public ResponseEntity<?> retrieveUser() {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<User> user = userRepository.findById(userId);

        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario nao encontrado");
        }

        User u = user.get();
        return ResponseEntity.ok(new AuthResponseDTO(u.getId(), u.getEmail(), u.getUsername()));
    }
}
