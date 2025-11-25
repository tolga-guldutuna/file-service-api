package com.fileservice.auth.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO returned from the login endpoint.
 * <p>
 * Contains the access token and a lightweight representation of the authenticated user.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Login response containing the JWT access token and basic user information.")
public class LoginResponse {

    /**
     * The JWT access token that the client must send in the Authorization header.
     * Example: {@code Authorization: Bearer <accessToken>}.
     */
    @Schema(description = "JWT access token that must be sent in the Authorization header.", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String accessToken;

    /**
     * Token type prefix. For standard bearer tokens this is {@code "Bearer"}.
     */
    @Schema(description = "Type of the token, usually 'Bearer'.",
            example = "Bearer",
            defaultValue = "Bearer")
    private String tokenType = "Bearer";

    /**
     * Token expiration in seconds.
     */
    @Schema(description = "Token time-to-live in seconds.", example = "3600")
    private long expiresIn;

    /**
     * Lightweight representation of the authenticated user.
     */
    @Schema(description = "Authenticated user information associated with this token.")
    private UserDto user;
}
