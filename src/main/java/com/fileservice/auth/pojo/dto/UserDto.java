package com.fileservice.auth.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Lightweight user representation used in API responses.
 * <p>
 * This DTO intentionally does not expose sensitive fields such as password hashes.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Authenticated user details that are safe to expose to clients.")
public class UserDto {

    /**
     * Technical identifier of the user.
     */
    @Schema(description = "Technical identifier of the user.", example = "1")
    private Long id;

    /**
     * Unique email of the user.
     */
    @Schema(description = "Unique email address of the user.", example = "user@example.com")
    private String email;

}
