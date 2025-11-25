package com.fileservice.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fileservice.auth.pojo.dto.LoginRequest;
import com.fileservice.auth.pojo.dto.LoginResponse;
import com.fileservice.auth.pojo.dto.RegisterUserRequest;
import com.fileservice.auth.pojo.dto.UserDto;
import com.fileservice.auth.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit tests for {@link AuthController}.
 */
@WebMvcTest(AuthController.class)
@DisplayName("AuthController Unit Tests")
@Import(TestSecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @Test
    @WithMockUser
    @DisplayName("Should login successfully with valid credentials")
    void shouldLoginSuccessfully() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .email("test@example.com")
                .password("password123")
                .build();

        UserDto userDto = UserDto.builder()
                .id(1L)
                .email("test@example.com")
                .build();

        LoginResponse response = new LoginResponse();
        response.setAccessToken("token");
        response.setTokenType("Bearer");
        response.setExpiresIn(3600L);
        response.setUser(userDto);

        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("token"))
                .andExpect(jsonPath("$.user.email").value("test@example.com"));
    }

    @Test
    @WithMockUser
    @DisplayName("Should register user successfully")
    void shouldRegisterSuccessfully() throws Exception {
        RegisterUserRequest request = RegisterUserRequest.builder()
                .email("newuser@example.com")
                .password("password123")
                .confirmPassword("password123")
                .build();

        UserDto userDto = UserDto.builder()
                .id(1L)
                .email("newuser@example.com")
                .build();

        when(authService.register(any(RegisterUserRequest.class))).thenReturn(userDto);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("newuser@example.com"));
    }
}
