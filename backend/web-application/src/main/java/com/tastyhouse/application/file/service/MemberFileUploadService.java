package com.tastyhouse.application.file.service;

import java.io.IOException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.application.file.port.in.MemberFileUploadUseCase;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

@Service
@Transactional
class MemberFileUploadService implements MemberFileUploadUseCase {

    private final FileUploadService fileUploadService;

    public MemberFileUploadService(FileUploadService fileUploadService) {
        this.fileUploadService = fileUploadService;
    }

    @Override
    public Long upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ApplicationException(ApplicationErrorCode.FILE_EMPTY);
        }

        byte[] content = readBytes(file);
        FileUploadCommand command = FileUploadCommand.of(
            file.getOriginalFilename(),
            content,
            file.getSize(),
            file.getContentType()
        );
        UploadedFileId fileId = fileUploadService.upload(command);
        return fileId.value();
    }

    private byte[] readBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException e) {
            throw new ApplicationException(ApplicationErrorCode.FILE_STORE_FAILED);
        }
    }
}
