package com.tastyhouse.infrastructure.bug.persistence;

import com.tastyhouse.domain.admin.vo.AdminId;
import com.tastyhouse.domain.bug.model.BugReport;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

final class BugReportMapper {
    private BugReportMapper() {
    }

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
