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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthServiceImpl Unit Tests")
class AuthServiceImplTest {

    @Mock
    private UserDao userDao;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthServiceImpl authService;

    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "SecurePass123!";
    private static final String ENCODED_PASSWORD = "$2a$10$encodedPasswordHash";
    private static final String JWT_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...";
    private static final Long TEST_USER_ID = 1L;
    private static final long TOKEN_VALIDITY_SECONDS = 3600L;

    @BeforeEach
    void setUp() {
        // @Value alanını test için set et
        ReflectionTestUtils.setField(authService, "accessTokenValiditySeconds", TOKEN_VALIDITY_SECONDS);
    }

    @Nested
    @DisplayName("Login Tests")
    class LoginTests {

        @Test
        @DisplayName("Should successfully authenticate user with valid credentials")
        void login_WithValidCredentials_ShouldReturnLoginResponse() {
            // Given
            LoginRequest request = new LoginRequest();
            request.setEmail(TEST_EMAIL);
            request.setPassword(TEST_PASSWORD);

            User user = createTestUser();

            when(userDao.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches(TEST_PASSWORD, ENCODED_PASSWORD)).thenReturn(true);
            when(jwtTokenProvider.generateAccessToken(TEST_USER_ID, TEST_EMAIL)).thenReturn(JWT_TOKEN);

            // When
            LoginResponse response = authService.login(request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getAccessToken()).isEqualTo(JWT_TOKEN);
            assertThat(response.getTokenType()).isEqualTo("Bearer");
            assertThat(response.getExpiresIn()).isEqualTo(TOKEN_VALIDITY_SECONDS);
            assertThat(response.getUser()).isNotNull();
            assertThat(response.getUser().getId()).isEqualTo(TEST_USER_ID);
            assertThat(response.getUser().getEmail()).isEqualTo(TEST_EMAIL);

            verify(userDao).findByEmail(TEST_EMAIL);
            verify(passwordEncoder).matches(TEST_PASSWORD, ENCODED_PASSWORD);
            verify(jwtTokenProvider).generateAccessToken(TEST_USER_ID, TEST_EMAIL);
        }

        @Test
        @DisplayName("Should normalize email to lowercase before lookup")
        void login_WithUppercaseEmail_ShouldNormalizeToLowercase() {
            // Given
            LoginRequest request = new LoginRequest();
            request.setEmail("TEST@EXAMPLE.COM");
            request.setPassword(TEST_PASSWORD);

            User user = createTestUser();

            when(userDao.findByEmail("test@example.com")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches(TEST_PASSWORD, ENCODED_PASSWORD)).thenReturn(true);
            when(jwtTokenProvider.generateAccessToken(anyLong(), anyString())).thenReturn(JWT_TOKEN);

            // When
            authService.login(request);

            // Then
            verify(userDao).findByEmail("test@example.com");
        }

        @Test
        @DisplayName("Should trim whitespace from email before lookup")
        void login_WithWhitespaceInEmail_ShouldTrimBeforeLookup() {
            // Given
            LoginRequest request = new LoginRequest();
            request.setEmail("  test@example.com  ");
            request.setPassword(TEST_PASSWORD);

            User user = createTestUser();

            when(userDao.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches(TEST_PASSWORD, ENCODED_PASSWORD)).thenReturn(true);
            when(jwtTokenProvider.generateAccessToken(anyLong(), anyString())).thenReturn(JWT_TOKEN);

            // When
            authService.login(request);

            // Then
            verify(userDao).findByEmail(TEST_EMAIL);
        }

        @Test
        @DisplayName("Should throw BusinessException when user not found")
        void login_WithNonExistentEmail_ShouldThrowBusinessException() {
            // Given
            LoginRequest request = new LoginRequest();
            request.setEmail("notfound@example.com");
            request.setPassword(TEST_PASSWORD);

            when(userDao.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Invalid email or password");

            verify(userDao).findByEmail("notfound@example.com");
            verify(passwordEncoder, never()).matches(anyString(), anyString());
            verify(jwtTokenProvider, never()).generateAccessToken(anyLong(), anyString());
        }

        @Test
        @DisplayName("Should throw BusinessException when password does not match")
        void login_WithIncorrectPassword_ShouldThrowBusinessException() {
            // Given
            LoginRequest request = new LoginRequest();
            request.setEmail(TEST_EMAIL);
            request.setPassword("WrongPassword123!");

            User user = createTestUser();

            when(userDao.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("WrongPassword123!", ENCODED_PASSWORD)).thenReturn(false);

            // When & Then
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Invalid email or password");

            verify(userDao).findByEmail(TEST_EMAIL);
            verify(passwordEncoder).matches("WrongPassword123!", ENCODED_PASSWORD);
            verify(jwtTokenProvider, never()).generateAccessToken(anyLong(), anyString());
        }
    }

