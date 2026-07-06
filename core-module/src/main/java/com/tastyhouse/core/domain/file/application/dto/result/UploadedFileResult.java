package com.tastyhouse.core.domain.file.application.dto.result;

import com.tastyhouse.core.domain.file.domain.model.UploadedFile;
import com.tastyhouse.core.domain.file.domain.vo.UploadedFileId;

public record UploadedFileResult(
    UploadedFileId id,
    String originalFilename,
    String filePath,
    String contentType
) {
    public static UploadedFileResult from(UploadedFile uploadedFile) {
        return new UploadedFileResult(
            uploadedFile.getUploadedFileId(),
            uploadedFile.getOriginalFilename(),
            uploadedFile.getFilePath(),
            uploadedFile.getContentType()
        );
    }
}
