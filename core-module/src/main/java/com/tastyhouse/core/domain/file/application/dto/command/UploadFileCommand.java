package com.tastyhouse.core.domain.file.application.dto.command;

public record UploadFileCommand(
    String originalFilename,
    String storedFilename,
    String filePath,
    Long fileSize,
    String contentType
) {

    public static UploadFileCommand of(
        String originalFilename,
        String storedFilename,
        String filePath,
        Long fileSize,
        String contentType
    ) {
        return new UploadFileCommand(originalFilename, storedFilename, filePath, fileSize, contentType);
    }
}