    @Nested
    @DisplayName("Register Tests")
    class RegisterTests {

        @Test
        @DisplayName("Should successfully register new user with valid data")
        void register_WithValidData_ShouldReturnUserDto() {
            // Given
            RegisterUserRequest request = new RegisterUserRequest();
            request.setEmail(TEST_EMAIL);
            request.setPassword(TEST_PASSWORD);
            request.setConfirmPassword(TEST_PASSWORD);

            User savedUser = createTestUser();

            when(userDao.existsByEmail(TEST_EMAIL)).thenReturn(false);
            when(passwordEncoder.encode(TEST_PASSWORD)).thenReturn(ENCODED_PASSWORD);
            when(userDao.save(any(User.class))).thenReturn(savedUser);

            // When
            UserDto result = authService.register(request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(TEST_USER_ID);
            assertThat(result.getEmail()).isEqualTo(TEST_EMAIL);

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userDao).save(userCaptor.capture());

            User capturedUser = userCaptor.getValue();
            assertThat(capturedUser.getEmail()).isEqualTo(TEST_EMAIL);
            assertThat(capturedUser.getPassword()).isEqualTo(ENCODED_PASSWORD);
        }

        @Test
        @DisplayName("Should normalize email to lowercase during registration")
        void register_WithUppercaseEmail_ShouldNormalizeToLowercase() {
            // Given
            RegisterUserRequest request = new RegisterUserRequest();
            request.setEmail("TEST@EXAMPLE.COM");
            request.setPassword(TEST_PASSWORD);
            request.setConfirmPassword(TEST_PASSWORD);

            User savedUser = createTestUser();

            when(userDao.existsByEmail("test@example.com")).thenReturn(false);
            when(passwordEncoder.encode(TEST_PASSWORD)).thenReturn(ENCODED_PASSWORD);
            when(userDao.save(any(User.class))).thenReturn(savedUser);

            // When
            authService.register(request);

            // Then
            verify(userDao).existsByEmail("test@example.com");
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userDao).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getEmail()).isEqualTo("test@example.com");
        }

        @Test
        @DisplayName("Should trim whitespace from email during registration")
        void register_WithWhitespaceInEmail_ShouldTrimEmail() {
            // Given
            RegisterUserRequest request = new RegisterUserRequest();
            request.setEmail("  test@example.com  ");
            request.setPassword(TEST_PASSWORD);
            request.setConfirmPassword(TEST_PASSWORD);

            User savedUser = createTestUser();

            when(userDao.existsByEmail(TEST_EMAIL)).thenReturn(false);
            when(passwordEncoder.encode(TEST_PASSWORD)).thenReturn(ENCODED_PASSWORD);
            when(userDao.save(any(User.class))).thenReturn(savedUser);

            // When
            authService.register(request);

            // Then
            verify(userDao).existsByEmail(TEST_EMAIL);
        }

        @Test
        @DisplayName("Should throw BusinessException when passwords do not match")
        void register_WithMismatchedPasswords_ShouldThrowBusinessException() {
            // Given
            RegisterUserRequest request = new RegisterUserRequest();
            request.setEmail(TEST_EMAIL);
            request.setPassword(TEST_PASSWORD);
            request.setConfirmPassword("DifferentPassword123!");

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
        void register_WithExistingEmail_ShouldThrowBusinessException() {
            // Given
            RegisterUserRequest request = new RegisterUserRequest();
            request.setEmail(TEST_EMAIL);
            request.setPassword(TEST_PASSWORD);
            request.setConfirmPassword(TEST_PASSWORD);

            when(userDao.existsByEmail(TEST_EMAIL)).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> authService.register(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("This email is already in use");

            verify(userDao).existsByEmail(TEST_EMAIL);
            verify(passwordEncoder, never()).encode(anyString());
            verify(userDao, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Should encode password before saving user")
        void register_ShouldEncodePasswordBeforeSaving() {
            // Given
            RegisterUserRequest request = new RegisterUserRequest();
            request.setEmail(TEST_EMAIL);
            request.setPassword(TEST_PASSWORD);
            request.setConfirmPassword(TEST_PASSWORD);

            User savedUser = createTestUser();

            when(userDao.existsByEmail(TEST_EMAIL)).thenReturn(false);
            when(passwordEncoder.encode(TEST_PASSWORD)).thenReturn(ENCODED_PASSWORD);
            when(userDao.save(any(User.class))).thenReturn(savedUser);

            // When
            authService.register(request);

            // Then
            verify(passwordEncoder).encode(TEST_PASSWORD);
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userDao).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getPassword()).isEqualTo(ENCODED_PASSWORD);
        }
    }

    // Helper method
    private User createTestUser() {
        User user = new User();
        user.setId(TEST_USER_ID);
        user.setEmail(TEST_EMAIL);
        user.setPassword(ENCODED_PASSWORD);
        return user;
    }
}