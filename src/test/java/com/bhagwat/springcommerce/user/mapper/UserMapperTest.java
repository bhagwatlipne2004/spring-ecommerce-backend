
package com.bhagwat.springcommerce.user.mapper;

import com.bhagwat.springcommerce.user.dto.RegisterRequest;
import com.bhagwat.springcommerce.user.dto.UserResponse;
import com.bhagwat.springcommerce.user.entity.Role;
import com.bhagwat.springcommerce.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {

    private UserMapper userMapper;

    @BeforeEach
    void setUp() {
        userMapper = new UserMapper();
    }

    @Test
    void toEntity_shouldMapAllFields() {
        RegisterRequest request = new RegisterRequest(
                "Bhagwat",
                "Lipne",
                "test@example.com",
                "StrongPassword123"
        );

        User result = userMapper.toEntity(request);

        assertNotNull(result);
        assertEquals("Bhagwat", result.getFirstName());
        assertEquals("Lipne", result.getLastName());
        assertEquals("test@example.com", result.getEmail());
        assertEquals("StrongPassword123", result.getPassword());
    }

    @Test
    void toEntity_shouldPreserveNullFields() {
        RegisterRequest request = new RegisterRequest(
                null, null, null, null
        );

        User result = userMapper.toEntity(request);

        assertNotNull(result);
        assertNull(result.getFirstName());
        assertNull(result.getLastName());
        assertNull(result.getEmail());
        assertNull(result.getPassword());
    }

    @Test
    void toResponse_shouldMapAllFields() {
        User user = new User();
        user.setId(1L);
        user.setFirstName("Bhagwat");
        user.setLastName("Lipne");
        user.setEmail("test@example.com");
        user.setRole(Role.CUSTOMER);

        UserResponse result = userMapper.toResponse(user);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Bhagwat", result.firstName());
        assertEquals("Lipne", result.lastName());
        assertEquals("test@example.com", result.email());
        assertEquals(Role.CUSTOMER, result.role());
    }

    @Test
    void toResponse_shouldPreserveNullFields() {
        User user = new User();

        UserResponse result = userMapper.toResponse(user);

        assertNotNull(result);
        assertNull(result.id());
        assertNull(result.firstName());
        assertNull(result.lastName());
        assertNull(result.email());
        assertNull(result.role());
    }
}
