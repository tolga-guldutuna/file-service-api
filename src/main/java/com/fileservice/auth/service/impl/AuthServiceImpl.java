package com.fileservice.auth.service.impl;

import com.fileservice.auth.dao.UserDao;
import com.fileservice.auth.pojo.dto.LoginRequest;
import com.fileservice.auth.pojo.dto.LoginResponse;
import com.fileservice.auth.pojo.dto.RegisterUserRequest;
import com.fileservice.auth.pojo.dto.UserDto;
import com.fileservice.auth.pojo.entity.User;
import com.fileservice.auth.service.AuthService;
import com.fileservice.common.exception.BusinessException;
import com.fileservice.common.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default implementation of the {@link AuthService} interface.
 * <p>
 * This service coordinates authentication and registration use cases by:
 * <ul>
 *     <li>Interacting with the {@link UserDao} for persistence operations.</li>
 *     <li>Using a {@link PasswordEncoder} to verify and hash passwords.</li>
 *     <li>Delegating JWT creation to {@link JwtTokenProvider}.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserDao userDao;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * JWT access token validity period in seconds.
     * <p>
     * Example configuration in {@code application.yml}:
     * <pre>
     * security:
     *   jwt:
     *     access-token-validity-seconds: 3600
     * </pre>
     */
    @Value("${security.jwt.access-token-validity-seconds:3600}")
    private long accessTokenValiditySeconds;

    /**
     * Authenticates a user using the provided credentials and issues a JWT access token.
     * <p>
     * This implementation applies the following steps:
     * <ol>
     *     <li>Normalize the email (trim and lower-case).</li>
     *     <li>Lookup the user by email.</li>
     *     <li>Return a generic error if the user cannot be found.</li>
     *     <li>Validate the raw password against the stored hash using {@link PasswordEncoder}.</li>
     *     <li>Return the same generic error if the password does not match.</li>
     *     <li>Generate a signed JWT bound to the authenticated user.</li>
     *     <li>Return a {@link LoginResponse} containing the token and a lightweight {@link UserDto}.</li>
     * </ol>
     * Error messages are intentionally generic to avoid exposing whether an email
     * exists in the system.
     *
     * @param request the login request DTO containing email and raw password
     * @return a {@link LoginResponse} with the JWT token and basic user information
     * @throws BusinessException if authentication fails
     */
    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        // Normalize email for consistent lookups
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        // Find user by email or fail with a generic authentication error
        User user = userDao.findByEmail(normalizedEmail)
                .orElseThrow(() -> new BusinessException("Invalid email or password"));

        // Validate password against stored hash
        boolean passwordMatches = passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()   // entity column 'password' holds the hash
        );

        if (!passwordMatches) {
            // Use the same generic message for security reasons
            throw new BusinessException("Invalid email or password");
        }

        // Create JWT token for the authenticated user
        // Adjust the signature of createToken(...) to match your JwtTokenProvider implementation
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail());

        UserDto userDto = toUserDto(user);

        LoginResponse response = new LoginResponse();
        response.setAccessToken(accessToken);
        response.setTokenType("Bearer");
        response.setExpiresIn(accessTokenValiditySeconds);
        response.setUser(userDto);

        return response;
    }

    /**
     * Registers a new user in the system using the provided credentials.
     * <p>
     * This implementation enforces the following business rules:
     * <ul>
     *     <li>The email is normalized (trimmed and converted to lower-case).</li>
     *     <li>{@code password} and {@code confirmPassword} must match.</li>
     *     <li>The email must not already exist in the underlying repository.</li>
     *     <li>The raw password is hashed before the user entity is persisted.</li>
     * </ul>
     * If any of these rules is violated, a {@link BusinessException} is thrown.
     *
     * @param request the registration request containing email, password and password confirmation
     * @return a {@link UserDto} representing the newly created user
     * @throws BusinessException if the email is already in use or passwords do not match
     */
    @Override
    @Transactional
    public UserDto register(RegisterUserRequest request) {
        // Normalize email for consistency and uniqueness checks
        String normalizedEmail = request.getEmail()
                .trim()
                .toLowerCase();

        // Basic password confirmation check at the business layer
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException("Passwords do not match");
        }

        // Ensure that the email is not already registered
        if (userDao.existsByEmail(normalizedEmail)) {
            throw new BusinessException("This email is already in use");
        }

        // Hash the raw password before persisting
        String passwordHash = passwordEncoder.encode(request.getPassword());

        // Map DTO to entity
        User user = new User();
        user.setEmail(normalizedEmail);
        user.setPassword(passwordHash);

        // Persist the new user
        User saved = userDao.save(user);

        // Convert entity to DTO
        return toUserDto(saved);
    }

    /**
     * Maps a {@link User} JPA entity to a lightweight {@link UserDto}.
     * <p>
     * This method intentionally exposes only non-sensitive fields and must never
     * include password hashes or any internal security-related attributes.
     *
     * @param user the persisted {@link User} entity; must not be {@code null}
     * @return a {@link UserDto} containing public user information
     */
    private UserDto toUserDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .build();
    }
}
