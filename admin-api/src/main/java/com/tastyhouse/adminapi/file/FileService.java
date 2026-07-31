package com.tastyhouse.adminapi.file;

import java.io.IOException;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.tastyhouse.core.domain.file.domain.repository.UploadedFileRepository;
import com.tastyhouse.core.domain.file.domain.service.FileUploadCommand;
import com.tastyhouse.core.domain.file.domain.service.FileUploadService;
import com.tastyhouse.core.domain.file.domain.vo.UploadedFileId;
import com.tastyhouse.core.exception.BusinessException;
import com.tastyhouse.core.exception.ErrorCode;

import com.tastyhouse.adminapi.file.response.FileResponse;

/**
 * 파일 업로드·URL 변환 서비스.
 *
 * <p>업로드 규칙 본체는 도메인 서비스 {@link FileUploadService}가 갖고, 이 클래스는 HTTP 경계의
 * {@code MultipartFile}을 도메인 입력({@link FileUploadCommand})으로 바꾸고 식별자를 {@code Long}으로
 * 내리는 어댑팅만 담당한다.
 *
 * <p>파일은 표현 목적 read model이 없어 infra query DAO를 두지 않으므로(공통 지침 "write 포트 잔류
 * 판정 기준"), 경로 조회는 domain write 포트 {@link UploadedFileRepository}를 직접 사용한다.
 * 다른 도메인 Service들이 응답 URL을 만들 때 쓰는 공용 변환 진입점이기도 하다.
 */
@Service
@RequiredArgsConstructor
public class FileService {

    private final FileUploadService fileUploadService;
    private final UploadedFileRepository uploadedFileRepository;

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

    public String getUrlByPath(String filePath) {
        return fileUploadService.getUrlByPath(filePath);
    }

    /**
     * 파일 식별자로부터 표시용 URL을 만든다. 식별자가 없거나 파일이 존재하지 않으면 {@code null}.
     */
    @Transactional(readOnly = true)
    public String getUrlByFileId(Long fileId) {
        if (fileId == null) {
            return null;
        }
        return uploadedFileRepository.findFilePath(UploadedFileId.of(fileId))
            .map(fileUploadService::getUrlByPath)
            .orElse(null);
    }

    /**
     * 파일 식별자로 파일명·URL을 함께 담은 응답을 만든다. 식별자가 없거나 파일이 존재하지 않으면
     * {@code null}. 도메인 애그리거트를 호출부로 흘리지 않도록 변환은 이 서비스가 끝낸다.
     */
    @Transactional(readOnly = true)
    public FileResponse findFileResponse(Long fileId) {
        if (fileId == null) {
            return null;
        }
        return uploadedFileRepository.findById(UploadedFileId.of(fileId))
            .map(file -> FileResponse.of(
                fileId,
                file.getOriginalFilename(),
                fileUploadService.getUrlByPath(file.getFilePath())
            ))
            .orElse(null);
    }

    private byte[] readBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.FILE_STORE_FAILED);
        }
    }
}
