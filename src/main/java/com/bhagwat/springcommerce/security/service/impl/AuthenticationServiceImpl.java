package com.bhagwat.springcommerce.security.service.impl;

import com.bhagwat.springcommerce.security.dto.LoginRequest;
import com.bhagwat.springcommerce.security.dto.LoginResponse;
import com.bhagwat.springcommerce.security.security.JwtService;
import com.bhagwat.springcommerce.security.service.AuthenticationService;
import com.bhagwat.springcommerce.common.exception.InvalidCredentialsException;
import com.bhagwat.springcommerce.user.entity.User;
import com.bhagwat.springcommerce.user.mapper.UserMapper;
import com.bhagwat.springcommerce.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private final JwtService jwtService;

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final UserMapper userMapper;

    public AuthenticationServiceImpl(JwtService jwtService, UserRepository userRepository, PasswordEncoder passwordEncoder, UserMapper userMapper) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(
                request.password(), user.getPassword()))
        {
            throw new InvalidCredentialsException();
        }

        String token = jwtService.generateToken(user);

        System.out.println("Generated JWT: " + token);

        return new LoginResponse(
                token,
                userMapper.toResponse(user)
        );
    }
}
