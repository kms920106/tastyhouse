package com.tastyhouse.application.review.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.review.port.in.ReviewMemberCountQueryUseCase;
import com.tastyhouse.application.review.port.out.ReviewStatisticsQueryPort;

@Service
@Transactional(readOnly = true)
class ReviewMemberCountQueryService implements ReviewMemberCountQueryUseCase {

    private final ReviewStatisticsQueryPort reviewStatisticsQueryPort;

    public ReviewMemberCountQueryService(ReviewStatisticsQueryPort reviewStatisticsQueryPort) {
        this.reviewStatisticsQueryPort = reviewStatisticsQueryPort;
    }

    @Override
    public long countVisibleReviewsByMemberId(Long memberId) {
        return reviewStatisticsQueryPort.countVisibleReviewsByMemberId(memberId);
    }
}
