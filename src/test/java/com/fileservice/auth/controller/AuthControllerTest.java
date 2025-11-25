package com.fileservice.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fileservice.auth.pojo.dto.LoginRequest;
import com.fileservice.auth.pojo.dto.LoginResponse;
import com.fileservice.auth.pojo.dto.RegisterUserRequest;
import com.fileservice.auth.pojo.dto.UserDto;
import com.fileservice.auth.service.AuthService;
import com.fileservice.common.exception.BusinessException;
import com.fileservice.common.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Auth Controller Tests")
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    // Test constants
    private static final String AUTH_BASE_URL = "/api/auth";
    private static final String VALID_EMAIL = "test@example.com";
    private static final String VALID_PASSWORD = "password123";
    private static final String ACCESS_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...";
    private static final Long USER_ID = 1L;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Nested
    @DisplayName("POST /api/auth/login")
    class LoginEndpointTests {

        @Test
        @DisplayName("Should return 200 and LoginResponse when credentials are valid")
        void login_WithValidCredentials_ShouldReturn200AndLoginResponse() throws Exception {
            // Given
            LoginRequest request = LoginRequest.builder()
                    .email(VALID_EMAIL)
                    .password(VALID_PASSWORD)
                    .build();

            UserDto userDto = UserDto.builder()
                    .id(USER_ID)
                    .email(VALID_EMAIL)
                    .build();

            LoginResponse loginResponse = new LoginResponse();
            loginResponse.setAccessToken(ACCESS_TOKEN);
            loginResponse.setTokenType("Bearer");
            loginResponse.setExpiresIn(3600L);
            loginResponse.setUser(userDto);

            when(authService.login(any(LoginRequest.class))).thenReturn(loginResponse);

            // When & Then
            mockMvc.perform(post(AUTH_BASE_URL + "/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").value(ACCESS_TOKEN))
                    .andExpect(jsonPath("$.tokenType").value("Bearer"))
                    .andExpect(jsonPath("$.expiresIn").value(3600))
                    .andExpect(jsonPath("$.user.id").value(USER_ID))
                    .andExpect(jsonPath("$.user.email").value(VALID_EMAIL));

            verify(authService, times(1)).login(any(LoginRequest.class));
        }

        @Test
        @DisplayName("Should return 400 when email is missing")
        void login_WithMissingEmail_ShouldReturn400() throws Exception {
            // Given
            LoginRequest request = LoginRequest.builder()
                    .password(VALID_PASSWORD)
                    .build();

            // When & Then
            mockMvc.perform(post(AUTH_BASE_URL + "/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(authService, never()).login(any(LoginRequest.class));
        }

        @Test
        @DisplayName("Should return 400 when password is missing")
        void login_WithMissingPassword_ShouldReturn400() throws Exception {
            // Given
            LoginRequest request = LoginRequest.builder()
                    .email(VALID_EMAIL)
                    .build();

            // When & Then
            mockMvc.perform(post(AUTH_BASE_URL + "/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(authService, never()).login(any(LoginRequest.class));
        }

        @Test
        @DisplayName("Should return 400 when email format is invalid")
        void login_WithInvalidEmailFormat_ShouldReturn400() throws Exception {
            // Given
            LoginRequest request = LoginRequest.builder()
                    .email("invalid-email")
                    .password(VALID_PASSWORD)
                    .build();

            // When & Then
            mockMvc.perform(post(AUTH_BASE_URL + "/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(authService, never()).login(any(LoginRequest.class));
        }

        @Test
        @DisplayName("Should return 401 when credentials are invalid")
        void login_WithInvalidCredentials_ShouldReturn401() throws Exception {
            // Given
            LoginRequest request = LoginRequest.builder()
                    .email(VALID_EMAIL)
                    .password("wrongpassword")
                    .build();

            when(authService.login(any(LoginRequest.class)))
                    .thenThrow(new BusinessException("Invalid email or password"));

            // When & Then
            mockMvc.perform(post(AUTH_BASE_URL + "/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(authService, times(1)).login(any(LoginRequest.class));
        }

        @Test
        @DisplayName("Should return 400 when JSON is malformed")
        void login_WithMalformedJson_ShouldReturn400() throws Exception {
            // Given
            String malformedJson = "{\"email\":\"test@example.com\", \"password\":}";

            // When & Then
            mockMvc.perform(post(AUTH_BASE_URL + "/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(malformedJson))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(authService, never()).login(any(LoginRequest.class));
        }

        @Test
        @DisplayName("Should return 415 when Content-Type is not JSON")
        void login_WithWrongContentType_ShouldReturn415() throws Exception {
            // Given
            LoginRequest request = LoginRequest.builder()
                    .email(VALID_EMAIL)
                    .password(VALID_PASSWORD)
                    .build();

            // When & Then
            mockMvc.perform(post(AUTH_BASE_URL + "/login")
                            .contentType(MediaType.TEXT_PLAIN)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isUnsupportedMediaType());

            verify(authService, never()).login(any(LoginRequest.class));
        }
    }

    @Nested
    @DisplayName("POST /api/auth/register")
    class RegisterEndpointTests {

        @Test
        @DisplayName("Should return 201 and UserDto when registration is successful")
        void register_WithValidData_ShouldReturn201AndUserDto() throws Exception {
            // Given
            RegisterUserRequest request = RegisterUserRequest.builder()
                    .email(VALID_EMAIL)
                    .password(VALID_PASSWORD)
                    .confirmPassword(VALID_PASSWORD)
                    .build();

            UserDto userDto = UserDto.builder()
                    .id(USER_ID)
                    .email(VALID_EMAIL)
                    .build();

            when(authService.register(any(RegisterUserRequest.class))).thenReturn(userDto);

            // When & Then
            mockMvc.perform(post(AUTH_BASE_URL + "/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(USER_ID))
                    .andExpect(jsonPath("$.email").value(VALID_EMAIL));

            verify(authService, times(1)).register(any(RegisterUserRequest.class));
        }

        @Test
        @DisplayName("Should return 400 when email is missing")
        void register_WithMissingEmail_ShouldReturn400() throws Exception {
            // Given
            RegisterUserRequest request = RegisterUserRequest.builder()
                    .password(VALID_PASSWORD)
                    .confirmPassword(VALID_PASSWORD)
                    .build();

            // When & Then
            mockMvc.perform(post(AUTH_BASE_URL + "/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(authService, never()).register(any(RegisterUserRequest.class));
        }

        @Test
        @DisplayName("Should return 400 when password is missing")
        void register_WithMissingPassword_ShouldReturn400() throws Exception {
            // Given
            RegisterUserRequest request = RegisterUserRequest.builder()
                    .email(VALID_EMAIL)
                    .confirmPassword(VALID_PASSWORD)
                    .build();

            // When & Then
            mockMvc.perform(post(AUTH_BASE_URL + "/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(authService, never()).register(any(RegisterUserRequest.class));
        }

        @Test
        @DisplayName("Should return 400 when confirmPassword is missing")
        void register_WithMissingConfirmPassword_ShouldReturn400() throws Exception {
            // Given
            RegisterUserRequest request = RegisterUserRequest.builder()
                    .email(VALID_EMAIL)
                    .password(VALID_PASSWORD)
                    .build();

            // When & Then
            mockMvc.perform(post(AUTH_BASE_URL + "/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(authService, never()).register(any(RegisterUserRequest.class));
        }

        @Test
        @DisplayName("Should return 400 when email format is invalid")
        void register_WithInvalidEmailFormat_ShouldReturn400() throws Exception {
            // Given
            RegisterUserRequest request = RegisterUserRequest.builder()
                    .email("invalid-email")
                    .password(VALID_PASSWORD)
                    .confirmPassword(VALID_PASSWORD)
                    .build();

            // When & Then
            mockMvc.perform(post(AUTH_BASE_URL + "/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(authService, never()).register(any(RegisterUserRequest.class));
        }

        @Test
        @DisplayName("Should return 400 when passwords do not match")
        void register_WithMismatchedPasswords_ShouldReturn400() throws Exception {
            // Given
            RegisterUserRequest request = RegisterUserRequest.builder()
                    .email(VALID_EMAIL)
                    .password(VALID_PASSWORD)
                    .confirmPassword("differentPassword")
                    .build();

            when(authService.register(any(RegisterUserRequest.class)))
                    .thenThrow(new BusinessException("Passwords do not match"));

            // When & Then
            mockMvc.perform(post(AUTH_BASE_URL + "/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(authService, times(1)).register(any(RegisterUserRequest.class));
        }

        @Test
        @DisplayName("Should return 400 when email already exists")
        void register_WithExistingEmail_ShouldReturn400() throws Exception {
            // Given
            RegisterUserRequest request = RegisterUserRequest.builder()
                    .email(VALID_EMAIL)
                    .password(VALID_PASSWORD)
                    .confirmPassword(VALID_PASSWORD)
                    .build();

            when(authService.register(any(RegisterUserRequest.class)))
                    .thenThrow(new BusinessException("This email is already in use"));

            // When & Then
            mockMvc.perform(post(AUTH_BASE_URL + "/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(authService, times(1)).register(any(RegisterUserRequest.class));
        }

        @Test
        @DisplayName("Should return 400 when JSON is malformed")
        void register_WithMalformedJson_ShouldReturn400() throws Exception {
            // Given
            String malformedJson = "{\"email\":\"test@example.com\", \"password\":}";

            // When & Then
            mockMvc.perform(post(AUTH_BASE_URL + "/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(malformedJson))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(authService, never()).register(any(RegisterUserRequest.class));
        }

        @Test
        @DisplayName("Should return 415 when Content-Type is not JSON")
        void register_WithWrongContentType_ShouldReturn415() throws Exception {
            // Given
            RegisterUserRequest request = RegisterUserRequest.builder()
                    .email(VALID_EMAIL)
                    .password(VALID_PASSWORD)
                    .confirmPassword(VALID_PASSWORD)
                    .build();

            // When & Then
            mockMvc.perform(post(AUTH_BASE_URL + "/register")
                            .contentType(MediaType.TEXT_PLAIN)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isUnsupportedMediaType());

            verify(authService, never()).register(any(RegisterUserRequest.class));
        }
    }
}