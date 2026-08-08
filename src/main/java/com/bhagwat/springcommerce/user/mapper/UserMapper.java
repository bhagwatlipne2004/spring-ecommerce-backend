package com.bhagwat.springcommerce.user.mapper;

import com.bhagwat.springcommerce.user.dto.RegisterRequest;
import com.bhagwat.springcommerce.user.dto.UserResponse;
import com.bhagwat.springcommerce.user.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toEntity(RegisterRequest request){
         User user = new User();
         user.setFirstName(request.firstName());
         user.setLastName(request.lastName());
         user.setEmail(request.email());
         user.setPassword(request.password());

         return user;
    }

    public UserResponse toResponse(User user) {

        return new UserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getRole()
        );
    }
}
