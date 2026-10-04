package com.tastyhouse.infrastructure.persistence.bug.persistence;

import com.tastyhouse.domain.admin.vo.AdminId;
import com.tastyhouse.domain.bug.model.BugReport;
import com.tastyhouse.domain.bug.model.BugReportCategory;
import com.tastyhouse.domain.bug.model.BugReportPlatform;
import com.tastyhouse.domain.bug.model.BugReportPriority;
import com.tastyhouse.domain.bug.model.BugReportStatus;
import com.tastyhouse.domain.member.vo.MemberId;

final class BugReportMapper {

    private BugReportMapper() {
    }

    static BugReport toDomain(BugReportJpaEntity entity) {
        return BugReport.reconstitute(
            entity.getId(),
            entity.getMemberId() == null ? null : MemberId.of(entity.getMemberId()),
            entity.getDevice(),
            entity.getTitle(),
            entity.getContent(),
            entity.getStatus() == null ? null : BugReportStatus.valueOf(entity.getStatus()),
            entity.getCategory() == null ? null : BugReportCategory.valueOf(entity.getCategory()),
            entity.getPriority() == null ? null : BugReportPriority.valueOf(entity.getPriority()),
            entity.getAssigneeAdminId() == null ? null : AdminId.of(entity.getAssigneeAdminId()),
            entity.getAdminAnswer(),
            entity.getResolvedAt(),
            entity.getAppVersion(),
            entity.getPlatform() == null ? null : BugReportPlatform.valueOf(entity.getPlatform()),
            entity.getOsVersion(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static BugReportJpaEntity toEntity(BugReport bugReport) {
        return BugReportJpaEntity.create(
            bugReport.getMemberId() == null ? null : bugReport.getMemberId().value(),
            bugReport.getDevice(),
            bugReport.getTitle(),
            bugReport.getContent(),
            bugReport.getStatus() == null ? null : bugReport.getStatus().name(),
            bugReport.getCategory() == null ? null : bugReport.getCategory().name(),
            bugReport.getPriority() == null ? null : bugReport.getPriority().name(),
            bugReport.getAssigneeAdminId() == null ? null : bugReport.getAssigneeAdminId().value(),
            bugReport.getAdminAnswer(),
            bugReport.getResolvedAt(),
            bugReport.getAppVersion(),
            bugReport.getPlatform() == null ? null : bugReport.getPlatform().name(),
            bugReport.getOsVersion()
        );
    }

    static void applyChanges(BugReportJpaEntity entity, BugReport bugReport) {
        entity.applyChanges(
            bugReport.getTitle(),
            bugReport.getContent(),
            bugReport.getStatus() == null ? null : bugReport.getStatus().name(),
            bugReport.getCategory() == null ? null : bugReport.getCategory().name(),
            bugReport.getPriority() == null ? null : bugReport.getPriority().name(),
            bugReport.getAssigneeAdminId() == null ? null : bugReport.getAssigneeAdminId().value(),
            bugReport.getAdminAnswer(),
            bugReport.getResolvedAt()
        );
    }
}
