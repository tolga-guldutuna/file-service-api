package com.fileservice.auth.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO used for user registration.
 * <p>
 * This object carries only raw credential data coming from the client.
 * The password contained here is never persisted in plain text; it is hashed
 * before storing it in the database. The {@code confirmPassword} field is used
 * only for backend-side consistency checks and is never stored.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Registration request payload containing new user credentials.")
public class RegisterUserRequest {

    /**
     * Unique email address of the user to be created.
     * <p>
     * This value is used as the logical username. It is validated for proper
     * email format and normalized (trimmed and converted to lower-case) before
     * being stored. The email must be unique in the system; otherwise, a
     * business error is raised during registration.
     */
    @Schema(description = "Unique email address for the new user. Used as the login identifier.",
            example = "newuser@example.com",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Email must not be blank")
    @Email(message = "Email must be a valid email address")
    private String email;

    /**
     * Plain-text password provided by the client.
     * <p>
     * This value is subject to basic length validation on the DTO level and
     * then hashed by the service layer using a {@link org.springframework.security.crypto.password.PasswordEncoder}
     * implementation (e.g. BCrypt). The raw password is never persisted or
     * returned back in any API response.
     */
    @Schema(description = "Plain-text password that will be hashed and stored.",
            example = "P@ssw0rd!",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Password must not be blank")
    @Size(min = 6, max = 255, message = "Password must be between 6 and 255 characters")
    private String password;

    /**
     * Confirmation of the plain-text password.
     * <p>
     * This field must match the {@link #password} field. It is used only for
     * server-side validation to prevent accidental typos during registration.
     * The service layer compares both values and rejects the request if they
     * do not match. This field is never stored or exposed in any response.
     */
    @Schema(description = "Password confirmation, must match the password field.",
            example = "P@ssw0rd!",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Confirm password must not be blank")
    @Size(min = 6, max = 255, message = "Confirm password must be between 6 and 255 characters")
    private String confirmPassword;
}
