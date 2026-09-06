package com.tastyhouse.application.follow.service;

import com.tastyhouse.application.shared.marker.WebApp;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.follow.service.MemberFollowService;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.application.follow.port.in.FollowCancelCommand;
import com.tastyhouse.application.follow.port.in.FollowCommandUseCase;
import com.tastyhouse.application.follow.port.in.FollowCreateCommand;
import com.tastyhouse.application.follow.port.in.FollowerRemoveCommand;

@Service
@WebApp
@Transactional
public class FollowCommandService implements FollowCommandUseCase {

    private final MemberFollowService memberFollowService;

    public FollowCommandService(MemberFollowService memberFollowService) {
        this.memberFollowService = memberFollowService;
    }

    @Override
    public Long follow(FollowCreateCommand command) {
        MemberId followerMemberId = MemberId.of(command.followerId());
        MemberId followingMemberId = MemberId.of(command.followingId());
        return memberFollowService.follow(followerMemberId, followingMemberId);
    }

    @Override
    public void unfollow(FollowCancelCommand command) {
        MemberId followerMemberId = MemberId.of(command.followerId());
        MemberId followingMemberId = MemberId.of(command.followingId());
        memberFollowService.unfollow(followerMemberId, followingMemberId);
    }

    @Override
    public void removeFollower(FollowerRemoveCommand command) {
        MemberId targetMemberId = MemberId.of(command.memberId());
        MemberId followerMemberId = MemberId.of(command.followerId());
        memberFollowService.removeFollower(targetMemberId, followerMemberId);
    }
}
