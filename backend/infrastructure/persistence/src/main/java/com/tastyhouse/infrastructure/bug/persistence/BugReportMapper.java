package com.tastyhouse.infrastructure.bug.persistence;

import com.tastyhouse.application.bug.port.out.write.BugReportState;

final class BugReportMapper {
    private BugReportMapper() {
    }

    static BugReportState toState(BugReportJpaEntity entity) {
        return new BugReportState(
            entity.getId(),
            entity.getMemberId(),
            entity.getDevice(),
            entity.getTitle(),
            entity.getContent(),
            entity.getStatus(),
            entity.getCategory(),
            entity.getPriority(),
            entity.getAssigneeAdminId(),
            entity.getAdminAnswer(),
            entity.getResolvedAt(),
            entity.getAppVersion(),
            entity.getPlatform(),
            entity.getOsVersion(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static BugReportJpaEntity toEntity(BugReportState state) {
        return BugReportJpaEntity.create(
            state.memberId(),
            state.device(),
            state.title(),
            state.content(),
            state.status(),
            state.category(),
            state.priority(),
            state.assigneeAdminId(),
            state.adminAnswer(),
            state.resolvedAt(),
            state.appVersion(),
            state.platform(),
            state.osVersion()
        );
    }

    static void applyChanges(BugReportJpaEntity entity, BugReportState state) {
        entity.applyChanges(
            state.title(),
            state.content(),
            state.status(),
            state.category(),
            state.priority(),
            state.assigneeAdminId(),
            state.adminAnswer(),
            state.resolvedAt()
        );
    }
}
