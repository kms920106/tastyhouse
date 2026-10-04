package com.tastyhouse.application.file.port.in;

import org.springframework.web.multipart.MultipartFile;

public interface FileUploadOwnerCommandUseCase {

    Long upload(MultipartFile file);
}
