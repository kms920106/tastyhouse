package com.tastyhouse.webapi.member.service;

import com.tastyhouse.core.service.FollowCoreService;
import com.tastyhouse.core.service.ReviewCoreService;
import com.tastyhouse.webapi.member.response.MemberStatsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberFollowService {

    private final ReviewCoreService reviewCoreService;
    private final FollowCoreService followCoreService;

    // 회원의 리뷰 수, 팔로잉 수, 팔로워 수를 조회
    @Transactional(readOnly = true)
    public MemberStatsResponse getMemberStats(Long memberId) {
        long reviewCount = reviewCoreService.countVisibleReviewsByMemberId(memberId);
        long followingCount = followCoreService.countFollowing(memberId);
        long followerCount = followCoreService.countFollower(memberId);

        return MemberStatsResponse.from(
            reviewCount,
            followingCount,
            followerCount
        );
    }
}
