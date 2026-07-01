package com.senhorcafe.openfeed.user.controller;

import com.senhorcafe.openfeed.user.dto.SignInDTO;
import com.senhorcafe.openfeed.user.dto.SignUpDTO;
import com.senhorcafe.openfeed.user.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    @PostMapping("entrar")
    public ResponseEntity<?> signIn(@RequestBody SignInDTO signInDTO) {
        return authService.signIn(signInDTO);
    }

    @PostMapping("criar-conta")
    public ResponseEntity<?> signUp(@RequestBody SignUpDTO signUpDTO) {
        return authService.signUp(signUpDTO);
    }
}
