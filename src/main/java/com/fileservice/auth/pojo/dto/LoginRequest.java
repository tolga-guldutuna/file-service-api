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
 * DTO used as the request body for the login endpoint.
 * <p>
 * It only carries raw credentials and must never be returned back to the client.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Login request payload containing user credentials.")
public class LoginRequest {

    /**
     * User email address used as the username.
     */
    @Schema(description = "Email address used as username.",
            example = "user@example.com",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Email must not be blank")
    @Email(message = "Email must be a valid email address")
    private String email;

    /**
     * Plain text password sent by the client.
     * This value is only used to verify credentials and is never stored as-is.
     */

    @Schema(description = "Plain-text password that will be validated by the backend.",
            example = "P@ssw0rd!",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Password must not be blank")
    @Size(min = 6, max = 255, message = "Password must be between 6 and 255 characters")
    private String password;
}
