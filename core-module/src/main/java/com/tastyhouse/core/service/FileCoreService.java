package com.tastyhouse.core.service;

import com.tastyhouse.core.entity.file.UploadedFile;
import com.tastyhouse.core.exception.EntityNotFoundException;
import com.tastyhouse.core.exception.ErrorCode;
import com.tastyhouse.core.repository.file.UploadedFileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileCoreService {

    private final UploadedFileRepository uploadedFileRepository;

    @Transactional
    public UploadedFile save(UploadedFile uploadedFile) {
        return uploadedFileRepository.save(uploadedFile);
    }

    @Transactional(readOnly = true)
    public UploadedFile findById(Long id) {
        return uploadedFileRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.FILE_NOT_FOUND));
    }
}
