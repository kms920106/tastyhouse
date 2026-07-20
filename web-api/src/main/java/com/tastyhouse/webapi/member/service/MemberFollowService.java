package com.tastyhouse.webapi.member.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.core.domain.member.follow.application.MemberFollowQueryService;
import com.tastyhouse.core.domain.review.application.ReviewQueryService;
import com.tastyhouse.webapi.member.response.MemberStatsResponse;

@Service
@RequiredArgsConstructor
public class MemberFollowService {

    private final ReviewQueryService reviewQueryService;
    private final MemberFollowQueryService followQueryService;

    // 회원의 리뷰 수, 팔로잉 수, 팔로워 수를 조회
    @Transactional(readOnly = true)
    public MemberStatsResponse getMemberStats(Long memberId) {
        long reviewCount = reviewQueryService.countVisibleReviewsByMemberId(MemberId.of(memberId));
        long followingCount = followQueryService.countFollowing(MemberId.of(memberId));
        long followerCount = followQueryService.countFollower(MemberId.of(memberId));

        return MemberStatsResponse.from(
            reviewCount,
            followingCount,
            followerCount
        );
    }
}
