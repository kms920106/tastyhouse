package com.tastyhouse.infrastructure.file.persistence;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.application.file.port.out.write.UploadedFileState;
import com.tastyhouse.application.file.port.out.write.UploadedFileStatePort;

@Repository
public class UploadedFileStatePortImpl implements UploadedFileStatePort {
    private final UploadedFileJpaRepository uploadedFileJpaRepository;

    public UploadedFileStatePortImpl(UploadedFileJpaRepository uploadedFileJpaRepository) {
        this.uploadedFileJpaRepository = uploadedFileJpaRepository;
    }

    @Override
    public UploadedFileState save(UploadedFileState state) {
        UploadedFileJpaEntity saved = uploadedFileJpaRepository.save(UploadedFileMapper.toEntity(state));
        return UploadedFileMapper.toState(saved);
    }

    @Override
    public Optional<UploadedFileState> findById(Long id) {
        return uploadedFileJpaRepository.findById(id).map(UploadedFileMapper::toState);
    }
}
