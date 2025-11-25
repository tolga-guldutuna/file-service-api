package com.fileservice.file.service.impl;

import com.fileservice.auth.pojo.entity.User;
import com.fileservice.auth.dao.UserDao;
import com.fileservice.common.exception.BusinessException;
import com.fileservice.file.dao.FileDao;
import com.fileservice.file.pojo.dto.FileListItemResponse;
import com.fileservice.file.pojo.dto.FileMetadataResponse;
import com.fileservice.file.pojo.dto.FileUploadResult;
import com.fileservice.file.pojo.entity.FileEntity;
import com.fileservice.file.service.FileService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * Default implementation of {@link FileService} that stores files on the local
 * filesystem and persists metadata in the {@code files} table.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final FileDao fileDao;
    private final UserDao userDao;

    /**
     * Root directory where uploaded files are stored.
     * <p>
     * Example: {@code ./data/files}
     */
    @Value("${file-storage.root-directory:./data/files}")
    private String rootDirectory;

    @Transactional
    @Override
    public FileUploadResult uploadFile(Long ownerId, MultipartFile multipartFile) {
        if (multipartFile.isEmpty()) {
            throw new BusinessException("Uploaded file must not be empty");
        }

        User owner = userDao.findById(ownerId)
                .orElseThrow(() -> new BusinessException("Owner not found"));

        String originalFilename = multipartFile.getOriginalFilename();
        String extension = resolveExtension(originalFilename);
        String publicId = UUID.randomUUID().toString();
        String storedName = publicId + "." + extension.toLowerCase();
        String contentType = multipartFile.getContentType() != null
                ? multipartFile.getContentType()
                : "application/octet-stream";
        long sizeBytes = multipartFile.getSize();

        // Build storage path: yyyy/MM/dd/<ext>/<first-letter>/<uuid.ext>
        String storagePath = buildStoragePath(extension, storedName);

        // Write file to disk
        Path target = Path.of(rootDirectory, storagePath);
        try {
            Files.createDirectories(target.getParent());
            multipartFile.transferTo(target);
        } catch (IOException e) {
            log.error("Failed to store file on disk", e);
            throw new BusinessException("Failed to store file on disk");
        }

        FileEntity entity = new FileEntity();
        entity.setPublicId(publicId);
        entity.setOwner(owner);
        entity.setOriginalName(originalFilename != null ? originalFilename : storedName);
        entity.setStoredName(storedName);
        entity.setExtension(extension.toUpperCase());
        entity.setContentType(contentType);
        entity.setSizeBytes(sizeBytes);
        entity.setStoragePath(storagePath);
        entity.setTemp(false); // default for now
        entity.setExpiresAt(null);
        entity.setSha256Hash(null); // can be filled later if hashing is added

        FileEntity saved = fileDao.save(entity);

        return FileUploadResult.builder()
                .publicId(saved.getPublicId())
                .originalName(saved.getOriginalName())
                .extension(saved.getExtension())
                .sizeBytes(saved.getSizeBytes())
                .storagePath(saved.getStoragePath())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<FileListItemResponse> listFiles(Long ownerId) {
        return fileDao.findByOwnerIdAndDeletedAtIsNullOrderByCreatedAtDesc(ownerId)
                .stream()
                .map(this::toListItem)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public FileMetadataResponse getFileMetadata(String publicId) {
        FileEntity entity = fileDao.findByPublicIdAndDeletedAtIsNull(publicId)
                .orElseThrow(() -> new BusinessException("File not found"));

        return toMetadata(entity);
    }

    @Override
    @Transactional
    public void deleteFile(Long ownerId, String publicId) {
        FileEntity entity = fileDao.findByPublicIdAndDeletedAtIsNull(publicId)
                .orElseThrow(() -> new BusinessException("File not found"));

        if (!entity.getOwner().getId().equals(ownerId)) {
            throw new BusinessException("You are not allowed to delete this file");
        }

        entity.setDeletedAt(Instant.now());
        fileDao.save(entity);
        // Physical delete can be implemented later if required
    }

    // =====================
    // Mapping helpers
    // =====================

    private FileListItemResponse toListItem(FileEntity entity) {
        return FileListItemResponse.builder()
                .publicId(entity.getPublicId())
                .originalName(entity.getOriginalName())
                .extension(entity.getExtension())
                .contentType(entity.getContentType())
                .sizeBytes(entity.getSizeBytes())
                .createdAt(entity.getCreatedAt())
                .temp(entity.isTemp())
                .build();
    }

    private FileMetadataResponse toMetadata(FileEntity entity) {
        return FileMetadataResponse.builder()
                .publicId(entity.getPublicId())
                .originalName(entity.getOriginalName())
                .storedName(entity.getStoredName())
                .extension(entity.getExtension())
                .contentType(entity.getContentType())
                .sizeBytes(entity.getSizeBytes())
                .storagePath(entity.getStoragePath())
                .sha256Hash(entity.getSha256Hash())
                .temp(entity.isTemp())
                .expiresAt(entity.getExpiresAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .deletedAt(entity.getDeletedAt())
                .build();
    }

    // =====================
    // Utility helpers
    // =====================

    /**
     * Resolves the file extension from the original filename.
     */
    private String resolveExtension(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            return "BIN";
        }
        String ext = originalFilename.substring(originalFilename.lastIndexOf('.') + 1);
        return ext.isEmpty() ? "BIN" : ext;
    }

    /**
     * Builds a relative storage path according to the agreed folder structure:
     * {@code yyyy/MM/dd/<ext-lowercase>/<first-letter-upper>/<storedName>}.
     */
    private String buildStoragePath(String extension, String storedName) {
        LocalDate today = LocalDate.now();
        String extLower = extension.toLowerCase();
        char firstLetter = Character.toUpperCase(storedName.charAt(0));

        return String.format(
                "%04d/%02d/%02d/%s/%c/%s",
                today.getYear(),
                today.getMonthValue(),
                today.getDayOfMonth(),
                extLower,
                firstLetter,
                storedName
        );
    }
}
