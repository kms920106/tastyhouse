package com.tastyhouse.core.repository.file;

import com.tastyhouse.core.entity.file.UploadedFile;

import java.util.List;
import java.util.Optional;

public interface UploadedFileRepository {

    UploadedFile save(UploadedFile uploadedFile);

    Optional<UploadedFile> findById(Long id);

    List<UploadedFile> findByIds(List<Long> ids);
}
