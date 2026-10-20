
package com.bhagwat.springcommerce.security.service.impl;

import com.bhagwat.springcommerce.common.exception.InvalidCredentialsException;
import com.bhagwat.springcommerce.security.dto.LoginRequest;
import com.bhagwat.springcommerce.security.dto.LoginResponse;
import com.bhagwat.springcommerce.security.security.JwtService;
import com.bhagwat.springcommerce.user.dto.UserResponse;
import com.bhagwat.springcommerce.user.entity.Role;
import com.bhagwat.springcommerce.user.entity.User;
import com.bhagwat.springcommerce.user.mapper.UserMapper;
import com.bhagwat.springcommerce.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    private LoginRequest request;
    private User user;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        request = new LoginRequest(
                "test@example.com",
                "StrongPassword123"
        );

        user = new User();
        user.setEmail("test@example.com");
        user.setPassword("encoded-password");

        userResponse = new UserResponse(
                1L,
                "Bhagwat",
                "Lipne",
                "test@example.com",
                Role.CUSTOMER
        );
    }

    @Test
    void login_shouldReturnTokenAndUser_whenCredentialsAreValid() {
        when(userRepository.findByEmail(request.email()))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches(
                request.password(), user.getPassword()))
                .thenReturn(true);
        when(jwtService.generateToken(user))
                .thenReturn("test-jwt-token");
        when(userMapper.toResponse(user))
                .thenReturn(userResponse);

        LoginResponse result = authenticationService.login(request);

        assertNotNull(result);
        assertEquals("test-jwt-token", result.token());
        assertEquals(userResponse, result.user());

        verify(userRepository).findByEmail(request.email());
        verify(passwordEncoder).matches(
                request.password(), user.getPassword());
        verify(jwtService).generateToken(user);
        verify(userMapper).toResponse(user);
    }

    @Test
    void login_shouldThrow_whenEmailDoesNotExist() {
        when(userRepository.findByEmail(request.email()))
                .thenReturn(Optional.empty());

        assertThrows(
                InvalidCredentialsException.class,
                () -> authenticationService.login(request)
        );

        verify(passwordEncoder, never())
                .matches(anyString(), anyString());
        verify(jwtService, never()).generateToken(any(User.class));
        verify(userMapper, never()).toResponse(any(User.class));
    }

    @Test
    void login_shouldThrow_whenPasswordIsIncorrect() {
        when(userRepository.findByEmail(request.email()))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches(
                request.password(), user.getPassword()))
                .thenReturn(false);

        assertThrows(
                InvalidCredentialsException.class,
                () -> authenticationService.login(request)
        );

        verify(passwordEncoder).matches(
                request.password(), user.getPassword());
        verify(jwtService, never()).generateToken(any(User.class));
        verify(userMapper, never()).toResponse(any(User.class));
    }
}
