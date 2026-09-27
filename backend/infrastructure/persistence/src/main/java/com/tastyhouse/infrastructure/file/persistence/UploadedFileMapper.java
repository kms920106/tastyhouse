package com.tastyhouse.infrastructure.file.persistence;

import com.tastyhouse.application.file.port.out.write.UploadedFileState;

final class UploadedFileMapper {
    private UploadedFileMapper() {
    }

    static UploadedFileState toState(UploadedFileJpaEntity entity) {
        return new UploadedFileState(
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

    static UploadedFileJpaEntity toEntity(UploadedFileState state) {
        return UploadedFileJpaEntity.create(
            state.originalFilename(),
            state.storedFilename(),
            state.filePath(),
            state.fileSize(),
            state.contentType()
        );
    }
}
