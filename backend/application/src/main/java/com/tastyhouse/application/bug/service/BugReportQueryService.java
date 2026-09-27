package com.tastyhouse.application.bug.service;

import com.tastyhouse.application.shared.marker.AdminApp;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.bug.model.BugReportCategory;
import com.tastyhouse.domain.bug.model.BugReportPriority;
import com.tastyhouse.domain.bug.model.BugReportStatus;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.application.bug.port.out.BugReportDetailResult;
import com.tastyhouse.application.bug.port.out.BugReportDetailWithMemberResult;
import com.tastyhouse.application.bug.port.out.BugReportListItemResult;
import com.tastyhouse.application.bug.port.out.BugReportListItemWithMemberResult;
import com.tastyhouse.application.bug.port.out.BugReportQueryPort;
import com.tastyhouse.application.bug.port.out.BugReportSearchCondition;
import com.tastyhouse.application.member.port.out.MemberManagementQueryPort;
import com.tastyhouse.application.member.port.out.MemberWithProfileImageResult;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;
import com.tastyhouse.application.bug.port.in.BugReportQueryUseCase;

@Service
@AdminApp
@Transactional(readOnly = true)
public class BugReportQueryService implements BugReportQueryUseCase {

    private final BugReportQueryPort bugReportQueryPort;
    private final MemberManagementQueryPort memberManagementQueryPort;

    public BugReportQueryService(BugReportQueryPort bugReportQueryPort, MemberManagementQueryPort memberManagementQueryPort) {
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

    @Override
    public BugReportDetailWithMemberResult getBugReport(Long id) {
        BugReportDetailResult detail = bugReportQueryPort.findDetailById(id)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.BUG_REPORT_NOT_FOUND));

        MemberWithProfileImageResult member = memberManagementQueryPort.findMemberWithProfileImageById(MemberId.of(detail.memberId()).value())
            .orElse(null);

        return new BugReportDetailWithMemberResult(detail, member);
    }
}
