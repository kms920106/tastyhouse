package com.tastyhouse.core.domain.file.application.dto.result;

import com.tastyhouse.core.domain.file.domain.model.UploadedFile;

public record UploadedFileResult(
    Long id,
    String originalFilename,
    String filePath,
    String contentType
) {
    public static UploadedFileResult from(UploadedFile uploadedFile) {
        return new UploadedFileResult(
            uploadedFile.getId(),
            uploadedFile.getOriginalFilename(),
            uploadedFile.getFilePath(),
            uploadedFile.getContentType()
        );
    }
}
