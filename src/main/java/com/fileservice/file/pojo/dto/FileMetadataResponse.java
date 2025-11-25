package com.fileservice.file.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Detailed metadata representation of a single file.
 * <p>
 * This DTO is typically returned from a "get file details" endpoint.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Detailed file metadata used for the file details view.")
public class FileMetadataResponse {

    /**
     * Public identifier used in URLs to reference the file.
     */
    @Schema(description = "Public UUID that uniquely identifies the file.",
            example = "3fa85f64-5717-4562-b3fc-2c963f66afa6",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String publicId;

    /**
     * Original filename as uploaded by the client.
     */
    @Schema(description = "Original filename as uploaded by the client.",
            example = "Meeting_Notes_Q4_2024.pdf",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String originalName;

    /**
     * Physical filename on disk.
     * <p>
     * May or may not be exposed to the client depending on security decisions.
     */

    @Schema(description = "Physical filename on disk. Usually derived from a UUID plus extension.", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6.pdf")
    private String storedName;

    /**
     * File extension (e.g. PDF, PNG, DOCX).
     */
    @Schema(description = "Upper-case file extension.",
            example = "PDF",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String extension;

    /**
     * MIME type of the file.
     */
    @Schema(description = "MIME content type of the file.",
            example = "application/pdf",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String contentType;

    /**
     * Size of the file in bytes.
     */
    @Schema(description = "File size in bytes.",
            example = "1258291",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private long sizeBytes;

    /**
     * Relative storage path used by the backend.
     */
    @Schema(description = "Relative storage path used by the backend.", example = "2025/11/23/P/3fa85f64-5717-4562-b3fc-2c963f66afa6.pdf")
    private String storagePath;

    /**
     * Optional SHA-256 hash of the file content.
     */
    @Schema(description = "Optional SHA-256 hash of the file content.", example = "b1946ac92492d2347c6235b4d2611184b1946ac92492d2347c6235b4d2611184")
    private String sha256Hash;

    /**
     * Indicates whether this file is temporary.
     */
    @Schema(description = "Indicates whether this file is temporary.", example = "false")
    private boolean temp;

    /**
     * Optional expiration timestamp for temporary files.
     */
    @Schema(description = "Optional expiration timestamp for temporary files.", example = "2024-12-01T10:15:30")
    private LocalDateTime expiresAt;

    /**
     * Timestamp when the file record was created.
     */
    @Schema(description = "Timestamp when the file record was created.",
            example = "2024-11-23T10:15:30",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createdAt;

    /**
     * Timestamp when the file record was last updated.
     */
    @Schema(description = "Timestamp when the file record was last updated.",
            example = "2024-11-23T11:00:00",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime updatedAt;

    /**
     * Timestamp when the file was logically deleted, if applicable.
     */
    @Schema(description = "Timestamp when the file was logically deleted, if applicable.", example = "2024-11-30T09:45:00")
    private LocalDateTime deletedAt;

    /**
     * Identifier of the owner user.
     */
    @Schema(description = "Identifier of the owner user.",
            example = "1",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private Long ownerId;

    /**
     * Email of the owner user.
     */
    @Schema(description = "Email of the owner user.",
            example = "john.doe@example.com",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String ownerEmail;
}
