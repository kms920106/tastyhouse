package com.tastyhouse.application.follow.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.application.follow.port.in.FollowCancelCommand;
import com.tastyhouse.application.follow.port.in.FollowCancelUseCase;
import com.tastyhouse.application.member.follow.service.MemberFollowService;

@Service
@Transactional
class FollowCancelService implements FollowCancelUseCase {

    private final MemberFollowService memberFollowService;

    public FollowCancelService(MemberFollowService memberFollowService) {
        this.memberFollowService = memberFollowService;
    }

    @Override
    public void unfollow(FollowCancelCommand command) {
        MemberId followerMemberId = MemberId.of(command.followerId());
        MemberId followingMemberId = MemberId.of(command.followingId());
        memberFollowService.unfollow(followerMemberId, followingMemberId);
    }
}
