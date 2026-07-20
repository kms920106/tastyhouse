package com.tastyhouse.core.domain.file.domain.model;

import java.time.LocalDateTime;

import lombok.Getter;

import com.tastyhouse.core.domain.file.domain.vo.UploadedFileId;

/**
 * 업로드 파일 순수 도메인 모델.
 *
 * <p>JPA/프레임워크에 의존하지 않는 POJO다. 영속화는 infrastructure-module의
 * {@code UploadedFileJpaEntity} + {@code UploadedFileMapper}가 담당한다.
 */
@Getter
public class UploadedFile {

    private final Long id; // null이면 아직 영속되지 않은 신규 상태
    private final String originalFilename;
    private final String storedFilename;
    private final String filePath;
    private final Long fileSize;
    private final String contentType;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private UploadedFile(
        Long id,
        String originalFilename,
        String storedFilename,
        String filePath,
        Long fileSize,
        String contentType,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.originalFilename = originalFilename;
        this.storedFilename = storedFilename;
        this.filePath = filePath;
        this.fileSize = fileSize;
        this.contentType = contentType;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 신규 업로드 파일을 생성한다(식별자·감사 시각 없음).
     */
    public static UploadedFile of(
        String originalFilename,
        String storedFilename,
        String filePath,
        Long fileSize,
        String contentType
    ) {
        return new UploadedFile(null, originalFilename, storedFilename, filePath, fileSize, contentType, null, null);
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이며,
     * 불변식을 우회한 임의 생성을 막기 위해 이 팩토리로만 식별자를 주입한다.
     */
    public static UploadedFile reconstitute(
        Long id,
        String originalFilename,
        String storedFilename,
        String filePath,
        Long fileSize,
        String contentType,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new UploadedFile(id, originalFilename, storedFilename, filePath, fileSize, contentType, createdAt, updatedAt);
    }

    public UploadedFileId getUploadedFileId() {
        return UploadedFileId.of(this.id);
    }
}
