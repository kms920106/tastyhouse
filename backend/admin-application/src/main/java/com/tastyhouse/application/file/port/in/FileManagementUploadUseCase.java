package com.tastyhouse.application.file.port.in;

import org.springframework.web.multipart.MultipartFile;

public interface FileManagementUploadUseCase {

    Long upload(MultipartFile file);
}
