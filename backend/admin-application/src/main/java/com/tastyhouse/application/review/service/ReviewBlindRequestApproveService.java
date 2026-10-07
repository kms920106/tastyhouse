package com.tastyhouse.application.review.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.review.port.in.ReviewBlindRequestApproveCommand;
import com.tastyhouse.application.review.port.in.ReviewBlindRequestApproveUseCase;

@Service
@Transactional
class ReviewBlindRequestApproveService implements ReviewBlindRequestApproveUseCase {

    private final ReviewBlindRequestService reviewBlindRequestService;

    public ReviewBlindRequestApproveService(ReviewBlindRequestService reviewBlindRequestService) {
        this.reviewBlindRequestService = reviewBlindRequestService;
    }

    @Override
    public void approveBlindRequest(ReviewBlindRequestApproveCommand command) {
        Long id = command.requestId();
        reviewBlindRequestService.approve(id, LocalDateTime.now());
    }
}
