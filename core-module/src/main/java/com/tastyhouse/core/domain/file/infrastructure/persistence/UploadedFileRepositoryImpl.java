package com.tastyhouse.core.domain.file.infrastructure.persistence;

import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.tastyhouse.core.domain.file.domain.model.UploadedFile;
import com.tastyhouse.core.domain.file.domain.model.UploadedFileId;
import com.tastyhouse.core.domain.file.domain.repository.UploadedFileRepository;

@Repository
@RequiredArgsConstructor
public class UploadedFileRepositoryImpl implements UploadedFileRepository {

    private final UploadedFileJpaRepository uploadedFileJpaRepository;

    @Override
    public UploadedFile save(UploadedFile file) {
        return uploadedFileJpaRepository.save(file);
    }

    @Override
    public Optional<UploadedFile> findById(Long id) {
        return uploadedFileJpaRepository.findById(id);
    }

    @Override
    public Optional<UploadedFile> findById(UploadedFileId id) {
        return uploadedFileJpaRepository.findById(id.value());
    }
}
