package com.tastyhouse.webapplication.member.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.webapplication.follow.service.FollowQueryService;
import com.tastyhouse.webapplication.member.response.MemberStatsResponse;
import com.tastyhouse.webapplication.member.port.in.MemberStatsQueryUseCase;
import com.tastyhouse.webapplication.review.service.ReviewQueryService;

/**
 * 회원 통계(리뷰 수·팔로잉 수·팔로워 수) 조회 전용 서비스.
 *
 * <p>팔로우 등록·해제 도메인 로직은 domain-module의
 * {@code com.tastyhouse.domain.member.follow.service.MemberFollowService}가 담당하며,
 * 이 클래스는 표현용 집계 조회만 수행한다.
 */
@Service
public class MemberStatsQueryService implements MemberStatsQueryUseCase {

    private final ReviewQueryService reviewQueryService;
    private final FollowQueryService followQueryService;

    public MemberStatsQueryService(ReviewQueryService reviewQueryService, FollowQueryService followQueryService) {
        this.reviewQueryService = reviewQueryService;
        this.followQueryService = followQueryService;
    }

    // 회원의 리뷰 수, 팔로잉 수, 팔로워 수를 조회
    @Transactional(readOnly = true)
    @Override
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
