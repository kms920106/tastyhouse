package com.tastyhouse.domain.file.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.UUID;

import com.tastyhouse.domain.file.event.FileUploadedEvent;
import com.tastyhouse.domain.file.model.UploadedFile;
import com.tastyhouse.domain.file.port.FileStoragePort;
import com.tastyhouse.domain.file.repository.UploadedFileRepository;
import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.shared.event.DomainEventPublisher;

public class FileUploadService {
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp", "pdf");
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
        "image/jpeg", "image/png", "image/gif", "image/webp", "application/pdf"
    );
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    private final UploadedFileRepository uploadedFileRepository;
    private final FileStoragePort fileStoragePort;
    private final DomainEventPublisher domainEventPublisher;

    public FileUploadService(
        UploadedFileRepository uploadedFileRepository,
        FileStoragePort fileStoragePort,
        DomainEventPublisher domainEventPublisher
    ) {
        this.uploadedFileRepository = uploadedFileRepository;
        this.fileStoragePort = fileStoragePort;
        this.domainEventPublisher = domainEventPublisher;
    }

    public UploadedFileId upload(FileUploadCommand command) {
        validate(command);

        String extension = extractExtension(command.originalFilename());
        String storedFilename = UUID.randomUUID() + "." + extension;
        String datePath = LocalDate.now().format(DATE_FORMATTER);

        String filePath = fileStoragePort.store(command.content(), storedFilename, datePath, command.contentType());

        UploadedFile saved = uploadedFileRepository.save(UploadedFile.of(
            command.originalFilename(),
            storedFilename,
            filePath,
            command.fileSize(),
            command.contentType()
        ));

        UploadedFileId fileId = saved.getUploadedFileId();
        domainEventPublisher.publish(new FileUploadedEvent(
            fileId,
            saved.getFilePath(),
            saved.getContentType(),
            LocalDateTime.now()
        ));
        return fileId;
    }

    private void validate(FileUploadCommand command) {
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
