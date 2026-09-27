package com.tastyhouse.application.file.port.out.write;

import java.time.LocalDateTime;

public record UploadedFileState(
    Long id,
    String originalFilename,
    String storedFilename,
    String filePath,
    Long fileSize,
    String contentType,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
