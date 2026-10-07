package com.tastyhouse.application.file.port.in;

import org.springframework.web.multipart.MultipartFile;

public interface MemberFileUploadUseCase {

    Long upload(MultipartFile file);
}
