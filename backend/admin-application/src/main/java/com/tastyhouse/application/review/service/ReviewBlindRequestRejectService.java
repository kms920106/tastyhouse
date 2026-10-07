package com.tastyhouse.application.review.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.review.port.in.ReviewBlindRequestRejectCommand;
import com.tastyhouse.application.review.port.in.ReviewBlindRequestRejectUseCase;

@Service
@Transactional
class ReviewBlindRequestRejectService implements ReviewBlindRequestRejectUseCase {

    private final ReviewBlindRequestService reviewBlindRequestService;

    public ReviewBlindRequestRejectService(ReviewBlindRequestService reviewBlindRequestService) {
        this.reviewBlindRequestService = reviewBlindRequestService;
    }

    @Override
    public void rejectBlindRequest(ReviewBlindRequestRejectCommand command) {
        Long id = command.requestId();
        String rejectReason = command.rejectReason();
        reviewBlindRequestService.reject(id, rejectReason);
    }
}
