package com.bhagwat.springcommerce.user.service.impl;

import com.bhagwat.springcommerce.common.exception.UserAlreadyExistsException;
import com.bhagwat.springcommerce.user.dto.RegisterRequest;
import com.bhagwat.springcommerce.user.dto.UserResponse;
import com.bhagwat.springcommerce.user.entity.Role;
import com.bhagwat.springcommerce.user.entity.User;
import com.bhagwat.springcommerce.user.mapper.UserMapper;
import com.bhagwat.springcommerce.user.repository.UserRepository;
import com.bhagwat.springcommerce.user.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final UserMapper userMapper;

    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new UserAlreadyExistsException(request.email());
        }

        User user = userMapper.toEntity(request);

        // Security-related changes
        user.setRole(Role.CUSTOMER);
        user.setPassword(passwordEncoder.encode(request.password()));

        // Save
        userRepository.save(user);

        User savedUser = userRepository.save(user);

        return userMapper.toResponse(savedUser);
    }
}
