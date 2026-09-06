package com.tastyhouse.infrastructure.file.persistence;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.file.model.UploadedFile;
import com.tastyhouse.domain.file.repository.UploadedFileRepository;
import com.tastyhouse.domain.file.vo.UploadedFileId;

@Repository
public class UploadedFileRepositoryImpl implements UploadedFileRepository {
    private final UploadedFileJpaRepository uploadedFileJpaRepository;

    public UploadedFileRepositoryImpl(UploadedFileJpaRepository uploadedFileJpaRepository) {
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
