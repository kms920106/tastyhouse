package com.tastyhouse.infrastructure.file.persistence;

import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.tastyhouse.core.domain.file.domain.model.UploadedFile;
import com.tastyhouse.core.domain.file.domain.repository.UploadedFileRepository;
import com.tastyhouse.core.domain.file.domain.vo.UploadedFileId;

@Repository
@RequiredArgsConstructor
public class UploadedFileRepositoryImpl implements UploadedFileRepository {

    private final UploadedFileJpaRepository uploadedFileJpaRepository;

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
