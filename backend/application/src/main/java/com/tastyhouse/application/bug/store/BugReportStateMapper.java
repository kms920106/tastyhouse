package com.tastyhouse.application.bug.store;

import com.tastyhouse.domain.admin.vo.AdminId;
import com.tastyhouse.domain.bug.model.BugReport;
import com.tastyhouse.domain.bug.model.BugReportCategory;
import com.tastyhouse.domain.bug.model.BugReportPlatform;
import com.tastyhouse.domain.bug.model.BugReportPriority;
import com.tastyhouse.domain.bug.model.BugReportStatus;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.application.bug.port.out.write.BugReportState;

final class BugReportStateMapper {
    private BugReportStateMapper() {
    }

    static BugReport toDomain(BugReportState state) {
        return BugReport.reconstitute(
            state.id(),
            state.memberId() == null ? null : MemberId.of(state.memberId()),
            state.device(),
            state.title(),
            state.content(),
            state.status() == null ? null : BugReportStatus.valueOf(state.status()),
            state.category() == null ? null : BugReportCategory.valueOf(state.category()),
            state.priority() == null ? null : BugReportPriority.valueOf(state.priority()),
            state.assigneeAdminId() == null ? null : AdminId.of(state.assigneeAdminId()),
            state.adminAnswer(),
            state.resolvedAt(),
            state.appVersion(),
            state.platform() == null ? null : BugReportPlatform.valueOf(state.platform()),
            state.osVersion(),
            state.createdAt(),
            state.updatedAt()
        );
    }

    static BugReportState toState(BugReport bugReport) {
        return new BugReportState(
            bugReport.getId(),
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
            bugReport.getOsVersion(),
            bugReport.getCreatedAt(),
            bugReport.getUpdatedAt()
        );
    }
}
