package com.tastyhouse.webapi.follow.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.follow.service.MemberFollowService;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.webapi.follow.application.port.in.FollowCancelCommand;
import com.tastyhouse.webapi.follow.application.port.in.FollowCommandUseCase;
import com.tastyhouse.webapi.follow.application.port.in.FollowCreateCommand;
import com.tastyhouse.webapi.follow.application.port.in.FollowerRemoveCommand;

/**
 * 팔로우 명령 서비스.
 *
 * <p>팔로우 등록·해제는 팔로우 대상 회원의 존재 확인을 포함하는 크로스 애그리거트 규칙이므로 도메인
 * 서비스({@link MemberFollowService})에 위임하고, 이 서비스는 HTTP 경계의 {@code Long}을 VO로 승격하는
 * 책임만 갖는다.
 */
@Service
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
