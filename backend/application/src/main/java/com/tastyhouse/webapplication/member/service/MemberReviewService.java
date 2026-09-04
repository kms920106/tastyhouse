package com.tastyhouse.webapplication.member.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shared.page.PageResult;

import com.tastyhouse.application.review.port.out.MyReviewListItemResult;
import com.tastyhouse.webapplication.review.service.ReviewQueryService;

@Service
public class MemberReviewService {

    private final ReviewQueryService reviewQueryService;

    public MemberReviewService(ReviewQueryService reviewQueryService) {
        this.reviewQueryService = reviewQueryService;
    }

    @Transactional(readOnly = true)
    public PageResult<MyReviewListItemResult> getMyReviews(Long memberId, int page, int size) {
        return reviewQueryService.findMyReviews(memberId, page, size);
    }

    @Transactional(readOnly = true)
    public long getMyReviewCount(Long memberId) {
        return reviewQueryService.countVisibleReviewsByMemberId(memberId);
    }
}
