package com.tastyhouse.apicommon.file;

import java.io.IOException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.tastyhouse.domain.file.domain.service.FileUploadCommand;
import com.tastyhouse.domain.file.domain.service.FileUploadService;
import com.tastyhouse.domain.file.domain.vo.UploadedFileId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 파일 업로드 서비스.
 *
 * <p>업로드 규칙 본체(허용 확장자·용량 한도·저장 경로·이벤트 발행)는 도메인 서비스
 * {@link FileUploadService}가 단독으로 갖고, 이 클래스는 HTTP 경계의 {@code MultipartFile}을 도메인
 * 입력({@link FileUploadCommand})으로 바꾸고 식별자를 {@code Long}으로 내리는 어댑팅만 담당한다.
 * {@code MultipartFile}은 spring-web 타입이라 프레임워크-프리인 domain-module에 둘 수 없으므로,
 * 이 얇은 어댑터만 각 api 모듈에 남는다({@code ApiResponse}·{@code PageRequest}와 동일한 모듈별
 * 중복 관례).
 *
 * <p><b>조회(URL 변환) 책임은 이 클래스에 없다.</b> 파일 경로를 표시용 URL로 바꾸는 일은
 * infrastructure-module의 {@code FileUrlResolver}를 통해 각 도메인 query DAO가 조회 시점에 끝내며,
 * Result가 이미 URL을 담은 채 나온다. 과거 이 클래스에 있던 {@code getUrlByPath}·
 * {@code getUrlByFileId}·{@code getUrlsByFileIds}·{@code findFileResponse}는 그 전환으로 전부
 * 제거됐다 — 뒤의 셋은 응답 조립 중에 파일을 다시 조회해 추가 DB 왕복을 유발하던 우회 경로였다.
 */
@Service
public class FileService {

    private final FileUploadService fileUploadService;

    public FileService(FileUploadService fileUploadService) {
        this.fileUploadService = fileUploadService;
    }

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
