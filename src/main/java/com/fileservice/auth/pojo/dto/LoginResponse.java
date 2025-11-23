package com.fileservice.auth.pojo.dto;

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
public class LoginResponse {

    /**
     * The JWT access token that the client must send in the Authorization header.
     * Example: {@code Authorization: Bearer <accessToken>}.
     */
    private String accessToken;

    /**
     * Token type prefix. For standard bearer tokens this is {@code "Bearer"}.
     */
    private String tokenType = "Bearer";

    /**
     * Token expiration in seconds.
     */
    private long expiresIn;

    /**
     * Lightweight representation of the authenticated user.
     */
    private UserDto user;
}
