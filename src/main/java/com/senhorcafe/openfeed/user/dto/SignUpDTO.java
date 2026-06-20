package com.senhorcafe.openfeed.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignUpDTO(
   @NotBlank @Email String email,
   @NotBlank @Size(min = 8, max = 25) String password,
   @Size(min = 5, max = 25) String username
) {}
