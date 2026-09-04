package com.tastyhouse.ceoapplication.file.service;

import java.io.IOException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.tastyhouse.ceoapplication.file.port.in.FileUploadOwnerCommandUseCase;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.file.service.FileUploadCommand;
import com.tastyhouse.domain.file.service.FileUploadService;
import com.tastyhouse.domain.file.vo.UploadedFileId;

/**
 * 파일 업로드 유스케이스(ceo 전용 트랜잭션 경계).
 *
 * <p>업로드 규칙 본체(허용 확장자·용량 한도·저장 경로·이벤트 발행)는 프레임워크-프리 도메인 서비스
 * {@link FileUploadService}가 단독으로 갖는다. 이 클래스가 하는 일은 두 가지뿐이다 —
 * HTTP 경계의 {@code MultipartFile}을 도메인 입력({@link FileUploadCommand})으로 바꾸는 어댑팅과,
 * {@code @Transactional}로 트랜잭션 경계를 선언하는 것이다.
 *
 * <p><b>앱마다 이 래퍼를 따로 두는 이유</b>는 트랜잭션 경계가 앱의 관심사이기 때문이다. 과거에는
 * api-common-module의 {@code FileService} 한 벌이 이 역할을 겸했는데, 표현 모듈이 {@code @Transactional}
 * 유스케이스를 갖는 데다 application 계층이 그것을 주입받아 application→표현 역방향 의존이 생겼다.
 * 세 벌의 래퍼는 로직 중복이 아니라 경계 선언 3개다.
 *
 * <p><b>조회(URL 변환) 책임은 이 클래스에 없다.</b> 파일 경로를 표시용 URL로 바꾸는 일은
 * infrastructure의 {@code FileUrlResolver}를 통해 각 도메인 query DAO가 조회 시점에 끝내며,
 * Result가 이미 URL을 담은 채 나온다.
 */
@Service
public class FileUploadOwnerCommandService implements FileUploadOwnerCommandUseCase {

    private final FileUploadService fileUploadService;

    public FileUploadOwnerCommandService(FileUploadService fileUploadService) {
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
