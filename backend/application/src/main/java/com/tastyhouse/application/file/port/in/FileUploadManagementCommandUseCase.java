package com.tastyhouse.application.file.port.in;

import com.tastyhouse.application.shared.marker.AdminApp;
import org.springframework.web.multipart.MultipartFile;

@AdminApp
public interface FileUploadManagementCommandUseCase {

    Long upload(MultipartFile file);
}
