package com.tastyhouse.core.domain.file.application.dto.command;

public record UploadFileCommand(
    String originalFilename,
    String storedFilename,
    String filePath,
    Long fileSize,
    String contentType
) {
}
