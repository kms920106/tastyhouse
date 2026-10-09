package com.tastyhouse.application.review.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.application.member.follow.port.out.MemberFollowQueryPort;
import com.tastyhouse.application.review.port.in.ReviewLatestListQueryUseCase;
import com.tastyhouse.application.review.port.out.LatestReviewListItemResult;
import com.tastyhouse.application.review.port.out.ReviewFeedQueryPort;
import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;

@Service
@Transactional(readOnly = true)
class ReviewLatestListQueryService implements ReviewLatestListQueryUseCase {

    private final ReviewFeedQueryPort reviewFeedQueryPort;
    private final MemberFollowQueryPort memberFollowQueryPort;

    public ReviewLatestListQueryService(
        ReviewFeedQueryPort reviewFeedQueryPort,
        MemberFollowQueryPort memberFollowQueryPort
    ) {
        this.reviewFeedQueryPort = reviewFeedQueryPort;
        this.memberFollowQueryPort = memberFollowQueryPort;
    }

    @Override
    public PageResult<LatestReviewListItemResult> searchLatestReviewList(
        int page,
        int size,
        String type,
        Long memberId
    ) {
        if (ReviewListType.from(type) == ReviewListType.FOLLOWING && memberId != null) {
            return findLatestReviewsByFollowing(MemberId.of(memberId), page, size);
        }
        return reviewFeedQueryPort.findLatestReviews(PageQuery.of(page, size));
    }

    private PageResult<LatestReviewListItemResult> findLatestReviewsByFollowing(MemberId memberId, int page, int size) {
        List<Long> followingMemberIds = memberFollowQueryPort.findFollowingIds(memberId.value());

        if (followingMemberIds.isEmpty()) {
            return PageResult.empty(page, size);
        }

        return reviewFeedQueryPort.findLatestReviewsByFollowing(followingMemberIds, PageQuery.of(page, size));
    }
}
