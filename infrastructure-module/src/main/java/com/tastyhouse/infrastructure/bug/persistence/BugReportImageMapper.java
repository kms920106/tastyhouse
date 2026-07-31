package com.tastyhouse.infrastructure.bug.persistence;

import com.tastyhouse.domain.bug.domain.model.BugReportImage;

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
            entity.getBugReportId(),
            entity.getImageFileId(),
            entity.getSort()
        );
    }

    /**
     * 신규 도메인 모델을 저장용 JPA 엔티티로 변환한다(식별자 없는 상태).
     */
    static BugReportImageJpaEntity toEntity(BugReportImage domain) {
        return BugReportImageJpaEntity.create(
            domain.getBugReportId(),
            domain.getImageFileId(),
            domain.getSort()
        );
    }
}
