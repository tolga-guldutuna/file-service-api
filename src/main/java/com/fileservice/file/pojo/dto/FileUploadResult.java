package com.fileservice.file.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO returned after a successful file upload operation.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileUploadResult {

    /**
     * Public identifier of the uploaded file.
     */
    private String publicId;

    /**
     * Original filename as uploaded by the client.
     */
    private String originalName;

    /**
     * File extension (e.g. PDF, PNG, DOCX).
     */
    private String extension;

    /**
     * Size of the file in bytes.
     */
    private long sizeBytes;

    /**
     * Optional direct download URL.
     * <p>
     * The API may choose to return a pre-built URL for convenience.
     */
    private String downloadUrl;
}
