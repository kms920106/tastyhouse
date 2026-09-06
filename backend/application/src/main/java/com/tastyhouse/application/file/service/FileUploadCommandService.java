package com.tastyhouse.application.file.service;

import com.tastyhouse.application.shared.marker.WebApp;
import java.io.IOException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.tastyhouse.application.file.port.in.FileUploadCommandUseCase;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.file.service.FileUploadCommand;
import com.tastyhouse.domain.file.service.FileUploadService;
import com.tastyhouse.domain.file.vo.UploadedFileId;

@Service
@WebApp
public class FileUploadCommandService implements FileUploadCommandUseCase {

    private final FileUploadService fileUploadService;

    public FileUploadCommandService(FileUploadService fileUploadService) {
        this.fileUploadService = fileUploadService;
    }

    @Override
    @Transactional
    public Long upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.FILE_EMPTY);
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
            throw new BusinessException(ErrorCode.FILE_STORE_FAILED);
        }
    }
}
