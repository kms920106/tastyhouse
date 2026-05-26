package com.tastyhouse.core.domain.file.domain.model;

import com.tastyhouse.core.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
    name = "UPLOADED_FILE",
    indexes = {
        @Index(name = "idx_uploaded_file_created_at", columnList = "created_at")
    }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UploadedFile extends BaseEntity {

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

    private UploadedFile(
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

    public UploadedFileId getUploadedFileId() {
        return new UploadedFileId(this.id);
    }

    public static UploadedFile of(
        String originalFilename,
        String storedFilename,
        String filePath,
        Long fileSize,
        String contentType
    ) {
        return new UploadedFile(
            originalFilename,
            storedFilename,
            filePath,
            fileSize,
            contentType
        );
    }
}
