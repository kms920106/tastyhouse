package com.tastyhouse.core.domain.file.domain.repository;

import java.util.Optional;

import com.tastyhouse.core.domain.file.domain.model.UploadedFile;
import com.tastyhouse.core.domain.file.domain.model.UploadedFileId;

public interface UploadedFileRepository {

    UploadedFile save(UploadedFile uploadedFile);

    Optional<UploadedFile> findById(Long id);

    Optional<UploadedFile> findById(UploadedFileId id);
}
