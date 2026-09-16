package com.bhagwat.springcommerce.security.dto;

import com.bhagwat.springcommerce.user.dto.UserResponse;

public record LoginResponse(

        String token,

        UserResponse user
) {
}
