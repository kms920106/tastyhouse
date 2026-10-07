package com.tastyhouse.application.follow.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.application.follow.port.in.FollowerListQueryUseCase;
import com.tastyhouse.application.member.follow.port.out.FollowMemberResult;
import com.tastyhouse.application.member.follow.port.out.MemberFollowQueryPort;
import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;

@Service
@Transactional(readOnly = true)
class FollowerListQueryService implements FollowerListQueryUseCase {

    private final MemberFollowQueryPort memberFollowQueryPort;

    public FollowerListQueryService(MemberFollowQueryPort memberFollowQueryPort) {
        this.memberFollowQueryPort = memberFollowQueryPort;
    }

    @Override
    public PageResult<FollowMemberResult> getFollowerList(Long memberId, Long viewerMemberId, int page, int size) {
        return memberFollowQueryPort.findFollowerList(
            MemberId.of(memberId).value(), toViewerId(viewerMemberId), PageQuery.of(page, size)
        );
    }

    private Long toViewerId(Long viewerMemberId) {
        return viewerMemberId == null ? null : MemberId.of(viewerMemberId).value();
    }
}
