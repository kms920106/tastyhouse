package com.tastyhouse.application.follow.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.application.follow.port.in.FollowerRemoveCommand;
import com.tastyhouse.application.follow.port.in.FollowerRemoveUseCase;
import com.tastyhouse.application.member.follow.service.MemberFollowService;

@Service
@Transactional
class FollowerRemoveService implements FollowerRemoveUseCase {

    private final MemberFollowService memberFollowService;

    public FollowerRemoveService(MemberFollowService memberFollowService) {
        this.memberFollowService = memberFollowService;
    }

    @Override
    public void removeFollower(FollowerRemoveCommand command) {
        MemberId targetMemberId = MemberId.of(command.memberId());
        MemberId followerMemberId = MemberId.of(command.followerId());
        memberFollowService.removeFollower(targetMemberId, followerMemberId);
    }
}
