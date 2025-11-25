package com.fileservice.auth.service.impl;

import com.fileservice.auth.dao.UserDao;
import com.fileservice.auth.pojo.dto.LoginRequest;
import com.fileservice.auth.pojo.dto.LoginResponse;
import com.fileservice.auth.pojo.dto.RegisterUserRequest;
import com.fileservice.auth.pojo.dto.UserDto;
import com.fileservice.auth.pojo.entity.User;
import com.fileservice.common.exception.BusinessException;
import com.fileservice.common.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AuthServiceImpl}.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceImplTest {

    @Mock
    private UserDao userDao;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthServiceImpl authService;

    private User testUser;
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "password123";
    private static final String TEST_PASSWORD_HASH = "$2a$10$hashedPassword";
    private static final String TEST_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test";

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail(TEST_EMAIL);
        testUser.setPassword(TEST_PASSWORD_HASH);

        // Set access token validity via reflection
        ReflectionTestUtils.setField(authService, "accessTokenValiditySeconds", 3600L);
    }

    // ==================== Login Tests ====================

    @Test
    @DisplayName("Should successfully login with valid credentials")
    void shouldLoginSuccessfully() {
        // Given
        LoginRequest request = LoginRequest.builder()
                .email(TEST_EMAIL)
                .password(TEST_PASSWORD)
                .build();

        when(userDao.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(TEST_PASSWORD, TEST_PASSWORD_HASH)).thenReturn(true);
        when(jwtTokenProvider.generateAccessToken(testUser.getId(), testUser.getEmail()))
                .thenReturn(TEST_TOKEN);

        // When
        LoginResponse response = authService.login(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo(TEST_TOKEN);
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getExpiresIn()).isEqualTo(3600L);
        assertThat(response.getUser()).isNotNull();
        assertThat(response.getUser().getId()).isEqualTo(testUser.getId());
        assertThat(response.getUser().getEmail()).isEqualTo(testUser.getEmail());

        verify(userDao).findByEmail(TEST_EMAIL);
        verify(passwordEncoder).matches(TEST_PASSWORD, TEST_PASSWORD_HASH);
        verify(jwtTokenProvider).generateAccessToken(testUser.getId(), testUser.getEmail());
    }

    @Test
    @DisplayName("Should throw BusinessException when user not found during login")
    void shouldThrowExceptionWhenUserNotFound() {
        // Given
        LoginRequest request = LoginRequest.builder()
                .email(TEST_EMAIL)
                .password(TEST_PASSWORD)
                .build();

        when(userDao.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Invalid email or password");

        verify(userDao).findByEmail(TEST_EMAIL);
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("Should throw BusinessException when password is incorrect")
    void shouldThrowExceptionWhenPasswordIncorrect() {
        // Given
        LoginRequest request = LoginRequest.builder()
                .email(TEST_EMAIL)
                .password("wrongPassword")
                .build();

        when(userDao.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongPassword", TEST_PASSWORD_HASH)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Invalid email or password");

        verify(userDao).findByEmail(TEST_EMAIL);
        verify(passwordEncoder).matches("wrongPassword", TEST_PASSWORD_HASH);
    }

    // ==================== Registration Tests ====================

    @Test
    @DisplayName("Should successfully register new user")
    void shouldRegisterSuccessfully() {
        // Given
        RegisterUserRequest request = RegisterUserRequest.builder()
                .email(TEST_EMAIL)
                .password(TEST_PASSWORD)
                .confirmPassword(TEST_PASSWORD)
                .build();

        when(userDao.existsByEmail(TEST_EMAIL)).thenReturn(false);
        when(passwordEncoder.encode(TEST_PASSWORD)).thenReturn(TEST_PASSWORD_HASH);
        when(userDao.save(any(User.class))).thenReturn(testUser);

        // When
        UserDto result = authService.register(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testUser.getId());
        assertThat(result.getEmail()).isEqualTo(testUser.getEmail());

        verify(userDao).existsByEmail(TEST_EMAIL);
        verify(passwordEncoder).encode(TEST_PASSWORD);
        verify(userDao).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw BusinessException when passwords do not match")
    void shouldThrowExceptionWhenPasswordsDontMatch() {
        // Given
        RegisterUserRequest request = RegisterUserRequest.builder()
                .email(TEST_EMAIL)
                .password(TEST_PASSWORD)
                .confirmPassword("differentPassword")
                .build();

        // When & Then
        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Passwords do not match");

        verify(userDao, never()).existsByEmail(anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userDao, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw BusinessException when email already exists")
    void shouldThrowExceptionWhenEmailExists() {
        // Given
        RegisterUserRequest request = RegisterUserRequest.builder()
                .email(TEST_EMAIL)
                .password(TEST_PASSWORD)
                .confirmPassword(TEST_PASSWORD)
                .build();

        when(userDao.existsByEmail(TEST_EMAIL)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("This email is already in use");

        verify(userDao).existsByEmail(TEST_EMAIL);
        verify(passwordEncoder, never()).encode(anyString());
        verify(userDao, never()).save(any(User.class));
    }
}
