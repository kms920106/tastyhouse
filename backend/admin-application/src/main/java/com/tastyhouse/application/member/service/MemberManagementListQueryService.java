package com.tastyhouse.application.member.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.model.MemberGrade;
import com.tastyhouse.domain.member.model.MemberStatus;
import com.tastyhouse.application.member.port.in.MemberManagementListQueryUseCase;
import com.tastyhouse.application.member.port.out.MemberListItemResult;
import com.tastyhouse.application.member.port.out.MemberManagementQueryPort;
import com.tastyhouse.application.member.port.out.MemberSearchCondition;
import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;

@Service
@Transactional(readOnly = true)
class MemberManagementListQueryService implements MemberManagementListQueryUseCase {

    private final MemberManagementQueryPort memberManagementQueryPort;

    public MemberManagementListQueryService(MemberManagementQueryPort memberManagementQueryPort) {
        this.memberManagementQueryPort = memberManagementQueryPort;
    }

    @Override
    public PageResult<MemberListItemResult> getMembers(
        String nickname,
        String username,
        String phone,
        String status,
        String grade,
        int page,
        int size
    ) {
        MemberSearchCondition condition = MemberSearchCondition.of(
            nickname,
            username,
            phone,
            status == null ? null : MemberStatus.from(status).name(),
            grade == null ? null : MemberGrade.from(grade).name()
        );
        PageQuery pageQuery = PageQuery.of(page, size);
        return memberManagementQueryPort.findMembers(condition, pageQuery);
    }
}
