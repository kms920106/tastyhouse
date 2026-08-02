package com.tastyhouse.domain.file.vo;

public record UploadedFileId(Long value) {

    public UploadedFileId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("UploadedFileId는 양수여야 합니다: " + value);
        }
    }

    public static UploadedFileId of(Long value) {
        return new UploadedFileId(value);
    }
}
