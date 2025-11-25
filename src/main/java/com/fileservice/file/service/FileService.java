package com.fileservice.file.service;

import com.fileservice.file.pojo.dto.FileListItemResponse;
import com.fileservice.file.pojo.dto.FileMetadataResponse;
import com.fileservice.file.pojo.dto.FileUploadResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Business service exposing high-level file operations used by controllers.
 */
public interface FileService {

    /**
     * Stores the given multipart file on disk and persists its metadata.
     *
     * @param ownerId id of the authenticated user uploading the file
     * @param file    file payload sent by the client
     * @return {@link FileUploadResult} describing the stored file
     */
    FileUploadResult uploadFile(Long ownerId, MultipartFile file);

    /**
     * Returns a read-only list of files owned by the given user.
     *
     * @param ownerId id of the authenticated user
     * @return list of lightweight file representations
     */
    List<FileListItemResponse> listFiles(Long ownerId);

    /**
     * Returns detailed metadata for the file identified by the given public id.
     *
     * @param publicId public UUID of the file
     * @return detailed metadata
     */
    FileMetadataResponse getFileMetadata(String publicId);

    /**
     * Marks the file identified by the given public id as soft-deleted.
     *
     * @param ownerId  id of the authenticated user
     * @param publicId public UUID of the file
     */
    void deleteFile(Long ownerId, String publicId);
}
