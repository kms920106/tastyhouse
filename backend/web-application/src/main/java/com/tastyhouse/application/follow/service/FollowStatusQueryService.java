package com.tastyhouse.application.follow.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.application.follow.port.in.FollowStatusQueryUseCase;
import com.tastyhouse.application.member.follow.port.out.MemberFollowQueryPort;

@Service
@Transactional(readOnly = true)
class FollowStatusQueryService implements FollowStatusQueryUseCase {

    private final MemberFollowQueryPort memberFollowQueryPort;

    public FollowStatusQueryService(MemberFollowQueryPort memberFollowQueryPort) {
        this.memberFollowQueryPort = memberFollowQueryPort;
    }

    @Override
    public boolean isFollowing(Long viewerMemberId, Long targetMemberId) {
        return memberFollowQueryPort.existsFollow(
            MemberId.of(viewerMemberId).value(), MemberId.of(targetMemberId).value()
        );
    }
}
