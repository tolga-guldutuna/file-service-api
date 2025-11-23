package com.fileservice.file.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Detailed metadata representation of a single file.
 * <p>
 * This DTO is typically returned from a "get file details" endpoint.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileMetadataResponse {

    /**
     * Public identifier used in URLs to reference the file.
     */
    private String publicId;

    /**
     * Original filename as uploaded by the client.
     */
    private String originalName;

    /**
     * Physical filename on disk.
     * <p>
     * May or may not be exposed to the client depending on security decisions.
     */
    private String storedName;

    /**
     * File extension (e.g. PDF, PNG, DOCX).
     */
    private String extension;

    /**
     * MIME type of the file.
     */
    private String contentType;

    /**
     * Size of the file in bytes.
     */
    private long sizeBytes;

    /**
     * Relative storage path used by the backend.
     */
    private String storagePath;

    /**
     * Optional SHA-256 hash of the file content.
     */
    private String sha256Hash;

    /**
     * Indicates whether this file is temporary.
     */
    private boolean temp;

    /**
     * Optional expiration timestamp for temporary files.
     */
    private LocalDateTime expiresAt;

    /**
     * Timestamp when the file record was created.
     */
    private LocalDateTime createdAt;

    /**
     * Timestamp when the file record was last updated.
     */
    private LocalDateTime updatedAt;

    /**
     * Timestamp when the file was logically deleted, if applicable.
     */
    private LocalDateTime deletedAt;

    /**
     * Identifier of the owner user.
     */
    private Long ownerId;

    /**
     * Email of the owner user.
     */
    private String ownerEmail;
}
