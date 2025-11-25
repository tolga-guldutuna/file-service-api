package com.fileservice.file.pojo.entity;

import com.fileservice.auth.pojo.entity.User;
import jakarta.persistence.*;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * JPA entity that maps the {@code files} table.
 * <p>
 * Each row represents a single stored file owned by a {@link User}.
 */
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = "owner")
@Entity
@Table(name = "files",
        indexes = {
                @Index(name = "idx_files_created_at", columnList = "created_at"),
                @Index(name = "idx_files_public_id", columnList = "public_id"),
                @Index(name = "idx_files_extension", columnList = "extension"),
                @Index(name = "idx_files_owner", columnList = "owner_id")
        }
)
public class FileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Public UUID used in URLs instead of the internal numeric id.
     */
    @Column(name = "public_id", nullable = false, unique = true, length = 255)
    private String publicId;

    /**
     * Owning user of this file.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    /**
     * Original filename as sent by the client during upload.
     */
    @Column(name = "original_name", nullable = false, length = 255)
    private String originalName;

    /**
     * Physical filename used on disk, typically &lt;UUID&gt;.&lt;ext&gt;.
     */
    @Column(name = "stored_name", nullable = false, length = 255)
    private String storedName;

    /**
     * File extension (PDF, PNG, DOCX...).
     */
    @Column(name = "extension", nullable = false, length = 10)
    private String extension;

    /**
     * MIME content type (application/pdf, image/png, ...).
     */
    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    /**
     * File size in bytes.
     */
    @Column(name = "size_bytes", nullable = false)
    private long sizeBytes;

    /**
     * Relative storage path, e.g. {@code 2025/11/25/pdf/K/uuid.pdf}.
     */
    @Column(name = "storage_path", nullable = false, length = 500)
    private String storagePath;

    /**
     * Optional SHA-256 hash of the file contents.
     */
    @Column(name = "sha256_hash", length = 64)
    private String sha256Hash;

    /**
     * Flag indicating whether this file is temporary.
     */
    @Column(name = "is_temp", nullable = false)
    private boolean temp;

    /**
     * Optional expiration timestamp for temporary files.
     */
    @Column(name = "expires_at")
    private Instant expiresAt;

    /**
     * Creation timestamp.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * Last update timestamp.
     */
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /**
     * Soft-delete marker. When not null, the file is considered deleted.
     */
    @Column(name = "deleted_at")
    private Instant deletedAt;

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    /**
     * Convenience method to check if the file is soft-deleted.
     */
    public boolean isDeleted() {
        return deletedAt != null;
    }
}
