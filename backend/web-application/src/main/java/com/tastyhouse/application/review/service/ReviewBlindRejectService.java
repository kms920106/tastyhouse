package com.tastyhouse.application.review.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.review.vo.ReviewId;
import com.tastyhouse.application.review.port.in.ReviewBlindRejectCommand;
import com.tastyhouse.application.review.port.in.ReviewBlindRejectUseCase;

@Service
@Transactional
class ReviewBlindRejectService implements ReviewBlindRejectUseCase {

    private final ReviewBlindRequestService reviewBlindRequestService;

    public ReviewBlindRejectService(ReviewBlindRequestService reviewBlindRequestService) {
        this.reviewBlindRequestService = reviewBlindRequestService;
    }

    @Override
    public void reject(ReviewBlindRejectCommand command) {
        reviewBlindRequestService.rejectDeletion(ReviewId.of(command.reviewId()), MemberId.of(command.memberId()));
    }
}
