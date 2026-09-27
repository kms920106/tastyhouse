package com.tastyhouse.application.file.port.in;

import org.springframework.web.multipart.MultipartFile;

import com.tastyhouse.application.shared.marker.CeoApp;

@CeoApp
public interface FileUploadOwnerCommandUseCase {

    Long upload(MultipartFile file);
}
