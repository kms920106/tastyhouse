package com.tastyhouse.application.member.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.member.port.in.MemberMyReviewListQueryUseCase;
import com.tastyhouse.application.review.port.out.MyReviewListItemResult;
import com.tastyhouse.application.shared.port.out.page.PageResult;

@Service
@Transactional(readOnly = true)
class MemberMyReviewListQueryService implements MemberMyReviewListQueryUseCase {

    private final MemberReviewService memberReviewService;

    public MemberMyReviewListQueryService(MemberReviewService memberReviewService) {
        this.memberReviewService = memberReviewService;
    }

    @Override
    public PageResult<MyReviewListItemResult> getMyReviews(Long memberId, int page, int size) {
        return memberReviewService.getMyReviews(memberId, page, size);
    }
}
