package com.tastyhouse.webapi.follow;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.core.domain.member.follow.domain.service.MemberFollowService;

/**
 * 팔로우 명령 서비스.
 *
 * <p>팔로우 등록·해제는 팔로우 대상 회원의 존재 확인을 포함하는 크로스 애그리거트 규칙이므로 도메인
 * 서비스({@link MemberFollowService})에 위임하고, 이 서비스는 HTTP 경계의 {@code Long}을 VO로 승격하는
 * 책임만 갖는다.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class FollowCommandService {

    private final MemberFollowService memberFollowService;

    public void follow(Long followerId, Long followingId) {
        MemberId followerMemberId = MemberId.of(followerId);
        MemberId followingMemberId = MemberId.of(followingId);
        memberFollowService.follow(followerMemberId, followingMemberId);
    }

    public void unfollow(Long followerId, Long followingId) {
        MemberId followerMemberId = MemberId.of(followerId);
        MemberId followingMemberId = MemberId.of(followingId);
        memberFollowService.unfollow(followerMemberId, followingMemberId);
    }

    public void removeFollower(Long memberId, Long followerId) {
        MemberId targetMemberId = MemberId.of(memberId);
        MemberId followerMemberId = MemberId.of(followerId);
        memberFollowService.removeFollower(targetMemberId, followerMemberId);
    }
}
