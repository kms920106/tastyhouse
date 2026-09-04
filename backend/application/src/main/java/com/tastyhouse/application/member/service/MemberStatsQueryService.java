package com.tastyhouse.application.member.service;

import com.tastyhouse.application.shared.marker.WebApp;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.follow.service.FollowQueryService;
import com.tastyhouse.application.member.port.in.MemberStatsQueryUseCase;
import com.tastyhouse.application.member.port.out.MemberStatsResult;
import com.tastyhouse.application.review.service.ReviewQueryService;

/**
 * 회원 통계(리뷰 수·팔로잉 수·팔로워 수) 조회 전용 서비스.
 *
 * <p>팔로우 등록·해제 도메인 로직은 domain-module의
 * {@code com.tastyhouse.domain.member.follow.service.MemberFollowService}가 담당하며,
 * 이 클래스는 표현용 집계 조회만 수행한다.
 */
@Service
@WebApp
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
    public MemberStatsResult getMemberStats(Long memberId) {
        long reviewCount = reviewQueryService.countVisibleReviewsByMemberId(memberId);
        long followingCount = followQueryService.countFollowing(memberId);
        long followerCount = followQueryService.countFollower(memberId);

        return new MemberStatsResult(
            reviewCount,
            followingCount,
            followerCount
        );
    }
}
