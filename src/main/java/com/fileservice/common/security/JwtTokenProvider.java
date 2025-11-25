package com.fileservice.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

/**
 * Component responsible for generating and validating JWT access tokens using the
 * new JJWT 0.12+ API (Jwts.SIG, parser().verifyWith(...).build().parseSignedClaims(...)).
 * <p>
 * Tokens are signed with an HMAC-SHA-256 {@link SecretKey} and contain:
 * <ul>
 *     <li>{@code sub}: user's email</li>
 *     <li>{@code uid}: user's technical identifier</li>
 *     <li>{@code iat}: issued-at timestamp</li>
 *     <li>{@code exp}: expiration timestamp</li>
 * </ul>
 */
@Slf4j
@Component
public class JwtTokenProvider {

    private static final String CLAIM_USER_ID = "uid";

    /**
     * Raw secret value used to derive the HMAC key.
     * <p>
     * It may be either:
     * <ul>
     *     <li>A Base64-encoded string (recommended), or</li>
     *     <li>A plain string (fallback for simple environments / coding challenges).</li>
     * </ul>
     */
    @Value("${file-service.security.jwt.secret}")
    private String jwtSecret;

    /**
     * Access token time-to-live in seconds.
     */
    @Value("${file-service.security.jwt.expiration-seconds:1800}")
    private long accessTokenTtlSeconds;

    /**
     * Precomputed signing key derived from {@link #jwtSecret}.
     * <p>
     * This key is used both to sign tokens and to verify signatures via
     * {@code Jwts.builder().signWith(key, Jwts.SIG.HS256)} and
     * {@code Jwts.parser().verifyWith(key).build().parseSignedClaims(token)}.
     */
    private SecretKey signingKey;

    /**
     * Initializes the {@link SecretKey} after configuration properties are injected.
     * <p>
     * The method first tries to treat {@link #jwtSecret} as Base64-encoded. If decoding
     * fails, it falls back to using the raw bytes of the string.
     * <p>
     * Note: for a real production system, you should:
     * <ul>
     *     <li>Use a strong, random 256-bit value stored in a secure secret manager, and</li>
     *     <li>Prefer Base64-encoded secrets.</li>
     * </ul>
     */
    @PostConstruct
    protected void initSigningKey() {
        byte[] keyBytes;

        try {
            // Preferred: secret is Base64-encoded
            keyBytes = Decoders.BASE64.decode(jwtSecret);
            log.info("JWT secret treated as Base64-encoded value.");
        } catch (IllegalArgumentException ex) {
            // Fallback: use raw UTF-8 bytes (sufficient for this challenge/demo)
            log.warn("JWT secret is not valid Base64; falling back to raw UTF-8 bytes.");
            keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        }

        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generates a signed JWT access token for the given user.
     *
     * @param userId the technical identifier of the user
     * @param email  the unique email address of the user (used as the subject)
     * @return a compact JWT string that can be sent to the client
     */
    public String generateAccessToken(Long userId, String email) {
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(accessTokenTtlSeconds);

        return Jwts.builder()
                .subject(email)
                .claim(CLAIM_USER_ID, userId)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();
    }

    /**
     * Extracts claims from the given JWT token by verifying its signature with the
     * configured {@link SecretKey}.
     *
     * @param token the compact JWT string
     * @return the token's {@link Claims} payload if the signature is valid
     * @throws JwtException if the token is malformed, has an invalid signature or cannot be parsed
     */
    public Claims parseClaims(String token) throws JwtException {
        return Jwts
                .parser()                 // new API: returns JwtParserBuilder
                .verifyWith(signingKey)   // configure verification key
                .build()                  // build JwtParser
                .parseSignedClaims(token) // parse and verify JWS
                .getPayload();            // return Claims payload
    }

    /**
     * Returns the e-mail (JWT {@code sub} claim) from the given token.
     *
     * @param token the compact JWT string
     * @return the email stored as the subject, or {@code null} if the token is invalid
     */
    public String getEmailFromToken(String token) {
        try {
            Claims claims = parseClaims(token);
            return claims.getSubject();
        } catch (JwtException ex) {
            log.debug("Failed to extract email from JWT: {}", ex.getMessage());
            return null;
        }
    }

    /**
     * Returns the user id (custom {@code uid} claim) from the given token.
     *
     * @param token the compact JWT string
     * @return the user id if present and the token is valid, otherwise {@code null}
     */
    public Long getUserIdFromToken(String token) {
        try {
            Claims claims = parseClaims(token);
            Object value = claims.get(CLAIM_USER_ID);
            if (value instanceof Number number) {
                return number.longValue();
            }
            return null;
        } catch (JwtException ex) {
            log.debug("Failed to extract user id from JWT: {}", ex.getMessage());
            return null;
        }
    }

    /**
     * Validates the given JWT token:
     * <ul>
     *     <li>Verifies the signature using {@link #signingKey}.</li>
     *     <li>Checks the {@code exp} claim against the current time.</li>
     * </ul>
     *
     * @param token the compact JWT string
     * @return {@code true} if the token is structurally valid, has a correct signature
     * and is not expired; {@code false} otherwise
     */
    public boolean isTokenValid(String token) {
        try {
            Claims claims = parseClaims(token);
            Date expiration = claims.getExpiration();
            return expiration == null || expiration.after(new Date());
        } catch (ExpiredJwtException ex) {
            log.debug("JWT token is expired: {}", ex.getMessage());
            return false;
        } catch (JwtException | IllegalArgumentException ex) {
            log.debug("JWT token is invalid: {}", ex.getMessage());
            return false;
        }
    }

    /**
     * Validates a token against a specific user e-mail. This is useful to ensure
     * that the token subject matches the currently authenticated principal.
     *
     * @param token        the compact JWT string
     * @param expectedMail the e-mail address that must match the token subject
     * @return {@code true} if the token is valid and its subject equals the expected e-mail
     */
    public boolean isTokenValidForUser(String token, String expectedMail) {
        if (!isTokenValid(token)) {
            return false;
        }
        String subject = getEmailFromToken(token);
        return subject != null && subject.equalsIgnoreCase(expectedMail);
    }

    /**
     * Extracts a JWT token value from the {@code Authorization} header of the HTTP request.
     * <p>
     * Header format: {@code Authorization: Bearer <token>}.
     *
     * @param request current HTTP request
     * @return the bare token value if present and well-formed; otherwise {@code null}
     */
    public String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (bearer == null || bearer.isBlank()) {
            return null;
        }
        if (!bearer.startsWith("Bearer ")) {
            return null;
        }
        return bearer.substring("Bearer ".length()).trim();
    }

    /**
     * Returns the configured access-token time-to-live in seconds.
     *
     * @return JWT access token TTL in seconds
     */
    public long getAccessTokenTtlSeconds() {
        return accessTokenTtlSeconds;
    }
}
