package com.tastyhouse.application.review.service;

import com.tastyhouse.application.shared.marker.AdminApp;
import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.review.model.ReviewBlindReason;
import com.tastyhouse.domain.review.model.ReviewBlindStatus;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.application.review.port.out.ReviewBlindRequestDetailResult;
import com.tastyhouse.application.review.port.out.ReviewBlindRequestListItemResult;
import com.tastyhouse.application.review.port.out.ReviewBlindRequestManagementQueryPort;
import com.tastyhouse.application.review.port.out.ReviewBlindRequestSearchCondition;
import com.tastyhouse.application.review.port.in.ReviewBlindRequestQueryUseCase;

@Service
@AdminApp
@Transactional(readOnly = true)
public class ReviewBlindRequestQueryService implements ReviewBlindRequestQueryUseCase {

    private final ReviewBlindRequestManagementQueryPort reviewBlindRequestManagementQueryPort;

    public ReviewBlindRequestQueryService(ReviewBlindRequestManagementQueryPort reviewBlindRequestManagementQueryPort) {
        this.reviewBlindRequestManagementQueryPort = reviewBlindRequestManagementQueryPort;
    }

    @Override
    public PageResult<ReviewBlindRequestListItemResult> getBlindRequests(
        Long shopId,
        String status,
        String reason,
        LocalDate startDate,
        LocalDate endDate,
        int page,
        int size
    ) {
        ReviewBlindStatus blindStatus = status == null ? null : ReviewBlindStatus.from(status);
        ReviewBlindReason blindReason = reason == null ? null : ReviewBlindReason.from(reason);

        ReviewBlindRequestSearchCondition condition = ReviewBlindRequestSearchCondition.of(
            shopId, blindStatus, blindReason, startDate, endDate
        );
        return reviewBlindRequestManagementQueryPort.findBlindRequestPage(condition, PageQuery.of(page, size));
    }

    @Override
    public ReviewBlindRequestDetailResult getBlindRequest(Long id) {
        return reviewBlindRequestManagementQueryPort.findBlindRequestDetail(id)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.REVIEW_BLIND_REQUEST_NOT_FOUND));
    }
}
