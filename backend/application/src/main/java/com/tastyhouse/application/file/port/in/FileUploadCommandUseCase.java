package com.tastyhouse.application.file.port.in;

import com.tastyhouse.application.shared.marker.WebApp;
import org.springframework.web.multipart.MultipartFile;

@WebApp
public interface FileUploadCommandUseCase {

    Long upload(MultipartFile file);
}
