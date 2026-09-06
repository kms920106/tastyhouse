package com.tastyhouse.domain.file.repository;

import java.util.Optional;

import com.tastyhouse.domain.file.model.UploadedFile;
import com.tastyhouse.domain.file.vo.UploadedFileId;

public interface UploadedFileRepository {
    UploadedFile save(UploadedFile uploadedFile);

    Optional<UploadedFile> findById(UploadedFileId id);
}
