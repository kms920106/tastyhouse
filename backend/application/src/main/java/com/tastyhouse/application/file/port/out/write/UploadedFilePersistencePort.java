package com.tastyhouse.application.file.port.out.write;

import java.util.Optional;

import com.tastyhouse.domain.file.model.UploadedFile;
import com.tastyhouse.domain.file.vo.UploadedFileId;

public interface UploadedFilePersistencePort {
    UploadedFile save(UploadedFile uploadedFile);

    Optional<UploadedFile> findById(UploadedFileId id);
}
