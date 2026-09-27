package com.tastyhouse.application.file.port.in;

import org.springframework.web.multipart.MultipartFile;

import com.tastyhouse.application.shared.marker.AdminApp;

@AdminApp
public interface FileUploadManagementCommandUseCase {

    Long upload(MultipartFile file);
}
