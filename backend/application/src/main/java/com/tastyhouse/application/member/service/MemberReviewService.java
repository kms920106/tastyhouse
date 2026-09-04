package com.tastyhouse.application.member.service;

import com.tastyhouse.application.shared.marker.WebApp;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shared.page.PageResult;

import com.tastyhouse.application.review.port.out.MyReviewListItemResult;
import com.tastyhouse.application.review.service.ReviewQueryService;

@Service
@WebApp
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
