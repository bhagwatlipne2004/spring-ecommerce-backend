package com.bhagwat.springcommerce.user.service;

import com.bhagwat.springcommerce.user.dto.RegisterRequest;
import com.bhagwat.springcommerce.user.dto.UserResponse;

public interface UserService {

    UserResponse register(RegisterRequest request);
}
