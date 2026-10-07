package com.tastyhouse.application.follow.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.application.follow.port.in.FollowingCountQueryUseCase;
import com.tastyhouse.application.member.follow.port.out.MemberFollowQueryPort;

@Service
@Transactional(readOnly = true)
class FollowingCountQueryService implements FollowingCountQueryUseCase {

    private final MemberFollowQueryPort memberFollowQueryPort;

    public FollowingCountQueryService(MemberFollowQueryPort memberFollowQueryPort) {
        this.memberFollowQueryPort = memberFollowQueryPort;
    }

    @Override
    public long countFollowing(Long memberId) {
        return memberFollowQueryPort.countFollowing(MemberId.of(memberId).value());
    }
}
