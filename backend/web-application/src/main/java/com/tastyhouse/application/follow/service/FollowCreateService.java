package com.tastyhouse.application.follow.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.application.follow.port.in.FollowCreateCommand;
import com.tastyhouse.application.follow.port.in.FollowCreateUseCase;
import com.tastyhouse.application.member.follow.service.MemberFollowService;

@Service
@Transactional
class FollowCreateService implements FollowCreateUseCase {

    private final MemberFollowService memberFollowService;

    public FollowCreateService(MemberFollowService memberFollowService) {
        this.memberFollowService = memberFollowService;
    }

    @Override
    public Long follow(FollowCreateCommand command) {
        MemberId followerMemberId = MemberId.of(command.followerId());
        MemberId followingMemberId = MemberId.of(command.followingId());
        return memberFollowService.follow(followerMemberId, followingMemberId);
    }
}
