package com.tastyhouse.application.review.service;

import com.tastyhouse.application.shared.marker.AdminApp;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.review.port.in.ReviewBlindRequestApproveCommand;
import com.tastyhouse.application.review.port.in.ReviewBlindRequestManagementCommandUseCase;
import com.tastyhouse.application.review.port.in.ReviewBlindRequestRejectCommand;
import com.tastyhouse.domain.review.service.ReviewBlindRequestService;

@Service
@AdminApp
@Transactional
public class ReviewBlindRequestManagementCommandService implements ReviewBlindRequestManagementCommandUseCase {

    private final ReviewBlindRequestService reviewBlindRequestService;

    public ReviewBlindRequestManagementCommandService(ReviewBlindRequestService reviewBlindRequestService) {
        this.reviewBlindRequestService = reviewBlindRequestService;
    }

    @Override
    public void approveBlindRequest(ReviewBlindRequestApproveCommand command) {
        Long id = command.requestId();
        reviewBlindRequestService.approve(id, LocalDateTime.now());
    }

    @Override
    public void rejectBlindRequest(ReviewBlindRequestRejectCommand command) {
        Long id = command.requestId();
        String rejectReason = command.rejectReason();
        reviewBlindRequestService.reject(id, rejectReason);
    }
}
