package com.fileservice.file.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import lombok.Builder;
import lombok.Data;

/**
 * Detailed metadata representation for a single stored file.
 * <p>
 * This DTO is typically used by endpoints that fetch information about
 * a specific file identified by its public id.
 */
@Data
@Builder
@Schema(name = "FileMetadataResponse",
        description = "Detailed metadata of a single stored file.")
public class FileMetadataResponse {

    /**
     * Public UUID of the file used in external URLs.
     */
    @Schema(description = "Public UUID of the file.",
            example = "11111111-1111-1111-1111-111111111111",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String publicId;

    /**
     * Original filename supplied by the client during upload.
     */
    @Schema(description = "Original filename provided during upload.",
            example = "Kimlik_Fotokopisi.pdf",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String originalName;

    /**
     * Physical filename used on disk (usually publicId + extension).
     */
    @Schema(description = "Physical filename stored on disk.",
            example = "11111111-1111-1111-1111-111111111111.pdf",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String storedName;

    /**
     * File extension in upper case.
     */
    @Schema(description = "File extension in upper case.",
            example = "PDF",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String extension;

    /**
     * MIME content type of the file.
     */
    @Schema(description = "MIME content type of the file.",
            example = "application/pdf",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String contentType;

    /**
     * File size in bytes.
     */
    @Schema(description = "File size in bytes.",
            example = "24576",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private long sizeBytes;

    /**
     * Relative storage path used by the backend to locate the file on disk.
     */
    @Schema(description = "Relative storage path used by the backend to locate the file.",
            example = "2025/11/25/pdf/K/11111111-1111-1111-1111-111111111111.pdf",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String storagePath;

    /**
     * Optional SHA-256 hash of the file contents, if computed.
     */
    @Schema(description = "Optional SHA-256 hash of the file contents.",
            example = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855")
    private String sha256Hash;

    /**
     * Flag indicating whether the file is temporary.
     */
    @Schema(description = "Flag indicating whether the file is temporary.",
            example = "false",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean temp;

    /**
     * Optional expiration timestamp for temporary files.
     */
    @Schema(description = "Optional expiration timestamp for temporary files.",
            example = "2025-11-30T23:59:59Z")
    private Instant expiresAt;

    /**
     * Creation timestamp stored in the database.
     */
    @Schema(description = "Creation timestamp of the file metadata.",
            example = "2025-11-25T10:15:30Z",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private Instant createdAt;

    /**
     * Last update timestamp stored in the database.
     */
    @Schema(description = "Last update timestamp of the file metadata.",
            example = "2025-11-25T11:00:00Z",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private Instant updatedAt;

    /**
     * Soft-delete timestamp. If non-null, the file is considered logically deleted.
     */
    @Schema(description = "Soft-delete timestamp. When set, the file is considered deleted.",
            example = "2025-11-26T09:00:00Z")
    private Instant deletedAt;
}
