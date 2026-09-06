package com.tastyhouse.application.file.port.in;

import com.tastyhouse.application.shared.marker.CeoApp;
import org.springframework.web.multipart.MultipartFile;

@CeoApp
public interface FileUploadOwnerCommandUseCase {

    Long upload(MultipartFile file);
}
