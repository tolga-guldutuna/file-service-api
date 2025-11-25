package com.fileservice.file.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import lombok.Builder;
import lombok.Data;

/**
 * Lightweight representation of a stored file used in list endpoints.
 * <p>
 * This DTO is intentionally small so that it can be returned efficiently when
 * listing all files of a user. For detailed information, use
 * {@link FileMetadataResponse}.
 */
@Data
@Builder
@Schema(name = "FileListItemResponse",
        description = "Represents a single file row in the file list API.")
public class FileListItemResponse {

    /**
     * Public UUID of the file exposed in URLs instead of the internal numeric id.
     */
    @Schema(description = "Public UUID of the file.",
            example = "11111111-1111-1111-1111-111111111111",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String publicId;

    /**
     * Original filename as provided by the client during upload.
     */
    @Schema(description = "Original filename provided during upload.",
            example = "Kimlik_Fotokopisi.pdf",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String originalName;

    /**
     * File extension in upper case (e.g. PDF, PNG, DOCX).
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
     * Creation timestamp of the file metadata.
     */
    @Schema(description = "Creation timestamp of the file metadata.",
            example = "2025-11-25T10:15:30Z",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private Instant createdAt;

    /**
     * Flag indicating whether the file is marked as temporary.
     */
    @Schema(description = "Flag indicating whether the file is temporary.",
            example = "false",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean temp;
}