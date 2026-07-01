package com.tastyhouse.core.domain.file.application;

import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.core.domain.file.domain.model.UploadedFile;
import com.tastyhouse.core.domain.file.domain.model.UploadedFileId;
import com.tastyhouse.core.domain.file.domain.repository.UploadedFileRepository;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FileQueryService {

    private final UploadedFileRepository uploadedFileRepository;

    public Optional<UploadedFile> findById(Long id) {
        return uploadedFileRepository.findById(id);
    }

    public Optional<UploadedFile> findById(UploadedFileId id) {
        return uploadedFileRepository.findById(id);
    }

    public Optional<String> findFilePath(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return uploadedFileRepository.findById(id)
            .map(UploadedFile::getFilePath);
    }
}
