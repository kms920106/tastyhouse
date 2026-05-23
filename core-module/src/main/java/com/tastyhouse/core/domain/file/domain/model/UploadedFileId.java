package com.tastyhouse.core.domain.file.domain.model;

public record UploadedFileId(Long value) {

    public UploadedFileId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("UploadedFileId는 양수여야 합니다: " + value);
        }
    }
}
