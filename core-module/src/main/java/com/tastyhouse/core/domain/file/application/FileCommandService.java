package com.tastyhouse.core.domain.file.application;

import com.tastyhouse.core.domain.file.application.dto.command.UploadFileCommand;
import com.tastyhouse.core.domain.file.domain.event.FileUploadedEvent;
import com.tastyhouse.core.domain.file.domain.model.UploadedFile;
import com.tastyhouse.core.domain.file.domain.model.UploadedFileId;
import com.tastyhouse.core.domain.file.domain.repository.UploadedFileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class FileCommandService {

    private final UploadedFileRepository uploadedFileRepository;
    private final ApplicationEventPublisher eventPublisher;

    public UploadedFileId save(UploadFileCommand command) {
        UploadedFile saved = uploadedFileRepository.save(
            UploadedFile.of(
                command.originalFilename(),
                command.storedFilename(),
                command.filePath(),
                command.fileSize(),
                command.contentType()
            )
        );
        UploadedFileId fileId = saved.getUploadedFileId();
        eventPublisher.publishEvent(new FileUploadedEvent(
            fileId,
            saved.getFilePath(),
            saved.getContentType(),
            LocalDateTime.now()
        ));
        return fileId;
    }
}
