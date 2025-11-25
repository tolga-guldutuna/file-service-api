package com.fileservice.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("JwtTokenProvider Unit Tests")
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    private static final String TEST_SECRET = Base64.getEncoder()
            .encodeToString("MyVerySecureSecretKeyForJWTTesting1234567890".getBytes());
    private static final long TOKEN_TTL_SECONDS = 3600L;
    private static final Long TEST_USER_ID = 123L;
    private static final String TEST_EMAIL = "test@example.com";

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtTokenProvider, "accessTokenTtlSeconds", TOKEN_TTL_SECONDS);
        jwtTokenProvider.initSigningKey();
    }

    @Nested
    @DisplayName("Token Generation Tests")
    class TokenGenerationTests {

        @Test
        @DisplayName("Should generate valid access token")
        void generateAccessToken_ShouldReturnValidToken() {
            // When
            String token = jwtTokenProvider.generateAccessToken(TEST_USER_ID, TEST_EMAIL);

            // Then
            assertThat(token).isNotNull();
            assertThat(token).isNotEmpty();
            assertThat(token.split("\\.")).hasSize(3); // JWT has 3 parts: header.payload.signature
        }

        @Test
        @DisplayName("Should embed user ID and email in token")
        void generateAccessToken_ShouldEmbedUserIdAndEmail() {
            // When
            String token = jwtTokenProvider.generateAccessToken(TEST_USER_ID, TEST_EMAIL);
            Claims claims = jwtTokenProvider.parseClaims(token);

            // Then
            assertThat(claims.getSubject()).isEqualTo(TEST_EMAIL);
            assertThat(claims.get("uid")).isEqualTo(TEST_USER_ID.intValue());
        }

        @Test
        @DisplayName("Should set issued-at and expiration claims")
        void generateAccessToken_ShouldSetIssuedAtAndExpiration() {
            // When
            String token = jwtTokenProvider.generateAccessToken(TEST_USER_ID, TEST_EMAIL);
            Claims claims = jwtTokenProvider.parseClaims(token);

            // Then
            assertThat(claims.getIssuedAt()).isNotNull();
            assertThat(claims.getExpiration()).isNotNull();
            assertThat(claims.getExpiration().getTime())
                    .isGreaterThan(claims.getIssuedAt().getTime());
        }
    }

    @Nested
    @DisplayName("Token Validation Tests")
    class TokenValidationTests {

        @Test
        @DisplayName("Should validate valid token")
        void isTokenValid_WithValidToken_ShouldReturnTrue() {
            // Given
            String token = jwtTokenProvider.generateAccessToken(TEST_USER_ID, TEST_EMAIL);

            // When
            boolean isValid = jwtTokenProvider.isTokenValid(token);

            // Then
            assertThat(isValid).isTrue();
        }

        @Test
        @DisplayName("Should reject malformed token")
        void isTokenValid_WithMalformedToken_ShouldReturnFalse() {
            // Given
            String malformedToken = "this.is.not.a.valid.jwt";

            // When
            boolean isValid = jwtTokenProvider.isTokenValid(malformedToken);

            // Then
            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("Should reject token with invalid signature")
        void isTokenValid_WithInvalidSignature_ShouldReturnFalse() {
            // Given
            String token = jwtTokenProvider.generateAccessToken(TEST_USER_ID, TEST_EMAIL);
            String tamperedToken = token.substring(0, token.length() - 10) + "TAMPERED12";

            // When
            boolean isValid = jwtTokenProvider.isTokenValid(tamperedToken);

            // Then
            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("Should reject expired token")
        void isTokenValid_WithExpiredToken_ShouldReturnFalse() throws InterruptedException {
            // Given - token with 1 second TTL
            ReflectionTestUtils.setField(jwtTokenProvider, "accessTokenTtlSeconds", 1L);
            String token = jwtTokenProvider.generateAccessToken(TEST_USER_ID, TEST_EMAIL);

            Thread.sleep(2000); // Wait for token to expire

            // When
            boolean isValid = jwtTokenProvider.isTokenValid(token);

            // Then
            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("Should validate token for specific user")
        void isTokenValidForUser_WithMatchingEmail_ShouldReturnTrue() {
            // Given
            String token = jwtTokenProvider.generateAccessToken(TEST_USER_ID, TEST_EMAIL);

            // When
            boolean isValid = jwtTokenProvider.isTokenValidForUser(token, TEST_EMAIL);

            // Then
            assertThat(isValid).isTrue();
        }

        @Test
        @DisplayName("Should reject token for different user")
        void isTokenValidForUser_WithDifferentEmail_ShouldReturnFalse() {
            // Given
            String token = jwtTokenProvider.generateAccessToken(TEST_USER_ID, TEST_EMAIL);

            // When
            boolean isValid = jwtTokenProvider.isTokenValidForUser(token, "other@example.com");

            // Then
            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("Should handle case-insensitive email validation")
        void isTokenValidForUser_WithDifferentCase_ShouldReturnTrue() {
            // Given
            String token = jwtTokenProvider.generateAccessToken(TEST_USER_ID, TEST_EMAIL);

            // When
            boolean isValid = jwtTokenProvider.isTokenValidForUser(token, "TEST@EXAMPLE.COM");

            // Then
            assertThat(isValid).isTrue();
        }
    }

    @Nested
    @DisplayName("Token Parsing Tests")
    class TokenParsingTests {

        @Test
        @DisplayName("Should extract email from valid token")
        void getEmailFromToken_WithValidToken_ShouldReturnEmail() {
            // Given
            String token = jwtTokenProvider.generateAccessToken(TEST_USER_ID, TEST_EMAIL);

            // When
            String email = jwtTokenProvider.getEmailFromToken(token);

            // Then
            assertThat(email).isEqualTo(TEST_EMAIL);
        }

        @Test
        @DisplayName("Should return null when extracting email from invalid token")
        void getEmailFromToken_WithInvalidToken_ShouldReturnNull() {
            // Given
            String invalidToken = "invalid.jwt.token";

            // When
            String email = jwtTokenProvider.getEmailFromToken(invalidToken);

            // Then
            assertThat(email).isNull();
        }

        @Test
        @DisplayName("Should extract user ID from valid token")
        void getUserIdFromToken_WithValidToken_ShouldReturnUserId() {
            // Given
            String token = jwtTokenProvider.generateAccessToken(TEST_USER_ID, TEST_EMAIL);

            // When
            Long userId = jwtTokenProvider.getUserIdFromToken(token);

            // Then
            assertThat(userId).isEqualTo(TEST_USER_ID);
        }

        @Test
        @DisplayName("Should return null when extracting user ID from invalid token")
        void getUserIdFromToken_WithInvalidToken_ShouldReturnNull() {
            // Given
            String invalidToken = "invalid.jwt.token";

            // When
            Long userId = jwtTokenProvider.getUserIdFromToken(invalidToken);

            // Then
            assertThat(userId).isNull();
        }

        @Test
        @DisplayName("Should parse claims from valid token")
        void parseClaims_WithValidToken_ShouldReturnClaims() {
            // Given
            String token = jwtTokenProvider.generateAccessToken(TEST_USER_ID, TEST_EMAIL);

            // When
            Claims claims = jwtTokenProvider.parseClaims(token);

            // Then
            assertThat(claims).isNotNull();
            assertThat(claims.getSubject()).isEqualTo(TEST_EMAIL);
            assertThat(claims.get("uid")).isNotNull();
        }

        @Test
        @DisplayName("Should throw JwtException when parsing invalid token")
        void parseClaims_WithInvalidToken_ShouldThrowJwtException() {
            // Given
            String invalidToken = "invalid.jwt.token";

            // When & Then
            assertThatThrownBy(() -> jwtTokenProvider.parseClaims(invalidToken))
                    .isInstanceOf(JwtException.class);
        }
    }

    @Nested
    @DisplayName("Token Resolution Tests")
    class TokenResolutionTests {

        @Test
        @DisplayName("Should extract token from valid Authorization header")
        void resolveToken_WithValidBearerToken_ShouldReturnToken() {
            // Given
            MockHttpServletRequest request = new MockHttpServletRequest();
            String token = jwtTokenProvider.generateAccessToken(TEST_USER_ID, TEST_EMAIL);
            request.addHeader("Authorization", "Bearer " + token);

            // When
            String resolved = jwtTokenProvider.resolveToken(request);

            // Then
            assertThat(resolved).isEqualTo(token);
        }

        @Test
        @DisplayName("Should return null when Authorization header is missing")
        void resolveToken_WithoutAuthorizationHeader_ShouldReturnNull() {
            // Given
            MockHttpServletRequest request = new MockHttpServletRequest();

            // When
            String resolved = jwtTokenProvider.resolveToken(request);

            // Then
            assertThat(resolved).isNull();
        }

        @Test
        @DisplayName("Should return null when Authorization header is empty")
        void resolveToken_WithEmptyAuthorizationHeader_ShouldReturnNull() {
            // Given
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader("Authorization", "");

            // When
            String resolved = jwtTokenProvider.resolveToken(request);

            // Then
            assertThat(resolved).isNull();
        }

        @Test
        @DisplayName("Should return null when Authorization header does not start with Bearer")
        void resolveToken_WithoutBearerPrefix_ShouldReturnNull() {
            // Given
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader("Authorization", "Basic sometoken");

            // When
            String resolved = jwtTokenProvider.resolveToken(request);

            // Then
            assertThat(resolved).isNull();
        }

        @Test
        @DisplayName("Should trim whitespace from extracted token")
        void resolveToken_WithWhitespaceInToken_ShouldTrimToken() {
            // Given
            MockHttpServletRequest request = new MockHttpServletRequest();
            String token = jwtTokenProvider.generateAccessToken(TEST_USER_ID, TEST_EMAIL);
            request.addHeader("Authorization", "Bearer  " + token + "  ");

            // When
            String resolved = jwtTokenProvider.resolveToken(request);

            // Then
            assertThat(resolved).isEqualTo(token);
        }
    }

    @Nested
    @DisplayName("Configuration Tests")
    class ConfigurationTests {

        @Test
        @DisplayName("Should return configured token TTL")
        void getAccessTokenTtlSeconds_ShouldReturnConfiguredValue() {
            // When
            long ttl = jwtTokenProvider.getAccessTokenTtlSeconds();

            // Then
            assertThat(ttl).isEqualTo(TOKEN_TTL_SECONDS);
        }

        @Test
        @DisplayName("Should initialize with Base64-encoded secret")
        void initSigningKey_WithBase64Secret_ShouldInitialize() {
            // Given
            JwtTokenProvider provider = new JwtTokenProvider();
            ReflectionTestUtils.setField(provider, "jwtSecret", TEST_SECRET);
            ReflectionTestUtils.setField(provider, "accessTokenTtlSeconds", TOKEN_TTL_SECONDS);

            // When
            provider.initSigningKey();
            String token = provider.generateAccessToken(TEST_USER_ID, TEST_EMAIL);

            // Then
            assertThat(token).isNotNull();
            assertThat(provider.isTokenValid(token)).isTrue();
        }

    }
}