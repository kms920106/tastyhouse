package com.tastyhouse.application.member.service;

import com.tastyhouse.application.shared.marker.WebApp;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.follow.service.FollowQueryService;
import com.tastyhouse.application.member.port.in.MemberStatsQueryUseCase;
import com.tastyhouse.application.member.port.out.MemberStatsResult;
import com.tastyhouse.application.review.service.ReviewQueryService;

@Service
@WebApp
public class MemberStatsQueryService implements MemberStatsQueryUseCase {

    private final ReviewQueryService reviewQueryService;
    private final FollowQueryService followQueryService;

    public MemberStatsQueryService(ReviewQueryService reviewQueryService, FollowQueryService followQueryService) {
        this.reviewQueryService = reviewQueryService;
        this.followQueryService = followQueryService;
    }

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
