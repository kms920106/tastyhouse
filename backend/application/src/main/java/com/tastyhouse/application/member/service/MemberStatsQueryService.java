package com.tastyhouse.application.member.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.follow.port.in.FollowQueryUseCase;
import com.tastyhouse.application.member.port.in.MemberStatsQueryUseCase;
import com.tastyhouse.application.member.port.out.MemberStatsResult;
import com.tastyhouse.application.review.port.in.ReviewQueryUseCase;
import com.tastyhouse.application.shared.marker.WebApp;

@Service
@WebApp
public class MemberStatsQueryService implements MemberStatsQueryUseCase {

    private final ReviewQueryUseCase reviewQueryUseCase;
    private final FollowQueryUseCase followQueryUseCase;

    public MemberStatsQueryService(ReviewQueryUseCase reviewQueryUseCase, FollowQueryUseCase followQueryUseCase) {
        this.reviewQueryUseCase = reviewQueryUseCase;
        this.followQueryUseCase = followQueryUseCase;
    }

    @Transactional(readOnly = true)
    @Override
    public MemberStatsResult getMemberStats(Long memberId) {
        long reviewCount = reviewQueryUseCase.countVisibleReviewsByMemberId(memberId);
        long followingCount = followQueryUseCase.countFollowing(memberId);
        long followerCount = followQueryUseCase.countFollower(memberId);

        return new MemberStatsResult(
            reviewCount,
            followingCount,
            followerCount
        );
    }
}
