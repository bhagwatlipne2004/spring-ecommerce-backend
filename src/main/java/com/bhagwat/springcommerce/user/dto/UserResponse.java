package com.bhagwat.springcommerce.user.dto;

import com.bhagwat.springcommerce.user.entity.Role;

public record UserResponse(

        Long id,

        String firstName,

        String lastName,

        String email,

        Role role
) {
}
