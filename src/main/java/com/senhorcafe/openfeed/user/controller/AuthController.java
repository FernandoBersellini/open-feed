package com.senhorcafe.openfeed.user.controller;

import com.senhorcafe.openfeed.config.JwtCookie;
import com.senhorcafe.openfeed.user.dto.SignInDTO;
import com.senhorcafe.openfeed.user.dto.SignUpDTO;
import com.senhorcafe.openfeed.user.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    @PostMapping("entrar")
    public ResponseEntity<?> signIn(@Valid @RequestBody SignInDTO signInDTO, CsrfToken csrfToken) {
        return authService.signIn(signInDTO, csrfToken);
    }

    @PostMapping("criar-conta")
    public ResponseEntity<?> signUp(@Valid @RequestBody SignUpDTO signUpDTO, CsrfToken csrfToken) {
        return authService.signUp(signUpDTO, csrfToken);
    }

    @PostMapping("sair")
    public ResponseEntity<String> signOut(@CookieValue(JwtCookie.NAME) String token) {
        return authService.signOut(token);
    }

    @GetMapping("me")
    public ResponseEntity<?> getCurrentUser(CsrfToken csrfToken) {
        return authService.retrieveUser(csrfToken);
    }
}
