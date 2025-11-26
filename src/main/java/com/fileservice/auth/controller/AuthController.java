package com.fileservice.auth.controller;

import com.fileservice.auth.pojo.dto.LoginRequest;
import com.fileservice.auth.pojo.dto.LoginResponse;
import com.fileservice.auth.pojo.dto.RegisterUserRequest;
import com.fileservice.auth.pojo.dto.UserDto;
import com.fileservice.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller exposing authentication endpoints such as login and user registration.
 * <p>
 * All endpoints under this controller are publicly accessible and do not require
 * a JWT access token. Successful login returns a signed JWT that must be used in
 * the {@code Authorization} header (Bearer scheme) for protected APIs.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for login and user registration")
@CrossOrigin(
        origins = "http://localhost:3000",
        allowedHeaders = "*",
        allowCredentials = "true",
        methods = {
                RequestMethod.GET,
                RequestMethod.POST,
                RequestMethod.PUT,
                RequestMethod.DELETE,
                RequestMethod.OPTIONS
        }
)
public class AuthController {

    private final AuthService authService;

    /**
     * Authenticates a user with the given credentials and returns a JWT access token.
     *
     * @param request login request containing email and plain-text password
     * @return {@link LoginResponse} with JWT token and basic user information
     */
    @PostMapping("/login")
    @Operation(summary = "Authenticate user and obtain JWT", description = "Validates the provided credentials and returns a signed JWT access token.")
    @ApiResponse(responseCode = "200", description = "Authentication successful", content = @Content(schema = @Schema(implementation = LoginResponse.class)))
    @ApiResponse(responseCode = "400", description = "Validation error in request payload", content = @Content)
    @ApiResponse(responseCode = "401", description = "Invalid email or password", content = @Content)
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Registers a new user account using the provided email and password.
     * <p>
     * If registration is successful, the created user is returned without sensitive
     * security-related fields (e.g. no password hash).
     *
     * @param request registration request containing email and password confirmation
     * @return {@link UserDto} for the newly created user
     */
    @PostMapping("/register")
    @Operation(summary = "Register new user", description = "Creates a new user account with the provided credentials.")
    @ApiResponse(responseCode = "201", description = "User successfully created", content = @Content(schema = @Schema(implementation = UserDto.class)))
    @ApiResponse(responseCode = "400", description = "Validation error or business rule violation (e.g. email already in use)", content = @Content)
    public ResponseEntity<UserDto> register(@Valid @RequestBody RegisterUserRequest request) {
        UserDto created = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
