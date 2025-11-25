package com.fileservice.common.exception;

/**
 * Generic unchecked exception type used to signal business rule violations.
 * <p>
 * This exception is intended for domain / application-level errors such as
 * invalid credentials, duplicate email, illegal state transitions, etc.
 * It is <b>not</b> meant for technical problems like database connectivity
 * issues or low-level I/O failures.
 * <p>
 * Typical usage:
 * <pre>
 *     if (userDao.existsByEmail(email)) {
 *         throw new BusinessException("This email is already in use");
 *     }
 * </pre>
 * In the web layer, a {@code @ControllerAdvice}-based global exception handler
 * can translate this exception into an appropriate HTTP response (for example
 * HTTP 400 Bad Request).
 */
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a new {@code BusinessException} with the given message.
     *
     * @param message human-readable description of the business rule violation
     */
    public BusinessException(String message) {
        super(message);
    }

    /**
     * Creates a new {@code BusinessException} with the given message and root cause.
     *
     * @param message human-readable description of the business rule violation
     * @param cause   underlying cause, can be used to preserve the original stack trace
     */
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
