package com.tastyhouse.domain.file.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.file.vo.UploadedFileId;

public class UploadedFile {
    private final Long id;
    private final String originalFilename;
    private final String storedFilename;
    private final String filePath;
    private final Long fileSize;
    private final String contentType;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private UploadedFile(
        Long id,
        String originalFilename,
        String storedFilename,
        String filePath,
        Long fileSize,
        String contentType,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.originalFilename = originalFilename;
        this.storedFilename = storedFilename;
        this.filePath = filePath;
        this.fileSize = fileSize;
        this.contentType = contentType;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static UploadedFile of(
        String originalFilename,
        String storedFilename,
        String filePath,
        Long fileSize,
        String contentType
    ) {
        return new UploadedFile(null, originalFilename, storedFilename, filePath, fileSize, contentType, null, null);
    }

    public static UploadedFile reconstitute(
        Long id,
        String originalFilename,
        String storedFilename,
        String filePath,
        Long fileSize,
        String contentType,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new UploadedFile(id, originalFilename, storedFilename, filePath, fileSize, contentType, createdAt, updatedAt);
    }

    public UploadedFileId getUploadedFileId() {
        return UploadedFileId.of(this.id);
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

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }
}
