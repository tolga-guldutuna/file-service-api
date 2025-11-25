package com.fileservice.auth.service;

import com.fileservice.auth.pojo.dto.LoginRequest;
import com.fileservice.auth.pojo.dto.LoginResponse;
import com.fileservice.auth.pojo.dto.RegisterUserRequest;
import com.fileservice.auth.pojo.dto.UserDto;
import com.fileservice.common.exception.BusinessException;

/**
 * Application-level service responsible for authentication and user registration
 * use cases.
 * <p>
 * This service encapsulates the core business logic for:
 * <ul>
 *     <li>Authenticating an existing user and issuing a JWT access token.</li>
 *     <li>Registering a new user with a unique email and hashed password.</li>
 * </ul>
 * It does not expose any persistence details; all interaction with the database
 * is delegated to the underlying repository/DAO layer.
 */
public interface AuthService {

    /**
     * Authenticates a user based on the provided credentials and issues a JWT access token.
     * <p>
     * The implementation is expected to:
     * <ol>
     *     <li>Normalize the email (trim and lower-case).</li>
     *     <li>Lookup the user by email.</li>
     *     <li>Validate the clear-text password against the stored hash.</li>
     *     <li>Generate a signed JWT token bound to the authenticated user.</li>
     * </ol>
     *
     * @param request the login request DTO containing the email and raw password
     * @return a {@link LoginResponse} containing the JWT token and basic user information
     * @throws BusinessException if the credentials are invalid or the account is not allowed to login
     */
    LoginResponse login(LoginRequest request);

    /**
     * Registers a new user in the system based on the provided credentials.
     * <p>
     * The implementation is expected to:
     * <ol>
     *     <li>Normalize and validate the email address.</li>
     *     <li>Verify that {@code password} and {@code confirmPassword} match.</li>
     *     <li>Ensure that the email is not already used by another user.</li>
     *     <li>Hash the plain-text password and persist the new user record.</li>
     * </ol>
     * On success, the method returns a lightweight {@link UserDto} that does not
     * expose any sensitive data such as password hashes.
     *
     * @param request the registration request containing email, password and password confirmation
     * @return a {@link UserDto} representing the newly created user
     * @throws BusinessException if the email is already in use or the passwords do not match
     */
    UserDto register(RegisterUserRequest request);
}
