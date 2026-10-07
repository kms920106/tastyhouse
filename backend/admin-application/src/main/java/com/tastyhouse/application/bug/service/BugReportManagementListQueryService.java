package com.tastyhouse.application.bug.service;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.bug.model.BugReportCategory;
import com.tastyhouse.domain.bug.model.BugReportPriority;
import com.tastyhouse.domain.bug.model.BugReportStatus;
import com.tastyhouse.application.bug.port.in.BugReportManagementListQueryUseCase;
import com.tastyhouse.application.bug.port.out.BugReportListItemResult;
import com.tastyhouse.application.bug.port.out.BugReportListItemWithMemberResult;
import com.tastyhouse.application.bug.port.out.BugReportQueryPort;
import com.tastyhouse.application.bug.port.out.BugReportSearchCondition;
import com.tastyhouse.application.member.port.out.MemberManagementQueryPort;
import com.tastyhouse.application.member.port.out.MemberWithProfileImageResult;
import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;

@Service
@Transactional(readOnly = true)
class BugReportManagementListQueryService implements BugReportManagementListQueryUseCase {

    private final BugReportQueryPort bugReportQueryPort;
    private final MemberManagementQueryPort memberManagementQueryPort;

    public BugReportManagementListQueryService(BugReportQueryPort bugReportQueryPort, MemberManagementQueryPort memberManagementQueryPort) {
        this.bugReportQueryPort = bugReportQueryPort;
        this.memberManagementQueryPort = memberManagementQueryPort;
    }

    @Override
    public PageResult<BugReportListItemWithMemberResult> getBugReports(
        String title,
        String content,
        Long memberId,
        String status,
        String category,
        String priority,
        int page,
        int size
    ) {
        BugReportSearchCondition condition = BugReportSearchCondition.of(
            title,
            content,
            memberId,
            status == null ? null : BugReportStatus.from(status).name(),
            category == null ? null : BugReportCategory.from(category).name(),
            priority == null ? null : BugReportPriority.from(priority).name()
        );
        PageQuery pageQuery = PageQuery.of(page, size);
        PageResult<BugReportListItemResult> pageResult = bugReportQueryPort.findBugReports(condition, pageQuery);

        Map<Long, MemberWithProfileImageResult> membersById = memberManagementQueryPort.findMemberWithProfileImagesByIds(
            pageResult.content().stream().map(BugReportListItemResult::memberId).toList()
        );

        return pageResult.map(
            dto -> new BugReportListItemWithMemberResult(dto, membersById.get(dto.memberId()))
        );
    }
}
