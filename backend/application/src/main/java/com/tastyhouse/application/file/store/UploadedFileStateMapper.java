package com.tastyhouse.application.file.store;

import com.tastyhouse.domain.file.model.UploadedFile;
import com.tastyhouse.application.file.port.out.write.UploadedFileState;

final class UploadedFileStateMapper {
    private UploadedFileStateMapper() {
    }

    static UploadedFile toDomain(UploadedFileState state) {
        return UploadedFile.reconstitute(
            state.id(),
            state.originalFilename(),
            state.storedFilename(),
            state.filePath(),
            state.fileSize(),
            state.contentType(),
            state.createdAt(),
            state.updatedAt()
        );
    }

    static UploadedFileState toState(UploadedFile uploadedFile) {
        return new UploadedFileState(
            uploadedFile.getId(),
            uploadedFile.getOriginalFilename(),
            uploadedFile.getStoredFilename(),
            uploadedFile.getFilePath(),
            uploadedFile.getFileSize(),
            uploadedFile.getContentType(),
            uploadedFile.getCreatedAt(),
            uploadedFile.getUpdatedAt()
        );
    }
}
