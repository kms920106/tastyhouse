package com.tastyhouse.application.member.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.follow.port.in.FollowerCountQueryUseCase;
import com.tastyhouse.application.follow.port.in.FollowingCountQueryUseCase;
import com.tastyhouse.application.member.port.in.MemberStatsQueryUseCase;
import com.tastyhouse.application.member.port.out.MemberStatsResult;
import com.tastyhouse.application.review.port.in.ReviewMemberCountQueryUseCase;

@Service
class MemberStatsQueryService implements MemberStatsQueryUseCase {

    private final ReviewMemberCountQueryUseCase reviewMemberCountQueryUseCase;
    private final FollowingCountQueryUseCase followingCountQueryUseCase;
    private final FollowerCountQueryUseCase followerCountQueryUseCase;

    public MemberStatsQueryService(
        ReviewMemberCountQueryUseCase reviewMemberCountQueryUseCase,
        FollowingCountQueryUseCase followingCountQueryUseCase,
        FollowerCountQueryUseCase followerCountQueryUseCase
    ) {
        this.reviewMemberCountQueryUseCase = reviewMemberCountQueryUseCase;
        this.followingCountQueryUseCase = followingCountQueryUseCase;
        this.followerCountQueryUseCase = followerCountQueryUseCase;
    }

    @Transactional(readOnly = true)
    @Override
    public MemberStatsResult getMemberStats(Long memberId) {
        long reviewCount = reviewMemberCountQueryUseCase.countVisibleReviewsByMemberId(memberId);
        long followingCount = followingCountQueryUseCase.countFollowing(memberId);
        long followerCount = followerCountQueryUseCase.countFollower(memberId);

        return new MemberStatsResult(
            reviewCount,
            followingCount,
            followerCount
        );
    }
}
