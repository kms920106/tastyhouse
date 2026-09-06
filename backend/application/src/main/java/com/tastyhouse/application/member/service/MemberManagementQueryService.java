package com.tastyhouse.application.member.service;

import com.tastyhouse.application.shared.marker.AdminApp;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.model.MemberGrade;
import com.tastyhouse.domain.member.model.MemberStatus;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.application.member.port.out.MemberListItemResult;
import com.tastyhouse.application.member.port.out.MemberManagementDetailResult;
import com.tastyhouse.application.member.port.out.MemberManagementDetailWithProfileImageResult;
import com.tastyhouse.application.member.port.out.MemberManagementQueryPort;
import com.tastyhouse.application.member.port.out.MemberSearchCondition;
import com.tastyhouse.application.member.port.in.MemberManagementQueryUseCase;

@Service
@AdminApp
@Transactional(readOnly = true)
public class MemberManagementQueryService implements MemberManagementQueryUseCase {

    private final MemberManagementQueryPort memberManagementQueryPort;

    public MemberManagementQueryService(MemberManagementQueryPort memberManagementQueryPort) {
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
            status == null ? null : MemberStatus.from(status),
            grade == null ? null : MemberGrade.from(grade)
        );
        PageQuery pageQuery = PageQuery.of(page, size);
        return memberManagementQueryPort.findMembers(condition, pageQuery);
    }

    @Override
    public MemberManagementDetailWithProfileImageResult getMember(Long id) {
        MemberManagementDetailResult member = memberManagementQueryPort.findManagementDetailById(MemberId.of(id))
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.MEMBER_NOT_FOUND));

        String profileImageUrl = memberManagementQueryPort.findProfileImageUrl(MemberId.of(member.id())).orElse(null);

        return new MemberManagementDetailWithProfileImageResult(member, profileImageUrl);
    }

}
