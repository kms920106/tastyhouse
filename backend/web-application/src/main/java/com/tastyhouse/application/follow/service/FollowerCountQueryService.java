package com.tastyhouse.application.follow.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.application.follow.port.in.FollowerCountQueryUseCase;
import com.tastyhouse.application.member.follow.port.out.MemberFollowQueryPort;

@Service
@Transactional(readOnly = true)
class FollowerCountQueryService implements FollowerCountQueryUseCase {

    private final MemberFollowQueryPort memberFollowQueryPort;

    public FollowerCountQueryService(MemberFollowQueryPort memberFollowQueryPort) {
        this.memberFollowQueryPort = memberFollowQueryPort;
    }

    @Override
    public long countFollower(Long memberId) {
        return memberFollowQueryPort.countFollower(MemberId.of(memberId).value());
    }
}
