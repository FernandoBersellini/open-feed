package com.senhorcafe.openfeed.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SignInDTO(
   @NotBlank @Email String email,
   @NotBlank String password
) {}
