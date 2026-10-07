package com.tastyhouse.application.review.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.review.model.ReviewBlindReason;
import com.tastyhouse.domain.review.model.ReviewBlindStatus;
import com.tastyhouse.application.review.port.in.ReviewBlindRequestDetailQueryUseCase;
import com.tastyhouse.application.review.port.out.ReviewBlindRequestDetailResult;
import com.tastyhouse.application.review.port.out.ReviewBlindRequestManagementQueryPort;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;

@Service
@Transactional(readOnly = true)
class ReviewBlindRequestDetailQueryService implements ReviewBlindRequestDetailQueryUseCase {

    private final ReviewBlindRequestManagementQueryPort reviewBlindRequestManagementQueryPort;

    public ReviewBlindRequestDetailQueryService(ReviewBlindRequestManagementQueryPort reviewBlindRequestManagementQueryPort) {
        this.reviewBlindRequestManagementQueryPort = reviewBlindRequestManagementQueryPort;
    }

    @Override
    public ReviewBlindRequestDetailResult getBlindRequest(Long id) {
        return reviewBlindRequestManagementQueryPort.findBlindRequestDetail(id)
            .map(detail -> detail.withDescriptions(reasonDescription(detail.reason()), statusDescription(detail.status())))
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.REVIEW_BLIND_REQUEST_NOT_FOUND));
    }

    private static String reasonDescription(String reason) {
        return reason == null ? null : ReviewBlindReason.valueOf(reason).getDescription();
    }

    private static String statusDescription(String status) {
        return status == null ? null : ReviewBlindStatus.valueOf(status).getDescription();
    }
}
