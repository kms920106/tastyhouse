package com.tastyhouse.application.file.port.in;

import org.springframework.web.multipart.MultipartFile;

import com.tastyhouse.application.shared.marker.WebApp;

@WebApp
public interface FileUploadCommandUseCase {

    Long upload(MultipartFile file);
}
