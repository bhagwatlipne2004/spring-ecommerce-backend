package com.bhagwat.springcommerce.security.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(

        @NotBlank()
        String email,

        @NotBlank()
        @Size(min = 8, max = 15)
        String password
) {
}
