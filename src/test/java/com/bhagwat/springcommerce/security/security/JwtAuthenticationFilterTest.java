
package com.bhagwat.springcommerce.security.security;

import com.bhagwat.springcommerce.user.entity.Role;
import com.bhagwat.springcommerce.user.entity.User;
import com.bhagwat.springcommerce.user.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private User user;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();

        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setRole(Role.CUSTOMER);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldContinueFilterChainWhenAuthorizationHeaderIsMissing()
            throws ServletException, IOException {

        when(request.getHeader("Authorization")).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(
                request, response, filterChain
        );

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService, userRepository);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void shouldContinueFilterChainWhenAuthorizationHeaderIsNotBearer()
            throws ServletException, IOException {

        when(request.getHeader("Authorization"))
                .thenReturn("Basic abc123");

        jwtAuthenticationFilter.doFilterInternal(
                request, response, filterChain
        );

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService, userRepository);
    }

    @Test
    void shouldAuthenticateUserWhenTokenIsValid()
            throws ServletException, IOException {

        when(request.getHeader("Authorization"))
                .thenReturn("Bearer valid-token");
        when(jwtService.extractUserName("valid-token"))
                .thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));
        when(jwtService.isTokenValid("valid-token", user))
                .thenReturn(true);

        jwtAuthenticationFilter.doFilterInternal(
                request, response, filterChain
        );

        var authentication =
                SecurityContextHolder.getContext().getAuthentication();

        assertNotNull(authentication);
        assertInstanceOf(
                UsernamePasswordAuthenticationToken.class,
                authentication
        );
        assertSame(user, authentication.getPrincipal());
        assertTrue(authentication.isAuthenticated());
        assertTrue(authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_CUSTOMER")));

        verify(filterChain).doFilter(request, response);
        verify(response, never()).sendError(anyInt(), anyString());
    }

    @Test
    void shouldRejectInvalidToken()
            throws ServletException, IOException {

        when(request.getHeader("Authorization"))
                .thenReturn("Bearer invalid-token");
        when(jwtService.extractUserName("invalid-token"))
                .thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));
        when(jwtService.isTokenValid("invalid-token", user))
                .thenReturn(false);

        jwtAuthenticationFilter.doFilterInternal(
                request, response, filterChain
        );

        assertNull(SecurityContextHolder.getContext().getAuthentication());

        verify(response).sendError(
                HttpServletResponse.SC_UNAUTHORIZED,
                "Invalid or expired token"
        );
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void shouldRejectTokenWhenUserDoesNotExist()
            throws ServletException, IOException {

        when(request.getHeader("Authorization"))
                .thenReturn("Bearer valid-token");
        when(jwtService.extractUserName("valid-token"))
                .thenReturn("missing@example.com");
        when(userRepository.findByEmail("missing@example.com"))
                .thenReturn(Optional.empty());

        jwtAuthenticationFilter.doFilterInternal(
                request, response, filterChain
        );

        assertNull(SecurityContextHolder.getContext().getAuthentication());

        verify(response).sendError(
                HttpServletResponse.SC_UNAUTHORIZED,
                "Invalid or expired token"
        );
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void shouldRejectRequestWhenTokenParsingFails()
            throws ServletException, IOException {

        when(request.getHeader("Authorization"))
                .thenReturn("Bearer broken-token");
        when(jwtService.extractUserName("broken-token"))
                .thenThrow(new IllegalArgumentException("Invalid token"));

        jwtAuthenticationFilter.doFilterInternal(
                request, response, filterChain
        );

        assertNull(SecurityContextHolder.getContext().getAuthentication());

        verify(response).sendError(
                HttpServletResponse.SC_UNAUTHORIZED,
                "Invalid or expired token"
        );
        verifyNoInteractions(userRepository);
        verify(filterChain, never()).doFilter(request, response);
    }
}
