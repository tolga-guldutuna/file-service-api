package com.fileservice.file.dao;

import com.fileservice.file.pojo.entity.FileEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Data access abstraction for {@link FileEntity} entities.
 * <p>
 * Provides finder methods based on the public UUID, owner and basic search by
 * original file name. The primary key (ID) remains an internal technical detail.
 */
@Repository
public interface FileDao extends JpaRepository<FileEntity, Long> {

    /**
     * Returns a file by its public UUID, which is the identifier exposed in URLs.
     *
     * @param publicId public UUID of the file
     * @return an {@link Optional} containing the file, or empty if none found
     */
    Optional<FileEntity> findByPublicId(UUID publicId);

    /**
     * Returns all files belonging to the given owner ordered by creation time
     * in descending order (newest first).
     *
     * @param ownerId {@code users.id} of the file owner
     * @return list of files owned by the user
     */
    List<FileEntity> findAllByOwnerIdOrderByCreatedAtDesc(Long ownerId);

    /**
     * Searches files for a given owner using a case-insensitive substring match
     * on the original file name. Results are returned as a pageable slice.
     *
     * @param ownerId     {@code users.id} of the file owner
     * @param namePart    substring to match against {@code originalName}
     * @param pageable    pagination information (page number, size, sorting)
     * @return a page of matching files
     */
    Page<FileEntity> findByOwnerIdAndOriginalNameContainingIgnoreCase(
            Long ownerId,
            String namePart,
            Pageable pageable
    );
}
