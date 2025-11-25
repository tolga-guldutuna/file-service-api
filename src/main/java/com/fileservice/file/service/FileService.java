package com.fileservice.file.service;

import com.fileservice.file.pojo.dto.FileListItemResponse;
import com.fileservice.file.pojo.dto.FileMetadataResponse;
import com.fileservice.file.pojo.dto.FileUploadResult;
import java.util.UUID;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service responsible for managing files and their metadata.
 * <p>
 * This service encapsulates both persistence (database) and physical
 * storage (file system) concerns behind a clean API.
 */
public interface FileService {

    /**
     * Stores a new file for the given owner.
     *
     * @param file    multipart file received from the client
     * @param ownerId identifier of the user who uploads the file
     * @return a {@link FileUploadResult} containing basic information and a download URL
     */
    FileUploadResult uploadFile(MultipartFile file, Long ownerId);

    /**
     * Returns a paged list of files owned by the given user.
     *
     * @param ownerId  identifier of the file owner
     * @param pageable pagination information
     * @return a page of {@link FileListItemResponse}
     */
    Page<FileListItemResponse> listFilesForOwner(Long ownerId, Pageable pageable);

    /**
     * Returns detailed metadata for a single file identified by its public UUID
     * and owned by the given user.
     *
     * @param publicId public UUID of the file
     * @param ownerId  identifier of the owner
     * @return a {@link FileMetadataResponse}
     */
    FileMetadataResponse getFileMetadata(UUID publicId, Long ownerId);

    /**
     * Loads the physical file as a Spring {@link Resource} for download.
     *
     * @param publicId public UUID of the file
     * @param ownerId  identifier of the owner
     * @return file content wrapped as a {@link Resource}
     */
    Resource loadFileAsResource(UUID publicId, Long ownerId);

    /**
     * Performs a logical delete of the file (soft delete).
     *
     * @param publicId public UUID of the file
     * @param ownerId  identifier of the owner
     */
    void deleteFile(UUID publicId, Long ownerId);
}
