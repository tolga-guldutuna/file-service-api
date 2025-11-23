package com.fileservice.auth.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Lightweight user representation used in API responses.
 * <p>
 * This DTO intentionally does not expose sensitive fields such as password hashes.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {

    /**
     * Technical identifier of the user.
     */
    private Long id;

    /**
     * Unique email of the user.
     */
    private String email;

    /**
     * Optional full name.
     */
    private String fullName;

    /**
     * List of role names assigned to the user.
     * Example: {@code ["ROLE_USER", "ROLE_ADMIN"]}.
     */
    private List<String> roles = new ArrayList<>();
}
