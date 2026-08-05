package com.tastyhouse.infrastructure.bug.persistence;

import com.tastyhouse.domain.bug.model.BugReportImage;
import com.tastyhouse.domain.bug.vo.BugReportId;
import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

/**
 * 버그 신고 이미지 도메인 모델 ↔ JPA 엔티티 변환기.
 */
final class BugReportImageMapper {

    private BugReportImageMapper() {
    }

    /**
     * JPA 엔티티를 도메인 모델로 재구성한다(조회 경로).
     */
    static BugReportImage toDomain(BugReportImageJpaEntity entity) {
        return BugReportImage.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getBugReportId(), BugReportId::of),
            IdMapping.vo(entity.getImageFileId(), UploadedFileId::of),
            entity.getSort()
        );
    }

    /**
     * 신규 도메인 모델을 저장용 JPA 엔티티로 변환한다(식별자 없는 상태).
     */
    static BugReportImageJpaEntity toEntity(BugReportImage domain) {
        return BugReportImageJpaEntity.create(
            IdMapping.raw(domain.getBugReportId(), BugReportId::value),
            IdMapping.raw(domain.getImageFileId(), UploadedFileId::value),
            domain.getSort()
        );
    }
}
