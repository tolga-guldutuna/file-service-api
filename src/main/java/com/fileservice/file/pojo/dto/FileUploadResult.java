package com.fileservice.file.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO returned after a successful file upload operation.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Response returned after a file has been uploaded successfully.")
public class FileUploadResult {

    /**
     * Public identifier of the uploaded file.
     */
    @Schema(description = "Public UUID that uniquely identifies the uploaded file.",
            example = "3fa85f64-5717-4562-b3fc-2c963f66afa6",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String publicId;

    /**
     * Original filename as uploaded by the client.
     */
    @Schema(description = "Original filename as provided by the client.",
            example = "Meeting_Notes_Q4_2024.pdf",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String originalName;

    /**
     * File extension (e.g. PDF, PNG, DOCX).
     */
    @Schema(description = "Upper-case file extension.",
            example = "PDF",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String extension;

    /**
     * Size of the file in bytes.
     */
    @Schema(description = "Size of the file in bytes.",
            example = "1258291",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private long sizeBytes;

    /**
     * Optional direct download URL.
     * <p>
     * The API may choose to return a pre-built URL for convenience.
     */
    @Schema(description = "MIME content type of the uploaded file.", example = "application/pdf")
    private String downloadUrl;
}
