package com.tastyhouse.domain.file.event;

import java.time.LocalDateTime;

import com.tastyhouse.domain.file.vo.UploadedFileId;

public record FileUploadedEvent(
    UploadedFileId fileId,
    String filePath,
    String contentType,
    LocalDateTime uploadedAt
) {
}
