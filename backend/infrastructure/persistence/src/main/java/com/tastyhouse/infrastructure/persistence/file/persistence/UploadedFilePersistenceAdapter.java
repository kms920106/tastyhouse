package com.tastyhouse.infrastructure.persistence.file.persistence;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.file.model.UploadedFile;
import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.application.file.port.out.write.UploadedFileLoadPort;
import com.tastyhouse.application.file.port.out.write.UploadedFileSavePort;

@Repository
class UploadedFilePersistenceAdapter implements UploadedFileLoadPort, UploadedFileSavePort {

    private final UploadedFileJpaRepository uploadedFileJpaRepository;

    public UploadedFilePersistenceAdapter(UploadedFileJpaRepository uploadedFileJpaRepository) {
        this.uploadedFileJpaRepository = uploadedFileJpaRepository;
    }

    @Override
    public UploadedFile save(UploadedFile uploadedFile) {
        UploadedFileJpaEntity saved = uploadedFileJpaRepository.save(UploadedFileMapper.toEntity(uploadedFile));
        return UploadedFileMapper.toDomain(saved);
    }

    @Override
    public Optional<UploadedFile> findById(UploadedFileId id) {
        return uploadedFileJpaRepository.findById(id.value()).map(UploadedFileMapper::toDomain);
    }
}
