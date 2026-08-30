package com.tastyhouse.infrastructure.file.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

/**
 * 업로드 파일 JPA 영속 모델.
 *
 * <p>순수 도메인 모델 {@code UploadedFile}과 분리된 영속 전용 엔티티다. DB 매핑(테이블/컬럼/감사 필드)만
 * 담당하고 비즈니스 행위는 갖지 않는다. 도메인↔엔티티 변환은 {@code UploadedFileMapper}가 수행한다.
 */
@Entity
@Table(
    name = "UPLOADED_FILE",
    indexes = {
        @Index(name = "idx_uploaded_file_created_at", columnList = "created_at")
    }
)
public class UploadedFileJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK

    @Column(name = "original_filename", nullable = false, length = 500)
    private String originalFilename; // 업로드 원본 파일명

    @Column(name = "stored_filename", nullable = false, length = 500)
    private String storedFilename; // 서버에 저장된 파일명 (UUID 등으로 변환된 이름)

    @Column(name = "file_path", nullable = false, length = 1000)
    private String filePath; // 파일 저장 경로

    @Column(name = "file_size", nullable = false)
    private Long fileSize; // 파일 크기 (bytes)

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType; // MIME 타입 (예: image/jpeg, application/pdf)

    protected UploadedFileJpaEntity() {
    }

    private UploadedFileJpaEntity(
        String originalFilename,
        String storedFilename,
        String filePath,
        Long fileSize,
        String contentType
    ) {
        this.originalFilename = originalFilename;
        this.storedFilename = storedFilename;
        this.filePath = filePath;
        this.fileSize = fileSize;
        this.contentType = contentType;
    }

    /**
     * 신규 저장용 엔티티를 생성한다(식별자 없음). {@code UploadedFileMapper#toEntity}에서만 호출한다.
     */
    static UploadedFileJpaEntity create(
        String originalFilename,
        String storedFilename,
        String filePath,
        Long fileSize,
        String contentType
    ) {
        return new UploadedFileJpaEntity(originalFilename, storedFilename, filePath, fileSize, contentType);
    }

    public Long getId() {
        return this.id;
    }

    public String getOriginalFilename() {
        return this.originalFilename;
    }

    public String getStoredFilename() {
        return this.storedFilename;
    }

    public String getFilePath() {
        return this.filePath;
    }

    public Long getFileSize() {
        return this.fileSize;
    }

    public String getContentType() {
        return this.contentType;
    }
}
