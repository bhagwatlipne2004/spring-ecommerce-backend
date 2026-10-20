
package com.bhagwat.springcommerce.user.service.impl;

import com.bhagwat.springcommerce.common.exception.UserAlreadyExistsException;
import com.bhagwat.springcommerce.user.dto.RegisterRequest;
import com.bhagwat.springcommerce.user.dto.UserResponse;
import com.bhagwat.springcommerce.user.entity.Role;
import com.bhagwat.springcommerce.user.entity.User;
import com.bhagwat.springcommerce.user.mapper.UserMapper;
import com.bhagwat.springcommerce.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private RegisterRequest request;
    private User user;
    private UserResponse response;

    @BeforeEach
    void setUp() {
        request = new RegisterRequest(
                "Bhagwat",
                "Lipne",
                "test@example.com",
                "StrongPassword123"
        );

        user = new User();
        user.setEmail("test@example.com");

        response = new UserResponse(
                1L,
                "Bhagwat",
                "Lipne",
                "test@example.com",
                Role.CUSTOMER
        );
    }

    @Test
    void register_shouldCreateCustomerWithEncodedPassword() {
        when(userRepository.findByEmail(request.email()))
                .thenReturn(Optional.empty());
        when(userMapper.toEntity(request)).thenReturn(user);
        when(passwordEncoder.encode(request.password()))
                .thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toResponse(user)).thenReturn(response);

        UserResponse result = userService.register(request);

        assertSame(response, result);
        assertEquals(Role.CUSTOMER, user.getRole());
        assertEquals("encoded-password", user.getPassword());

        verify(userRepository).findByEmail(request.email());
        verify(passwordEncoder).encode(request.password());
        verify(userRepository, times(2)).save(user);
        verify(userMapper).toResponse(user);
    }

    @Test
    void register_shouldThrowWhenEmailAlreadyExists() {
        when(userRepository.findByEmail(request.email()))
                .thenReturn(Optional.of(user));

        assertThrows(
                UserAlreadyExistsException.class,
                () -> userService.register(request)
        );

        verify(userRepository).findByEmail(request.email());
        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(anyString());
        verify(userMapper, never()).toEntity(any(RegisterRequest.class));
    }
}
