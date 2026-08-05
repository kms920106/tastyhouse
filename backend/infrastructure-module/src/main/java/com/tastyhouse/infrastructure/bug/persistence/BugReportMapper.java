package com.tastyhouse.infrastructure.bug.persistence;

import com.tastyhouse.domain.admin.vo.AdminId;
import com.tastyhouse.domain.bug.model.BugReport;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

/**
 * 버그 신고 도메인 모델 ↔ JPA 엔티티 변환기. 도메인이 프레임워크-프리를 유지하도록 변환 책임을 infrastructure에 둔다.
 */
final class BugReportMapper {

    private BugReportMapper() {
    }

    /**
     * JPA 엔티티를 도메인 모델로 재구성한다(조회 경로).
     */
    static BugReport toDomain(BugReportJpaEntity entity) {
        return BugReport.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getMemberId(), MemberId::of),
            entity.getDevice(),
            entity.getTitle(),
            entity.getContent(),
            entity.getStatus(),
            entity.getCategory(),
            entity.getPriority(),
            IdMapping.vo(entity.getAssigneeAdminId(), AdminId::of),
            entity.getAdminAnswer(),
            entity.getResolvedAt(),
            entity.getAppVersion(),
            entity.getPlatform(),
            entity.getOsVersion(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    /**
     * 신규 도메인 모델을 저장용 JPA 엔티티로 변환한다(식별자 없는 상태).
     */
    static BugReportJpaEntity toEntity(BugReport domain) {
        return BugReportJpaEntity.create(
            IdMapping.raw(domain.getMemberId(), MemberId::value),
            domain.getDevice(),
            domain.getTitle(),
            domain.getContent(),
            domain.getStatus(),
            domain.getCategory(),
            domain.getPriority(),
            IdMapping.raw(domain.getAssigneeAdminId(), AdminId::value),
            domain.getAdminAnswer(),
            domain.getResolvedAt(),
            domain.getAppVersion(),
            domain.getPlatform(),
            domain.getOsVersion()
        );
    }

    /**
     * managed 엔티티에 도메인의 변경 필드를 복사한다(update 경로, dirty checking 대체).
     */
    static void applyChanges(BugReportJpaEntity entity, BugReport domain) {
        entity.applyChanges(
            domain.getTitle(),
            domain.getContent(),
            domain.getStatus(),
            domain.getCategory(),
            domain.getPriority(),
            IdMapping.raw(domain.getAssigneeAdminId(), AdminId::value),
            domain.getAdminAnswer(),
            domain.getResolvedAt()
        );
    }
}
