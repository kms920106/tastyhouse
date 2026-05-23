package com.tastyhouse.core.domain.file.domain.repository;

import com.tastyhouse.core.domain.file.domain.model.UploadedFile;
import com.tastyhouse.core.domain.file.domain.model.UploadedFileId;

import java.util.List;
import java.util.Optional;

public interface UploadedFileRepository {

    UploadedFile save(UploadedFile uploadedFile);

    Optional<UploadedFile> findById(Long id);

    Optional<UploadedFile> findById(UploadedFileId id);

    List<UploadedFile> findByIds(List<Long> ids);
}
