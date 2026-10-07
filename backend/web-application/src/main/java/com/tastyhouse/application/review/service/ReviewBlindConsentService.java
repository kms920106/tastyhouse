package com.tastyhouse.application.review.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.review.vo.ReviewId;
import com.tastyhouse.application.review.port.in.ReviewBlindConsentCommand;
import com.tastyhouse.application.review.port.in.ReviewBlindConsentUseCase;

@Service
@Transactional
class ReviewBlindConsentService implements ReviewBlindConsentUseCase {

    private final ReviewBlindRequestService reviewBlindRequestService;

    public ReviewBlindConsentService(ReviewBlindRequestService reviewBlindRequestService) {
        this.reviewBlindRequestService = reviewBlindRequestService;
    }

    @Override
    public void consent(ReviewBlindConsentCommand command) {
        reviewBlindRequestService.consentToDelete(ReviewId.of(command.reviewId()), MemberId.of(command.memberId()));
    }
}
