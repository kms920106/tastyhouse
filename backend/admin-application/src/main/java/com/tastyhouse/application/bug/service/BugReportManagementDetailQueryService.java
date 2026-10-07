package com.tastyhouse.application.bug.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.application.bug.port.in.BugReportManagementDetailQueryUseCase;
import com.tastyhouse.application.bug.port.out.BugReportDetailResult;
import com.tastyhouse.application.bug.port.out.BugReportDetailWithMemberResult;
import com.tastyhouse.application.bug.port.out.BugReportQueryPort;
import com.tastyhouse.application.member.port.out.MemberManagementQueryPort;
import com.tastyhouse.application.member.port.out.MemberWithProfileImageResult;
import com.tastyhouse.application.shared.exception.AdminErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;

@Service
@Transactional(readOnly = true)
class BugReportManagementDetailQueryService implements BugReportManagementDetailQueryUseCase {

    private final BugReportQueryPort bugReportQueryPort;
    private final MemberManagementQueryPort memberManagementQueryPort;

    public BugReportManagementDetailQueryService(BugReportQueryPort bugReportQueryPort, MemberManagementQueryPort memberManagementQueryPort) {
        this.bugReportQueryPort = bugReportQueryPort;
        this.memberManagementQueryPort = memberManagementQueryPort;
    }

    @Override
    public BugReportDetailWithMemberResult getBugReport(Long id) {
        BugReportDetailResult detail = bugReportQueryPort.findDetailById(id)
            .orElseThrow(() -> new ResourceNotFoundException(AdminErrorCode.BUG_REPORT_NOT_FOUND));

        MemberWithProfileImageResult member = memberManagementQueryPort.findMemberWithProfileImageById(MemberId.of(detail.memberId()).value())
            .orElse(null);

        return new BugReportDetailWithMemberResult(detail, member);
    }
}
