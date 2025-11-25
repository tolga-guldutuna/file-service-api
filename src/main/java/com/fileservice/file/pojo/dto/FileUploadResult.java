package com.fileservice.file.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * Response returned after a successful file upload operation.
 * <p>
 * This DTO contains enough information for the client to reference or display
 * the uploaded file without exposing internal implementation details.
 */
@Data
@Builder
@Schema(name = "FileUploadResult",
        description = "Result of a successful file upload operation.")
public class FileUploadResult {

    /**
     * Public UUID of the uploaded file that can be used in subsequent API calls.
     */
    @Schema(description = "Public UUID of the uploaded file.",
            example = "22222222-2222-2222-2222-222222222222",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String publicId;

    /**
     * Original filename provided by the client during upload.
     */
    @Schema(description = "Original filename provided during upload.",
            example = "Maas_Bordrosu_Ekim_2025.xlsx",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String originalName;

    /**
     * File extension in upper case.
     */
    @Schema(description = "File extension in upper case.",
            example = "XLSX",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String extension;

    /**
     * File size in bytes.
     */
    @Schema(description = "File size in bytes.",
            example = "32768",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private long sizeBytes;

    /**
     * Relative storage path pointing to where the file is stored on the backend.
     */
    @Schema(description = "Relative storage path used by the backend to access the file.",
            example = "2025/11/25/xlsx/M/22222222-2222-2222-2222-222222222222.xlsx",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String storagePath;
}
