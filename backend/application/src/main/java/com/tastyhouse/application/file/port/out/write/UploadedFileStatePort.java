package com.tastyhouse.application.file.port.out.write;

import java.util.Optional;

public interface UploadedFileStatePort {
    UploadedFileState save(UploadedFileState state);

    Optional<UploadedFileState> findById(Long id);
}
