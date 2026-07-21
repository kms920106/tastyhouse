package com.tastyhouse.adminapi.file;

import java.io.IOException;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.tastyhouse.core.domain.file.domain.vo.UploadedFileId;
import com.tastyhouse.core.domain.file.application.FileCommandService;
import com.tastyhouse.core.domain.file.application.FileQueryService;
import com.tastyhouse.core.domain.file.application.dto.command.UploadFileCommand;
import com.tastyhouse.core.exception.BusinessException;
import com.tastyhouse.core.exception.ErrorCode;

@Service
@RequiredArgsConstructor
public class FileService {

    private final FileCommandService fileCommandService;
    private final FileQueryService fileQueryService;

    public Long upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.FILE_EMPTY);
        }

        byte[] content = readBytes(file);
        UploadFileCommand command = UploadFileCommand.of(
            file.getOriginalFilename(),
            content,
            file.getSize(),
            file.getContentType()
        );
        UploadedFileId fileId = fileCommandService.upload(command);
        return fileId.value();
    }

    public String getUrlByPath(String filePath) {
        return fileQueryService.getUrlByPath(filePath);
    }

    private byte[] readBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.FILE_STORE_FAILED);
        }
    }
}
