package com.tastyhouse.core.domain.file.domain.event;

import java.time.LocalDateTime;

import com.tastyhouse.core.domain.file.domain.vo.UploadedFileId;

public record FileUploadedEvent(
    UploadedFileId fileId,
    String filePath,
    String contentType,
    LocalDateTime uploadedAt
) {
}
