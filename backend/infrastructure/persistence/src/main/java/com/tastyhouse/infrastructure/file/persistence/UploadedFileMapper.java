package com.tastyhouse.infrastructure.file.persistence;

import com.tastyhouse.domain.file.model.UploadedFile;

final class UploadedFileMapper {
    private UploadedFileMapper() {
    }

    static UploadedFile toDomain(UploadedFileJpaEntity entity) {
        return UploadedFile.reconstitute(
            entity.getId(),
            entity.getOriginalFilename(),
            entity.getStoredFilename(),
            entity.getFilePath(),
            entity.getFileSize(),
            entity.getContentType(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static UploadedFileJpaEntity toEntity(UploadedFile domain) {
        return UploadedFileJpaEntity.create(
            domain.getOriginalFilename(),
            domain.getStoredFilename(),
            domain.getFilePath(),
            domain.getFileSize(),
            domain.getContentType()
        );
    }
}
