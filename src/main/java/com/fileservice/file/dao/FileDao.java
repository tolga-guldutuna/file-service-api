package com.fileservice.file.dao;

import com.fileservice.file.pojo.entity.FileEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * DAO for {@link FileEntity} providing type-safe access to the {@code files} table.
 */
@Repository
public interface FileDao extends JpaRepository<FileEntity, Long> {

    /**
     * Finds a file by its public identifier while ignoring soft-deleted rows.
     */
    Optional<FileEntity> findByPublicIdAndDeletedAtIsNull(String publicId);

    /**
     * Returns all non-deleted files for a given owner.
     */
    List<FileEntity> findByOwnerIdAndDeletedAtIsNullOrderByCreatedAtDesc(Long ownerId);
}
