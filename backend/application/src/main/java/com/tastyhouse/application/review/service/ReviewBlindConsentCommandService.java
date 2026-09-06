package com.tastyhouse.application.review.service;

import com.tastyhouse.application.shared.marker.WebApp;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.review.service.ReviewBlindRequestService;
import com.tastyhouse.domain.review.vo.ReviewId;
import com.tastyhouse.application.review.port.in.ReviewBlindConsentCommand;
import com.tastyhouse.application.review.port.in.ReviewBlindConsentCommandUseCase;
import com.tastyhouse.application.review.port.in.ReviewBlindRejectCommand;

@Service
@WebApp
@Transactional
public class ReviewBlindConsentCommandService implements ReviewBlindConsentCommandUseCase {

    private final ReviewBlindRequestService reviewBlindRequestService;

    public ReviewBlindConsentCommandService(ReviewBlindRequestService reviewBlindRequestService) {
        this.reviewBlindRequestService = reviewBlindRequestService;
    }

    @Override
    public void consent(ReviewBlindConsentCommand command) {
        reviewBlindRequestService.consentToDelete(ReviewId.of(command.reviewId()), MemberId.of(command.memberId()));
    }

    @Override
    public void reject(ReviewBlindRejectCommand command) {
        reviewBlindRequestService.rejectDeletion(ReviewId.of(command.reviewId()), MemberId.of(command.memberId()));
    }
}
