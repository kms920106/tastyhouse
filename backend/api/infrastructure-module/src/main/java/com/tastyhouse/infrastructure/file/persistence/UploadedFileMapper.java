package com.tastyhouse.infrastructure.file.persistence;

import com.tastyhouse.domain.file.model.UploadedFile;

/**
 * 업로드 파일 도메인 모델 ↔ JPA 엔티티 변환기. 도메인이 프레임워크-프리를 유지하도록 변환 책임을 infrastructure에 둔다.
 */
final class UploadedFileMapper {

    private UploadedFileMapper() {
    }

    /**
     * JPA 엔티티를 도메인 모델로 재구성한다(조회 경로).
     */
    static UploadedFile toDomain(UploadedFileJpaEntity entity) {
        return UploadedFile.reconstitute(
            entity.getId(),
            entity.getOriginalFilename(),
            entity.getStoredFilename(),
            entity.getFilePath(),
            entity.getFileSize(),
            entity.getContentType(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    /**
     * 신규 도메인 모델을 저장용 JPA 엔티티로 변환한다(식별자 없는 상태).
     */
    static UploadedFileJpaEntity toEntity(UploadedFile domain) {
        return UploadedFileJpaEntity.create(
            domain.getOriginalFilename(),
            domain.getStoredFilename(),
            domain.getFilePath(),
            domain.getFileSize(),
            domain.getContentType()
        );
    }
}
