package com.tastyhouse.core.domain.file.application;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.core.domain.file.domain.event.FileUploadedEvent;
import com.tastyhouse.core.domain.file.domain.model.UploadedFile;
import com.tastyhouse.core.domain.file.domain.repository.UploadedFileRepository;
import com.tastyhouse.core.domain.file.domain.vo.UploadedFileId;
import com.tastyhouse.core.domain.file.application.dto.command.UploadFileCommand;
import com.tastyhouse.core.domain.file.application.port.out.FileStoragePort;
import com.tastyhouse.core.exception.BusinessException;
import com.tastyhouse.core.exception.ErrorCode;

@Service
@Transactional
@RequiredArgsConstructor
public class FileCommandService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
        "image/jpeg", "image/png", "image/gif", "image/webp"
    );
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    private final UploadedFileRepository uploadedFileRepository;
    private final FileStoragePort fileStoragePort;
    private final ApplicationEventPublisher eventPublisher;

    public UploadedFileId upload(UploadFileCommand command) {
        validate(command);

        String extension = extractExtension(command.originalFilename());
        String storedFilename = UUID.randomUUID() + "." + extension;
        String datePath = LocalDate.now().format(DATE_FORMATTER);

        String filePath = fileStoragePort.store(command.content(), storedFilename, datePath, command.contentType());

        return save(command.originalFilename(), storedFilename, filePath, command.fileSize(), command.contentType());
    }

    private UploadedFileId save(
        String originalFilename,
        String storedFilename,
        String filePath,
        Long fileSize,
        String contentType
    ) {
        UploadedFile saved = uploadedFileRepository.save(
            UploadedFile.of(originalFilename, storedFilename, filePath, fileSize, contentType)
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

    private void validate(UploadFileCommand command) {
        if (command.content() == null || command.content().length == 0) {
            throw new BusinessException(ErrorCode.FILE_EMPTY);
        }

        if (command.fileSize() > MAX_FILE_SIZE) {
            throw new BusinessException(ErrorCode.FILE_SIZE_EXCEEDED);
        }

        String contentType = command.contentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new BusinessException(ErrorCode.FILE_TYPE_NOT_ALLOWED);
        }

        String extension = extractExtension(command.originalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException(ErrorCode.FILE_EXTENSION_NOT_ALLOWED);
        }
    }

    private String extractExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            throw new BusinessException(ErrorCode.FILE_EXTENSION_UNKNOWN);
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }
}
