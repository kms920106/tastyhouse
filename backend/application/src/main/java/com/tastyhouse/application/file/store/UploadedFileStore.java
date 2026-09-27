package com.tastyhouse.application.file.store;

import java.util.Optional;

import com.tastyhouse.domain.file.model.UploadedFile;
import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.application.file.port.out.write.UploadedFileStatePort;

public class UploadedFileStore implements UploadedFileRepository {
    private final UploadedFileStatePort uploadedFileStatePort;

    public UploadedFileStore(UploadedFileStatePort uploadedFileStatePort) {
        this.uploadedFileStatePort = uploadedFileStatePort;
    }

    @Override
    public UploadedFile save(UploadedFile uploadedFile) {
        return UploadedFileStateMapper.toDomain(
            uploadedFileStatePort.save(UploadedFileStateMapper.toState(uploadedFile)));
    }

    @Override
    public Optional<UploadedFile> findById(UploadedFileId id) {
        return uploadedFileStatePort.findById(id.value()).map(UploadedFileStateMapper::toDomain);
    }
}
