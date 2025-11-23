package com.fileservice.file.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Lightweight representation of a file used for listing in tables or dashboards.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileListItemResponse {

    /**
     * Public identifier of the file.
     */
    private String publicId;

    /**
     * Display name of the file (usually the original name).
     */
    private String name;

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
     * Creation timestamp used in sorted lists or audit views.
     */
    private LocalDateTime createdAt;

    /**
     * Flag indicating whether this file can be previewed inline (image/PDF).
     */
    private boolean canPreview;
}
