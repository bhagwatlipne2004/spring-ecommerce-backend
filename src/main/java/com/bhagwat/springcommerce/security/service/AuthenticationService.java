package com.bhagwat.springcommerce.security.service;

import com.bhagwat.springcommerce.security.dto.LoginRequest;
import com.bhagwat.springcommerce.security.dto.LoginResponse;

public interface AuthenticationService {

    LoginResponse login(LoginRequest request);
}
