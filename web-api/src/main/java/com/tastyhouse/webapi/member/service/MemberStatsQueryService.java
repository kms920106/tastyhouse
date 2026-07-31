package com.tastyhouse.webapi.member.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.webapi.follow.FollowQueryService;
import com.tastyhouse.webapi.member.response.MemberStatsResponse;
import com.tastyhouse.webapi.review.ReviewQueryService;

/**
 * 회원 통계(리뷰 수·팔로잉 수·팔로워 수) 조회 전용 서비스.
 *
 * <p>팔로우 등록·해제 도메인 로직은 domain-module의
 * {@code com.tastyhouse.domain.member.follow.domain.service.MemberFollowService}가 담당하며,
 * 이 클래스는 표현용 집계 조회만 수행한다.
 */
@Service
@RequiredArgsConstructor
public class MemberStatsQueryService {

    private final ReviewQueryService reviewQueryService;
    private final FollowQueryService followQueryService;

    // 회원의 리뷰 수, 팔로잉 수, 팔로워 수를 조회
    @Transactional(readOnly = true)
    public MemberStatsResponse getMemberStats(Long memberId) {
        long reviewCount = reviewQueryService.countVisibleReviewsByMemberId(memberId);
        long followingCount = followQueryService.countFollowing(memberId);
        long followerCount = followQueryService.countFollower(memberId);

        return MemberStatsResponse.from(
            reviewCount,
            followingCount,
            followerCount
        );
    }
}
