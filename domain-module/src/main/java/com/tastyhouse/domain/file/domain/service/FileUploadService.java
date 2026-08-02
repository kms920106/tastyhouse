package com.tastyhouse.domain.file.domain.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.UUID;

import com.tastyhouse.domain.file.domain.event.FileUploadedEvent;
import com.tastyhouse.domain.file.domain.model.UploadedFile;
import com.tastyhouse.domain.file.domain.port.FileStoragePort;
import com.tastyhouse.domain.file.domain.repository.UploadedFileRepository;
import com.tastyhouse.domain.file.domain.vo.UploadedFileId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.shared.event.DomainEventPublisher;

/**
 * 파일 업로드 규칙(도메인 서비스).
 *
 * <p>업로드는 "규격 검증 → 스토리지 저장(출력 포트) → 메타 애그리거트 저장 → 업로드 이벤트 발행"을
 * 한 트랜잭션에서 원자로 묶는 액터 무관 연산이다(공통 지침 분류 C). 업로드 트리거가 web·admin·ceo·
 * batch(외부 이미지 다운로드)로 여러 개이므로, 허용 확장자·용량 한도·저장 경로 규칙이 모듈마다
 * 갈리지 않도록 도메인 계층에 단 하나만 둔다.
 *
 * <p>{@code @Service}/{@code @Transactional} 없는 순수 POJO이며(공통 지침 패턴 1), 빈 등록은
 * infrastructure-module의 {@code DomainServiceConfig}가 담당한다. 이벤트 발행은 프레임워크-프리
 * 포트인 {@link DomainEventPublisher}를 통해 수행한다.
 *
 * <p>이 서비스는 쓰기(업로드)만 담당한다. 저장 경로를 표시용 URL로 바꾸는 읽기 측 변환은
 * infrastructure-module의 {@code FileUrlResolver}가 {@link FileStoragePort}를 직접 사용해 수행하므로,
 * 과거 여기 있던 {@code getUrlByPath}는 제거했다 — 조회 응답 조립에 도메인 서비스를 끌어들일 이유가
 * 없고, 변환 지점을 read 어댑터 한 곳으로 모으는 편이 일관되기 때문이다.
 */
public class FileUploadService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
        "image/jpeg", "image/png", "image/gif", "image/webp"
    );
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    private final UploadedFileRepository uploadedFileRepository;
    private final FileStoragePort fileStoragePort;
    private final DomainEventPublisher domainEventPublisher;

    public FileUploadService(
        UploadedFileRepository uploadedFileRepository,
        FileStoragePort fileStoragePort,
        DomainEventPublisher domainEventPublisher
    ) {
        this.uploadedFileRepository = uploadedFileRepository;
        this.fileStoragePort = fileStoragePort;
        this.domainEventPublisher = domainEventPublisher;
    }

    /**
     * 파일을 검증·저장하고 업로드 이벤트를 발행한 뒤 식별자를 반환한다.
     */
    public UploadedFileId upload(FileUploadCommand command) {
        validate(command);

        String extension = extractExtension(command.originalFilename());
        String storedFilename = UUID.randomUUID() + "." + extension;
        String datePath = LocalDate.now().format(DATE_FORMATTER);

        String filePath = fileStoragePort.store(command.content(), storedFilename, datePath, command.contentType());

        UploadedFile saved = uploadedFileRepository.save(UploadedFile.of(
            command.originalFilename(),
            storedFilename,
            filePath,
            command.fileSize(),
            command.contentType()
        ));

        UploadedFileId fileId = saved.getUploadedFileId();
        domainEventPublisher.publish(new FileUploadedEvent(
            fileId,
            saved.getFilePath(),
            saved.getContentType(),
            LocalDateTime.now()
        ));
        return fileId;
    }

    private void validate(FileUploadCommand command) {
        if (command.content() == null || command.content().length == 0) {
            throw new BusinessException(ErrorCode.FILE_EMPTY);
        }

        if (command.fileSize() > MAX_FILE_SIZE) {
            throw new BusinessException(ErrorCode.FILE_SIZE_EXCEEDED);
        }

        String contentType = command.contentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new BusinessException(ErrorCode.FILE_TYPE_NOT_ALLOWED);
        }

        String extension = extractExtension(command.originalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException(ErrorCode.FILE_EXTENSION_NOT_ALLOWED);
        }
    }

    private String extractExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            throw new BusinessException(ErrorCode.FILE_EXTENSION_UNKNOWN);
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }
}
