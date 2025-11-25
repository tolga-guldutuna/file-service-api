package com.fileservice.file.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Lightweight representation of a file used for listing in tables or dashboards.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "File list item view used in dashboard tables.")
public class FileListItemResponse {

    /**
     * Public identifier of the file.
     */
    @Schema(description = "Public UUID that uniquely identifies the file.", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private String publicId;

    /**
     * Display name of the file (usually the original name).
     */
    @Schema(description = "Human readable file name that is shown in the UI.", example = "Project_Proposal.pdf")
    private String name;

    /**
     * File extension (e.g. PDF, PNG, DOCX).
     */
    @Schema(description = "File extension in upper-case.", example = "PDF")
    private String extension;

    /**
     * MIME type of the file.
     */
    @Schema(description = "MIME content type of the file.", example = "application/pdf")
    private String contentType;

    /**
     * Size of the file in bytes.
     */
    @Schema(description = "File size in bytes.", example = "1200000")
    private long sizeBytes;

    /**
     * Creation timestamp used in sorted lists or audit views.
     */
    @Schema(description = "Creation timestamp of the file metadata.", example = "2024-11-23T10:15:30")
    private LocalDateTime createdAt;

    /**
     * Flag indicating whether this file can be previewed inline (image/PDF).
     */
    @Schema(description = "Indicates whether the file type can be previewed in the UI (PDF, PNG, JPG).", example = "true")
    private boolean canPreview;
}
