package com.tastyhouse.domain.file.service;

public record FileUploadCommand(
    String originalFilename,
    byte[] content,
    Long fileSize,
    String contentType
) {
    public static FileUploadCommand of(
        String originalFilename,
        byte[] content,
        Long fileSize,
        String contentType
    ) {
        return new FileUploadCommand(originalFilename, content, fileSize, contentType);
    }
}
