package com.tastyhouse.webapi.member.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.apicommon.common.PaginationResponse;
import com.tastyhouse.webapi.member.response.MyReviewCountResponse;
import com.tastyhouse.webapi.member.response.MyReviewListItemResponse;
import com.tastyhouse.webapi.review.ReviewQueryService;

@Service
public class MemberReviewService {

    private final ReviewQueryService reviewQueryService;

    public MemberReviewService(ReviewQueryService reviewQueryService) {
        this.reviewQueryService = reviewQueryService;
    }

    @Transactional(readOnly = true)
    public PaginationResponse<MyReviewListItemResponse> getMyReviews(Long memberId, int page, int size) {
        return PaginationResponse.from(reviewQueryService.findMyReviews(memberId, page, size)
            .map(dto -> MyReviewListItemResponse.from(
                dto.id(),
                dto.imageUrl(),
                dto.ownerOnly()
            )));
    }

    @Transactional(readOnly = true)
    public MyReviewCountResponse getMyReviewCount(Long memberId) {
        long count = reviewQueryService.countVisibleReviewsByMemberId(memberId);
        return MyReviewCountResponse.from(count);
    }
}
