package com.tastyhouse.core.domain.file.application.dto.command;

public record UploadFileCommand(
    String originalFilename,
    byte[] content,
    Long fileSize,
    String contentType
) {

    public static UploadFileCommand of(
        String originalFilename,
        byte[] content,
        Long fileSize,
        String contentType
    ) {
        return new UploadFileCommand(originalFilename, content, fileSize, contentType);
    }
}
