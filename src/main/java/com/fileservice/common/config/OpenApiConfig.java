package com.fileservice.common.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * Central OpenAPI configuration for the File Service API.
 * <p>
 * Defines basic metadata and a reusable HTTP Bearer security scheme for JWT-based
 * authentication. Public endpoints (e.g. /api/auth/**) may choose not to require
 * this scheme, while protected controllers can reference it via
 * {@code @SecurityRequirement(name = "bearerAuth")}.
 */
@Configuration
@OpenAPIDefinition(info = @Info(
                   title = "File Service API",
                   version = "v1",
                   description = "Simple file management API used for the coding challenge.",
                   contact = @Contact(
                        name = "File Service",
                        email = "support@example.com")
        )
)
@SecurityScheme(name = "bearerAuth",
                description = "JWT Bearer token for securing API requests",
                type = SecuritySchemeType.HTTP,
                scheme = "bearer",
                bearerFormat = "JWT",
                in = SecuritySchemeIn.HEADER)
public class OpenApiConfig {
}
