package com.tastyhouse.core.domain.file.domain.event;

import com.tastyhouse.core.domain.file.domain.model.UploadedFileId;

import java.time.LocalDateTime;

public record FileUploadedEvent(
    UploadedFileId fileId,
    String filePath,
    String contentType,
    LocalDateTime uploadedAt
) {
}
