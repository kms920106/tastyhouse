package com.tastyhouse.infrastructure.file.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

@Entity
@Table(
    name = "UPLOADED_FILE",
    indexes = {
        @Index(name = "idx_uploaded_file_created_at", columnList = "created_at")
    }
)
public class UploadedFileJpaEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "original_filename", nullable = false, length = 500)
    private String originalFilename;

    @Column(name = "stored_filename", nullable = false, length = 500)
    private String storedFilename;

    @Column(name = "file_path", nullable = false, length = 1000)
    private String filePath;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    protected UploadedFileJpaEntity() {
    }

    private UploadedFileJpaEntity(
        String originalFilename,
        String storedFilename,
        String filePath,
        Long fileSize,
        String contentType
    ) {
        this.originalFilename = originalFilename;
        this.storedFilename = storedFilename;
        this.filePath = filePath;
        this.fileSize = fileSize;
        this.contentType = contentType;
    }

    static UploadedFileJpaEntity create(
        String originalFilename,
        String storedFilename,
        String filePath,
        Long fileSize,
        String contentType
    ) {
        return new UploadedFileJpaEntity(originalFilename, storedFilename, filePath, fileSize, contentType);
    }

    public Long getId() {
        return this.id;
    }

    public String getOriginalFilename() {
        return this.originalFilename;
    }

    public String getStoredFilename() {
        return this.storedFilename;
    }

    public String getFilePath() {
        return this.filePath;
    }

    public Long getFileSize() {
        return this.fileSize;
    }

    public String getContentType() {
        return this.contentType;
    }
}
